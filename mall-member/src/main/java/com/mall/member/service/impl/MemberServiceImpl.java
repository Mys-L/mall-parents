package com.mall.member.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.HttpUtils;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.member.dao.MemberDao;
import com.mall.member.dao.MemberLevelDao;
import com.mall.member.entity.MemberEntity;
import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.exception.PhoneExistException;
import com.mall.member.exception.UserNameExistException;
import com.mall.member.service.MemberService;
import com.mall.member.vo.MemberLoginVo;
import com.mall.member.vo.MemberRegisterVo;
import com.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author L
 */
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo registerVo) {
        MemberEntity entity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());
        // 检查手机号 用户名是否唯一 // 不一致则抛出异常
        checkPhone(registerVo.getPhone());
        checkUserName(registerVo.getUserName());
        entity.setMobile(registerVo.getPhone());
        entity.setUsername(registerVo.getUserName());

        // 密码要加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        entity.setPassword(bCryptPasswordEncoder.encode(registerVo.getPassword()));
        entity.setNickname(registerVo.getUserName());
        // 其他的默认信息
        entity.setCity("北京");
        entity.setCreateTime(new Date());
        entity.setStatus(0);
        entity.setBirth(new Date());
        entity.setEmail("L@mail.com");
        entity.setGender(1);
        entity.setJob("JAVA");
        this.baseMapper.insert(entity);
    }
    @Override
    public void checkPhone(String phone) {
        if(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0){
            throw new PhoneExistException();
        }
    }
    @Override
    public void checkUserName(String userName) {
        if(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName)) > 0){
            throw new UserNameExistException();
        }
    }

    /**
     * 用户登录
     * @param vo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        // 去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if(entity == null){
            // 登录失败
            return null;
        }else{
            // 前面传一个明文密码 后面传一个编码后的密码
            boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(), entity.getPassword());
            if (matches){
                entity.setPassword(null);
                return entity;
            }else {
                return null;
            }
        }
    }

    @Override // 已经用code生成了token
    public MemberEntity login(SocialUser socialUser) {
        // 微博的uid
        String uid = socialUser.getUid();
        // 1.判断社交用户登录过系统
        MemberDao dao = this.baseMapper;
        MemberEntity entity = dao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        MemberEntity memberEntity = new MemberEntity();
        if(entity != null){ // 注册过
            // 说明这个用户注册过, 修改它的资料
            // 更新令牌
            memberEntity.setId(entity.getId());
            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());
            // 更新
            dao.updateById(memberEntity);
            entity.setAccessToken(socialUser.getAccessToken());
            entity.setExpiresIn(socialUser.getExpiresIn());
            entity.setPassword(null);
            return entity;
        }else{ // 没有注册过
            // 2. 没有查到当前社交用户对应的记录 我们就需要注册一个
            HashMap<String, String> map = new HashMap<>();
            map.put("access_token", socialUser.getAccessToken());
            map.put("uid", socialUser.getUid());
            try {
                // 3. 查询当前社交用户账号信息(昵称、性别、头像等)
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), map);
                if(response.getStatusLine().getStatusCode() == 200){
                    // 查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    // 这个JSON对象什么样的数据都可以直接获取
                    JSONObject jsonObject = JSON.parseObject(json);
                    memberEntity.setNickname(jsonObject.getString("name"));
                    memberEntity.setUsername(jsonObject.getString("name"));
                    memberEntity.setGender("m".equals(jsonObject.getString("gender"))?1:0);
                    memberEntity.setCity(jsonObject.getString("location"));
                    memberEntity.setJob("自媒体");
                    memberEntity.setEmail(jsonObject.getString("email"));
                }
            } catch (Exception e) {
                log.warn("社交登录时远程调用出错 [尝试修复]");
            }
            memberEntity.setStatus(0);
            memberEntity.setCreateTime(new Date());
            memberEntity.setBirth(new Date());
            memberEntity.setLevelId(1L);
            memberEntity.setSocialUid(socialUser.getUid());
            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());
            // 注册 -- 登录成功
            dao.insert(memberEntity);
            memberEntity.setPassword(null);
            return memberEntity;
        }
    }
}