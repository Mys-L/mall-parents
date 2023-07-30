package com.mall.product.vo;

import com.mall.product.entity.AttrEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AttrGroupWithAttrsVo
 * @Description:
 * @Author: L
 * @Create: 2023-07-26 11:34
 * @Version: 1.0
 */
@Data
public class AttrGroupWithAttrsVo implements Serializable {


    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 保存整个实体信息
     */
    private List<AttrEntity> attrs;
}
