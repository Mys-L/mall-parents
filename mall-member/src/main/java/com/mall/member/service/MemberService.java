package com.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.PhoneExistException;
import com.mall.member.exception.UserNameExistException;
import com.mall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:55:33
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo registerVo);
    void checkPhone(String phone) throws PhoneExistException;
    void checkUserName(String userName) throws UserNameExistException;
}

