package com.mall.product.exception;

import com.mall.common.exception.BizCodeEnum;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


/**
 * @author L
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice (basePackages = "com.mall.product.controller")
public class MallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class) // 也可以返回ModelAndView
    public R handleValidException(MethodArgumentNotValidException exception){
        Map<String,String> map=new HashMap<>();
        // 获取数据校验的错误结果
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage();
            String field = fieldError.getField();
            map.put(field,message);
        });
        log.error("数据校验出现问题{},异常类型{}",exception.getMessage(),exception.getClass());
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(),BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data",map);
    }
    @ExceptionHandler(value = Throwable.class) // 处理任意类型异常
    public R handleException(Throwable throwable){
        log.error("未知异常{},异常类型{}",throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(),BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}
