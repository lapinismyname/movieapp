package com.lapin.movieapp.aspect;

import org.aspectj.lang.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect { //checks and controls in high level, no business logic!
    @Before("execution(* com.lapin.movieapp.controller.*.*(..))")
    //The @Before annotation is Advice
    //The expression "execution(* com.lapin.movieapp.controller.*.*(..))" is a Pointcut
    public void logBefore() {
        System.out.println("Method execution started...");
    }

    @After("execution(* com.lapin.movieapp.controller.*.*(..))")
    public void logAfter() {
        System.out.println("Method execution finished.");
    }

    @AfterReturning("execution(* com.lapin.movieapp.controller.*.*(..))")
    public void logAfterReturning() {
        System.out.println("    Status: Successful");
    }

    @AfterThrowing("execution(* com.lapin.movieapp.controller.*.*(..))")
    public void logAfterThrowing() {
        System.out.println("    Status: Aborted");
    }

    @Around("execution(* com.lapin.movieapp.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("Around method: " + proceedingJoinPoint.getSignature());
        System.out.println("Before calling joint point controller method");

        Object obj = proceedingJoinPoint.proceed();

        System.out.println("After calling joint point controller method");

        return obj;
    }

    /* Observe that the flow of the program is as following:
     * JWTFilter
     * if JWTFilter authenticates:
     * LogInterceptor (preHandle())
     * LogAspect (with @Around advice, before proceeding)
     * LogAspect (with @Before advice)
     * MovieController
     * MovieService
     * LogAspect (with @After advice)
     * LogAspect (with @Around advice, after proceeding)
     * LogInterceptor (postHandle(), afterCompletion())
     * CustomErrorHandler (handles any uncaught Exceptions at the end)
     */
}