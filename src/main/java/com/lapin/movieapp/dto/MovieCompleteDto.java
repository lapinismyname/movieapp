package com.lapin.movieapp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MovieCompleteDto extends MovieDto { // hides id field of a movie
    private Integer year;
    private Integer duration;
    private String director;
}
