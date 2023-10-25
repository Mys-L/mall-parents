package com.mall.auth.web;

import com.alibaba.fastjson2.TypeReference;
import com.mall.auth.feign.MemberFeignService;
import com.mall.auth.feign.ThirdPartFeignService;
import com.mall.auth.vo.UserLoginVo;
import com.mall.auth.vo.UserRegisterVo;
import com.mall.common.constant.Constant;
import com.mall.common.exception.BizCodeEnum;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: LoginController
 * @Description:
 * @Author: L
 * @Create: 2023-08-13 15:27
 * @Version: 1.0
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 网页登录 去掉MyMallWebConfig 配置
     *
     */
    @GetMapping({"/login.html","/","/index","/index.html"}) // auth
    public String loginPage(HttpSession session){
        // 从会话从获取loginUser
        Object attribute = session.getAttribute(Constant.LOGIN_USER);// "loginUser";
        System.out.println("attribute:"+attribute);
        if(attribute == null){
            return "login";
        }
        System.out.println("已登陆过，重定向到首页");
        return "redirect:http://mall.com";
    }

    /**
     * 用户登录请求
     */
    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, // from表单里带过来的
                        RedirectAttributes redirectAttributes,
                        HttpSession session){
        // 远程登录
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode() == 0){
            // 登录成功
            MemberRespVo respVo = r.getData("data", new TypeReference<MemberRespVo>() {});
            // 放入session
            session.setAttribute(Constant.LOGIN_USER, respVo);//loginUser
            log.info("\n欢迎 [" + respVo.getUsername() + "] 登录");
            return "redirect:http://mall.com";
        }else {
            HashMap<String, String> error = new HashMap<>();
            // 获取错误信息
            error.put("msg", r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.mall.com/login.html";
        }
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //  TODO 接口防刷(冷却时长递增)，redis缓存 sms:code:电话号
        String redisCode = stringRedisTemplate.opsForValue().get(Constant.SMS_CODE_CACHE_PREFIX + phone);
        // 如果不为空，返回错误信息
        if (null != redisCode && redisCode.length() > 0) {
            long CuuTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - CuuTime < 60 * 1000) { // 60s
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 生成验证码
        String code = UUID.randomUUID().toString().substring(0, 6);
        String redis_code = code + "_" + System.currentTimeMillis();
        // 缓存验证码
        stringRedisTemplate.opsForValue().set(Constant.SMS_CODE_CACHE_PREFIX + phone, redis_code, 10, TimeUnit.MINUTES);
        try {// 调用第三方短信服务
            return thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.warn("远程调用不知名错误 [无需解决]");
        }
        return R.ok();
    }

    /**
     * 注册 重定向携带数据，利用session原理，将数据放到session中。分布式session共享问题？
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 将错误属性与错误信息一一封装
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            // addFlashAttribute 这个数据只取一次
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
        // 开始注册 调用远程服务
        // 1.校验验证码
        String code = userRegisterVo.getCode();
        String redis_code = stringRedisTemplate.opsForValue().get(Constant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(redis_code)) {
            // 验证码通过
            if (code.equals(redis_code.split("_")[0])) {
                // 删除验证码
                stringRedisTemplate.delete(Constant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
                // 调用远程服务进行注册
                R r = memberFeignService.register(userRegisterVo);
                if (r.getCode() == 0) {
                    // 注册成功，去登录
                    return "redirect:http://auth.mall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    // 数据只需要取一次
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                // addFlashAttribute 这个数据只取一次
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.mall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            // addFlashAttribute 这个数据只取一次
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
    }


}
