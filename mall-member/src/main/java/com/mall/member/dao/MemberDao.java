package com.mall.member.dao;

import com.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:55:33
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
