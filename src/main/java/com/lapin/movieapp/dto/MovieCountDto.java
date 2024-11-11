package com.lapin.movieapp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MovieCountDto extends MovieDto {
    private int count;

    public MovieCountDto(String title, int count) {
        this.setTitle(title);
        this.setCount(count);
    }
}
