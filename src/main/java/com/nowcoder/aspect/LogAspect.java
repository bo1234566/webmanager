package com.nowcoder.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by nowcoder on 2016/6/26.
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* com.nowcoder.controller.*Controller.*(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : joinPoint.getArgs()) {
            sb.append("arg:" + arg.toString() + "|");
        }
        logger.info("before method controller: " + sb.toString());
    }

    @Before("execution(* com.nowcoder.service.*Service.*(..))")
    public void beforeMethodService(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        logger.info("before method service: " + joinPoint);
    }
    @After("execution(* com.nowcoder.controller.IndexController.*(..))")
    public void afterMethod(JoinPoint joinPoint) {
        logger.info("after method: ");
    }
}
