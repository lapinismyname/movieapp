package com.lapin.movieapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Users {
    @Id
    private Integer id;
    private String username;
    private String password;
}
