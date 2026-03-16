package com.yrris.coddy.aop;

import com.yrris.coddy.annotation.AuthCheck;
import com.yrris.coddy.constant.UserSessionConstant;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP aspect that enforces auth checks on methods annotated with {@link AuthCheck}.
 * Validates login status and optional role requirement via session attributes.
 */
@Aspect
@Component
public class AuthCheckAspect {

    @Around("@annotation(authCheck)")
    public Object doCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        Object rawUserId = session.getAttribute(UserSessionConstant.LOGIN_USER_ID);
        if (!(rawUserId instanceof Long)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        String mustRole = authCheck.mustRole();
        if (StringUtils.hasText(mustRole)) {
            Object rawRole = session.getAttribute(UserSessionConstant.LOGIN_USER_ROLE);
            String currentRole = rawRole instanceof String ? (String) rawRole : "";
            if (!mustRole.equals(currentRole)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Permission denied: " + mustRole + " role required");
            }
        }

        return joinPoint.proceed();
    }
}
