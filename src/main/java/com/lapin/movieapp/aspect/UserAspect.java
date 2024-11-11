package com.lapin.movieapp.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lapin.movieapp.entity.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Order(1)
@Component
@Slf4j
public class UserAspect {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before("execution(* com.lapin.movieapp.controller.MovieController.*(..))")
    public void setUserBefore() throws JsonProcessingException {
        setUser();
    }

    @After("execution(* com.lapin.movieapp.controller.UserController.*(..))")
    public void setUserAfter() throws JsonProcessingException {
        if (setUser()) {
            log.info("User logged in");
            MDC.remove("User");
        }
        else log.info("Cannot log user in");
    }

    private boolean setUser() throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                //store the user in MDC context
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", userPrincipal.getUsername());
                String json = objectMapper.writeValueAsString(userMap);
                MDC.put("User", json);
                return true;
            }
        }
        return false;
    }
}
