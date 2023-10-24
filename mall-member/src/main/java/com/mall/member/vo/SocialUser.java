package com.mall.member.vo;

import lombok.Data;

/**
 * @ClassName: SocialUserVo
 * @Description:
 * @Author: L
 * @Create: 2023-10-24 14:19
 * @Version: 1.0
 */
@Data
public class SocialUser {
    private String accessToken;
    private String remindIn;
    private Long expiresIn;
    private String uid;
    private String isrealname;
}
