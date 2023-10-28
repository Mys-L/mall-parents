package com.mall.cart.vo;

import lombok.Data;
import lombok.ToString;
/**
 * @ClassName: UserInfoTo
 * @Description:
 * @Author: L
 * @Create: 2023-10-28,  13:41
 * @Version: 1.0
 */
@ToString
@Data
public class UserInfoTo {

    private Long userId;
    private String userKey; //一定封装
    private boolean tempUser = false;
}
