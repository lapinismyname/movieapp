package com.lapin.movieapp.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception e) {

        System.err.println(e.getMessage());
        //instanceof is introduced in the java version Java 1.0?
        if (e instanceof NullPointerException) {
            return new ResponseEntity<>("Segmentation fault\n" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (e instanceof RuntimeException) {
            return new ResponseEntity<>("An error occurred while running the program\n" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else {
            return new ResponseEntity<>("An error occurred\n" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
