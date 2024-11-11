package com.lapin.movieapp.repository;

import com.lapin.movieapp.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByDirector(String director);
    Movie findByTitle(String title);
}