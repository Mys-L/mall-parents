package com.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
        // 其他的默认信息
        entity.setCity("北京");
        entity.setCreateTime(new Date());
        entity.setStatus(0);
        entity.setNickname(registerVo.getUserName());
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
}