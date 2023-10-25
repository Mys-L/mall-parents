package com.mall.auth.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.mall.auth.feign.MemberFeignService;
import com.mall.auth.vo.SocialUser;
import com.mall.common.constant.Constant;
import com.mall.common.utils.HttpUtils;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: Oath2Controller
 * @Description:
 * @Author: L
 * @Create: 2023-10-23 17:45
 * @Version: 1.0
 */

@Slf4j
@Controller
@RequestMapping("/oauth2.0")
public class Oath2Controller {
    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/logout")
    public String login(HttpSession session){
        if(session.getAttribute(Constant.LOGIN_USER) != null){
            log.info("\n[" + ((MemberRespVo)session.getAttribute(Constant.LOGIN_USER)).getUsername() + "] 已下线");
        }
        session.invalidate();
        return "redirect:http://auth.mall.com/login.html";
    }

    /**
     * 登录成功回调
     * {
     *     "access_token": "2.00b5w4HGbwxc6B0e3d62c666DlN1DD",
     *     "remind_in": "157679999",
     *     "expires_in": 157679999,
     *     "uid": "5605937365",
     *     "isRealName": "true"
     * }
     *
     */
    @GetMapping("/weibo/success") // Oath2Controller
    public String weiBo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        // 根据code换取 Access Token
        Map<String,String> header = new HashMap<>();
        Map<String,String> query = new HashMap<>();
        Map<String,String> map = new HashMap<>();
        map.put("client_id","2636917288");
        map.put("client_secret","6a263e9284c6c1a74a62eadacc11b6e2");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.mall.com/oauth2.0/weibo/success");
        map.put("code",code);
        //1、根据code换取accessToken；
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com",
                "/oauth2/access_token", "post", header, query, map);
        //2、处理
        if(response.getStatusLine().getStatusCode() == 200){
            // 获取响应体： Access Token
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            // 相当于我们知道了当前是那个用户
            // 1.如果用户是第一次进来 自动注册进来(为当前社交用户生成一个会员信息 以后这个账户就会关联这个账号)
            R login = memberFeignService.oauthLogin(socialUser);
            if(login.getCode() == 0){
                MemberRespVo respVo = login.getData("data" ,new TypeReference<MemberRespVo>() {});
                log.info("\n欢迎 [" + respVo.getUsername() + "] 使用社交账号登录");
                // 第一次使用session 命令浏览器保存这个用户信息 JESSIONSEID 每次只要访问这个网站就会带上这个cookie
                // 在发卡的时候扩大session作用域 (指定域名为父域名)
                // TODO 1.默认发的当前域的session (需要解决子域session共享问题)
				/*
				session.setAttribute("logUser",respVo);
				 Cookie cookie = new Cookie("JSESSIONID","123");
				 cookie.setDomain("mall.com");
				servletResponse.addCookie(cookie);
				*/
                // TODO 2.使用JSON序列化后保存到redis  自动完成
                session.setAttribute(Constant.LOGIN_USER, respVo);
                // 登录成功 跳回首页
                return "redirect:http://mall.com";
            }else{
                return "redirect:http://auth.mall.com/login.html";
            }
        }else{
            return "redirect:http://auth.mall.com/login.html";
        }
    }
}
