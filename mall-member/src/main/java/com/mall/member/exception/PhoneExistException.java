package com.mall.member.exception;

/**
 * @ClassName: PhoneExistException
 * @Description:
 * @Author: L
 * @Create: 2023-10-21 16:37
 * @Version: 1.0
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号存在");
    }
}
