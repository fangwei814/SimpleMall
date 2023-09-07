package com.fangw.simplemall.seckill.interceptor;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fangw.common.constant.AuthServerConstant;
import com.fangw.common.vo.MemberRespVo;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    /**
     * 用户登录拦截器，没有登录就跳转到登录页面
     * 
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", uri);
        if (match) {
            MemberRespVo attribute = (MemberRespVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (Objects.nonNull(attribute)) {
                // 登录放行
                loginUser.set(attribute);
                return true;
            } else {
                // 未登录要求登录
                request.getSession().setAttribute("msg", "请先登录");
                response.sendRedirect("http://auth.simplemall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
