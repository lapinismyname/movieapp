package com.lapin.movieapp.controller;

import com.lapin.movieapp.entity.Users;
import com.lapin.movieapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<Users> register(@RequestBody Users user) {
        return userService.register(user);
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody Users user) {
        return userService.login(user);
    }
}
