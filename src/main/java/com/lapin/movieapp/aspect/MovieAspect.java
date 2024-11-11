package com.lapin.movieapp.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lapin.movieapp.dto.MovieCompleteDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Component
@Slf4j
public class MovieAspect {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterReturning(pointcut="execution(* com.lapin.movieapp.controller.MovieController.getCachedMovies(..))", returning="movie")
    public void setMovieAfter(ResponseEntity<MovieCompleteDto> movie) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(movie.getBody());
        MDC.put("Movie", json);
        log.info("Return movie");
        MDC.remove("Movie");
    }
}
