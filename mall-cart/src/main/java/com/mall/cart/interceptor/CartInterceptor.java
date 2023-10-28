package com.mall.cart.interceptor;

import com.mall.cart.vo.UserInfoTo;
import com.mall.common.constant.Constant;
import com.mall.common.vo.MemberRespVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @ClassName: CartInterceptor
 * @Description: 拦截器 在执行目标方法之前，判断用户登录状态，并封装传递给controller目标请求
 * @Author: L
 * @Create: 2023-10-28 13:32
 * @Version: 1.0
 */

//@Component不起作用，必须同时需要进行配置
public class CartInterceptor implements HandlerInterceptor {

    /**
     * ThreadLocal 同一个线程共享数据
     */
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前进行拦截
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(Constant.LOGIN_USER);
        if(member != null){
            //用户登录
            userInfoTo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null && cookies.length>0){
            for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if(name.equals(Constant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //如果没有临时用户一定分配一个临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后 将 userKey 通知浏览器并保存
     * 分配临时用户，让浏览器保存
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     * (can also be {@code null})
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        //如果没有临时用户一定保存一个临时用户
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(Constant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setMaxAge(Constant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
