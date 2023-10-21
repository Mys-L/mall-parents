package com.mall.thirdparty.controller;

import com.mall.common.exception.BizCodeEnum;
import com.mall.common.utils.R;
import com.mall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName: SmsSendController
 * @Description:
 * @Author: L
 * @Create: 2023-10-16 14:02
 * @Version: 1.0
 */

@Controller
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    private SmsComponent smsComponent;

    /*** 提供给别的服务进行调用的
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        if(!"fail".equals(smsComponent.sendSmsCode(phone, code).split("_")[0])){
            return R.ok();
        }
        return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
    }
}
