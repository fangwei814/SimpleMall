package com.fangw.simplemall.cart.interceptor;

import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fangw.common.constant.AuthServerConstant;
import com.fangw.common.constant.CartConstant;
import com.fangw.common.vo.MemberRespVo;
import com.fangw.simplemall.cart.vo.UserInfoTo;

/**
 * 拦截器 在执行目标方法之前判断用户登录状态，并封装传递controller目标请求 登录用户/临时用户
 */
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在拦截之前，主要用来包装用户
     * 
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        // 1.判断session中是否有用户信息
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (Objects.nonNull(member)) {
            // 用户已登录
            userInfoTo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (Objects.nonNull(cookies) && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // user-key
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true); // 标记携带user-key
                }
            }
        }

        // 2.分配临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        // 3.信息存到threadlocal
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 让浏览器保存临时用户
     * 
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();

        if (!userInfoTo.isTempUser()) {
            // 持续的延长临时用户的过期时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("simplemall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
