package com.mall.member.exception;

/**
 * @ClassName: UserNameExistException
 * @Description:
 * @Author: L
 * @Create: 2023-10-21 16:38
 * @Version: 1.0
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名存在");
    }
}
