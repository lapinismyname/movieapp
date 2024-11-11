package com.lapin.movieapp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    // request (incoming) is intercepted by this method before reaching the Controller
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        System.out.println("preHandle(): Before sending the request to the Controller");
        try {
            HandlerMethod handlerMethod = (HandlerMethod) handler; //if a request with a URI that does not match any of the routes in any Controller is made, this line explodes.
            System.out.println("    Method Type: " + request.getMethod());
            System.out.println("    Request URL: " + request.getRequestURI());
            System.out.println("    Controller: " + handlerMethod.getBeanType().getSimpleName());
            System.out.println("    Method: " + handlerMethod.getMethod().getName());
            System.out.println("    Parameters: " + this.formatParameters(request.getParameterMap()));
        }
        catch (Exception e) {
            log.error(e.getMessage());
            //if it returns false, this means the request handling process will be ended prematurely
            //which means there will be no postHandle and afterCompletion calls as well as the request will never reach to a Controller
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
        return true;
    }

    // response (obviously outgoing) is intercepted by this method before reaching the client
    // this method is only executed when preHandle() returns true and the handler method returns successfully
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle(): After the Controller handles the request (before returning a response back to the client)");
        try {
            System.out.println("    Status code: " + response.getStatus());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // this method is called after request & response cycle of the HTTP communication is completed
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable Exception ex) throws Exception {
            System.out.println("afterCompletion(): After the request and response cycle is completed" + '\n');
    }

    private String formatParameters(Map<String, String[]> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=[" + String.join(", ", entry.getValue()) + "]")
                .collect(Collectors.joining(", "));
    }
}
