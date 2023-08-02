package com.mall.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.dao.CategoryDao;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryBrandRelationService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.Catelog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //@Autowired
    //CategoryDao categoryDao
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类，组装成tree
        //List<CategoryEntity> entities = categoryDao.selectList(null);
        List<CategoryEntity> entities = baseMapper.selectList(null);
        List<CategoryEntity> levelMenus = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).toList();//注释：stream().toList() 新能大于stream().collect(Collectors.toList())。 toList()实际上这个方法是Java 16才支持的一个方法
        return levelMenus;
    }

    /**
     * 递归
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity) -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).toList();
        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        //TODO 检查当前被删除菜单是否被其它地方引用
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

    /**
     * 级联更新相关数据信息
     * @CachePut 将修改的数据放到缓存 ：双写模式
     * @CacheEvict:失效模式
     * 1、同时进行多种缓存操作  @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀
     *      @Caching(evict = { 不加单引号会认为是动态取值
     *          @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
     *          @CacheEvict(value = "category",key = "'getCatalogJson'")
     *      })
     */

    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        //TODO 更新其它关联
    }

    /**
     * 查询导航所有一级分类
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 2、 @Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *      1）、如果缓存中有，方法不用调用。
     *      2）、key默认自动生成；缓存的名字::SimpleKey [](自主生成的key值)
     *      3）、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4）、默认ttl时间 -1；
     *    自定义：
     *      1）、指定生成的缓存使用的key：  key属性指定，接受一个SpEL
     *             SpEL的详细 https://docs.spring.io/spring/docs/5.1.12.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *      2）、指定缓存的数据的存活时间： 配置文件中修改ttl
     *      3）、将数据保存为json格式:
     *              自定义RedisCacheConfiguration即可
     * 4、Spring-Cache的不足；
     *      1）、读模式：
     *          缓存穿透：查询一个null数据。解决：缓存空数据；ache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；？默认是无加锁的;sync = true（加锁，解决击穿）
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间。：spring.cache.redis.time-to-live=3600000
     *      2）、写模式：（缓存与数据库一致）
     *          1）、读写加锁。
     *          2）、引入Canal，感知到MySQL的更新去更新数据库
     *          3）、读多写多，直接去数据库查询就行
     *    总结：
     *      常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用Spring-Cache；写模式（只要缓存的数据有过期时间就足够了）
     *      特殊数据：特殊设计
     *   原理：
     *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     * sync = true只有Cacheable 缓存加锁：加的是本地锁 synchronized 解决击穿
     */
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> list = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("消耗时间 ============> " + (System.currentTimeMillis() - l));
        return list;
    }

    /**
     * 使用 springcache 注解
     * 获取二级菜单
     * sync = true只有Cacheable 缓存加锁：加的是本地锁 synchronized 解决击穿
     */
    @Cacheable(value = "category",key = "#root.methodName",sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        System.out.println("================查询了数据库================");
        List<CategoryEntity> selectAllList = this.baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParentCid(selectAllList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParentCid(selectAllList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = new ArrayList<>();
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3Catelog = getParentCid(selectAllList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> voList = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).toList();
                        catelog2Vo.setCatalog3List(voList);
                    }
                    return catelog2Vo;
                }).toList();
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }
//------------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * 加入缓存逻辑
     * 缓存穿透，雪崩，击穿等问题
     * 加锁 缓存击穿：synchronized ()
     */
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //加入缓存
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            return catalogJsonFromDB;
        }
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * redisson 分布式锁 是redis的升级
     * 缓存里面的数据如何和数据库保持一致
     * 缓存一致性问题：
     *      双写模式 ->会有脏数据的问题,解决 1加锁 2允不允许暂时有脏数据
     *      失效模式 ->也会有这样问题
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        //锁的名字
        RLock redissonLock = redisson.getLock("catalogJson-lock");
        Map<String, List<Catelog2Vo>> dataFromDB = new HashMap<>();
        redissonLock.lock();
        try {
            dataFromDB = getDataFromDB();
        } finally {
            redissonLock.unlock();
        }

        return dataFromDB;
    }

    /**
     * redis 分布式 简单逻辑
     * 存在问题：如果在查询数据库时，出现异常。导致没有删除锁的问题=导致死锁
     * 解决：添加过期时间(只加过期时间也不行) 需要加锁和过期时间必须原子性
     * 问题2 ： 删除锁的时候必须删除自己的锁，不能删除别人的锁 查询和删除必须原子操作
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        //抢占redis分布式锁，去redis占位
        String uuid = UUID.randomUUID().toString();
        Boolean addLock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (addLock) {
            Map<String, List<Catelog2Vo>> dataFromDB = null;
            try {
                //加锁成功 执行业务
                dataFromDB = getDataFromDB();
            } finally {
                //获取值对比+对比成功删除=原子操作  lua脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long delLock = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {
            //加锁失败   重试=自旋
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("redis 锁:自旋等待加锁时出现异常，原因:" + e.getMessage());
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    /**
     * 查询所有分类菜单 加入缓存优化 重新提取方法
     * 从数据库查询并封装数据
     * 本地锁  synchronized
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        //当前实例容器，单体应用没有问题，分布式需要多台服务器
        synchronized (this) {
            return getDataFromDB();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            System.out.println("================缓存命中无需查询数据库================");
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("================查询了数据库================");
        List<CategoryEntity> selectAllList = this.baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParentCid(selectAllList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParentCid(selectAllList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = new ArrayList<>();
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> level3Catelog = getParentCid(selectAllList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> voList = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).toList();
                        catelog2Vo.setCatalog3List(voList);
                    }
                    return catelog2Vo;
                }).toList();
            }
            return catelog2Vos;
        }));
        redisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }
//------------------------------------------------------------------------------------------------------------------------------------------------
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectAllList, Long parentCid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> list = selectAllList.stream().filter(item -> item.getParentCid() == parentCid).toList();
        return list;
    }

}