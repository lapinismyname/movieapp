package com.lapin.movieapp.controller;

import com.lapin.movieapp.dto.MovieCompleteDto;
import com.lapin.movieapp.dto.MovieCountDto;
import com.lapin.movieapp.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieCompleteDto>> getMovies() {
        return movieService.getMovies();
    }

    @GetMapping("director/{director}")
    public ResponseEntity<List<MovieCompleteDto>> getMoviesByDirector(@PathVariable String director) {
        return movieService.getMoviesByDirector(director);
    }

    @GetMapping("earlier-than")
    public ResponseEntity<List<String>> getMoviesEarlierThan(@RequestParam int year) {
        return movieService.getMoviesEarlierThan(year);
    }

    @GetMapping("number-of-title")
    public ResponseEntity<Integer> getMoviesEarlierThan(@RequestParam String title) {
        return movieService.getNumberOfTitle(title);
    }

    @GetMapping("more-than-one")
    public ResponseEntity<List<MovieCountDto>> getMoreThanOne() {
        return movieService.getMoreThanOne();
    }

    @GetMapping("distinct")
    public ResponseEntity<Set<String>> getDistinct() {
        return movieService.getDistinct();
    }

    @PostMapping("add")
    public ResponseEntity<String> addMovie(@RequestBody MovieCompleteDto movieDto) {
        return movieService.addMovie(movieDto);
    }

    @PutMapping("update-by-id")
    public ResponseEntity<String> updateMovie(@RequestParam int id, @RequestBody MovieCompleteDto movieDto) {
        return movieService.updateMovie(id, movieDto);
    }

    @PutMapping("update-by-title")
    public ResponseEntity<String> updateMovie(@RequestParam String title, @RequestBody MovieCompleteDto movieDto) {
        return movieService.updateMovie(title, movieDto);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMovie(@RequestParam int id) {
        return movieService.deleteMovie(id);
    }

    @GetMapping("plot")
        public ResponseEntity<String> getPlot(@RequestParam String title) {
            return movieService.getPlot(title);
    }

    @GetMapping("id/{id}")
    public ResponseEntity<MovieCompleteDto> getCachedMovies(@PathVariable int id) {
        return movieService.getMovieById(id);
    }
}
