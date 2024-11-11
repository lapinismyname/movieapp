package com.lapin.movieapp.service;

import com.lapin.movieapp.dto.MovieCompleteDto;
import com.lapin.movieapp.dto.MovieCountDto;
import com.lapin.movieapp.entity.Movie;
import com.lapin.movieapp.repository.MovieRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    public static int TIMEOUT = 300;

    // constructor injection, rather than field injection
    //@Autowired -no need if there is only one constructor
    public MovieService(MovieRepository movieRepository, RedisTemplate<String, Object> redisTemplate) {
        this.movieRepository = movieRepository;
        this.redisTemplate = redisTemplate;

        this.restTemplate =  new RestTemplate();
        this.modelMapper = new ModelMapper();
    }

    @PostConstruct
    public void cacheMovies() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            redisTemplate.opsForValue().set("id: " + movie.getId(), movie, TIMEOUT, TimeUnit.SECONDS);
        }
    }

    public ResponseEntity<List<MovieCompleteDto>> getMovies() {
        try {
            List<MovieCompleteDto> movieDtoList = movieRepository.findAll().stream()
                    .map(movie -> modelMapper.map(movie, MovieCompleteDto.class))
                    .toList();
            log.info("Return all movies");
            return new ResponseEntity<>(movieDtoList, HttpStatus.OK); //HttpStatusCode.valueOf(200)
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<List<MovieCompleteDto>> getMoviesByDirector(String director) {
        try {
            List<MovieCompleteDto> movieDtoList = movieRepository.findByDirector(director).stream()
                    .map(movie -> modelMapper.map(movie, MovieCompleteDto.class))
                    .toList();

            return new ResponseEntity<>(movieDtoList, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<String>> getMoviesEarlierThan(int year) {
        try {
            List<String> movieTitleList = movieRepository.findAll().stream()
                    .filter(movie -> movie.getYear() < year)
                    .map(Movie::getTitle) // equivalent to .map(movie -> movie.getTitle())
                    .toList();

            return new ResponseEntity<>(movieTitleList, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Integer> getNumberOfTitle(String title) {
        try {
            int count = (int) movieRepository.findAll().stream()
                    .filter(movie -> movie.getTitle().equalsIgnoreCase(title))
                    .count();

            return new ResponseEntity<>(count, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<MovieCountDto>> getMoreThanOne() {
        try {
            List<MovieCountDto> countMovies = movieRepository.findAll().stream()
                    .collect(Collectors.groupingBy(Movie::getTitle, Collectors.counting()))
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(entry -> new MovieCountDto(entry.getKey(), entry.getValue().intValue()))
                    .toList();

            return new ResponseEntity<>(countMovies, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Set<String>> getDistinct() {
        try {

            long start;
            long end;
            long[] times = new long[3];

            List<Movie> movies = movieRepository.findAll();

            // the time stamps are something like: [150, 64, 62],
            // but the speed differences between the second and the third one oscillates a lot

            start = System.currentTimeMillis();
            Set<String> titles1 = movies.stream()
                    .collect(Collectors.groupingBy(Movie::getTitle))
                    .keySet();
            end = System.currentTimeMillis();
            times[0] = end - start;

            //distinct():
            start = System.currentTimeMillis();
            List<String> titles2 = movies.stream()
                    .map(Movie::getTitle)
                    .distinct()
                    .toList();
            end = System.currentTimeMillis();
            times[1] = end - start;

            //Set<>:
            start = System.currentTimeMillis();
            Set<String> titles3 = movies.stream()
                    .map(Movie::getTitle)
                    .collect(Collectors.toSet());
            end = System.currentTimeMillis();
            times[2] = end - start;

            return new ResponseEntity<>(titles1, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> addMovie(MovieCompleteDto movieDto) {
        try {
            Movie movie = modelMapper.map(movieDto, Movie.class);
            movieRepository.save(movie);

            String key = "id: " + movie.getId();
            redisTemplate.opsForValue().set(key, movie, TIMEOUT, TimeUnit.SECONDS);
            return new ResponseEntity<>("Movie added", HttpStatus.CREATED);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>("Cannot add new movie", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> updateMovie(int id, MovieCompleteDto updatedMovieDto) {
        Movie existingMovie = movieRepository.findById(id).orElse(null);

        if (existingMovie != null) {
            existingMovie.setTitle(updatedMovieDto.getTitle());
            existingMovie.setYear(updatedMovieDto.getYear());
            existingMovie.setDuration(updatedMovieDto.getDuration());
            existingMovie.setDirector(updatedMovieDto.getDirector());

            movieRepository.save(existingMovie);
            redisTemplate.opsForValue().set("id: " + existingMovie.getId(), existingMovie, TIMEOUT, TimeUnit.SECONDS);
            return new ResponseEntity<>("Movie updated successfully", HttpStatus.OK);
        }
        //else return new ResponseEntity<>("Movie not found", HttpStatus.NOT_FOUND);
        else throw new RuntimeException("Cannot update the movie: movie not found");

        //return new ResponseEntity<>("Cannot update the movie", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> updateMovie(String title, MovieCompleteDto updatedMovieDto) {
        try {
            Movie existingMovie = movieRepository.findByTitle(title);

            if (existingMovie != null) {
                existingMovie.setTitle(updatedMovieDto.getTitle());
                existingMovie.setYear(updatedMovieDto.getYear());
                existingMovie.setDuration(updatedMovieDto.getDuration());
                existingMovie.setDirector(updatedMovieDto.getDirector());

                movieRepository.save(existingMovie);
                redisTemplate.opsForValue().set("id: " + existingMovie.getId(), existingMovie, TIMEOUT, TimeUnit.SECONDS);
                return new ResponseEntity<>("Movie updated successfully", HttpStatus.OK);
            }
            else return new ResponseEntity<>("Movie not found", HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>("Cannot update the movie", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> deleteMovie(int id) {
        try {
            if (movieRepository.existsById(id)) {
                movieRepository.deleteById(id);

                String key = "id: " + id;
                redisTemplate.delete(key);

                return new ResponseEntity<>("Movie removed", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Movie not found" , HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Cannot remove the movie", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> getPlot(String title) {
        try {
            String url = "http://www.omdbapi.com/?";
            String key = "apikey=603fa5f8";
            return restTemplate.getForEntity(url + key + "&t=" + title, String.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<MovieCompleteDto> getMovieById(int id) {
        try {
            Movie movie = (Movie) redisTemplate.opsForValue().get("id: " + id);
            if (movie == null) movie = movieRepository.findById(id).orElse(null);

            /*
            performance measure:

            long start;
            long end;
            long[] times = new long[2];
            Movie movie;

            start = System.currentTimeMillis();
            movie = (Movie) redisTemplate.opsForValue().get("id: " + id);
            end = System.currentTimeMillis();
            times[0] = end - start;

            start = System.currentTimeMillis();
            movie = movieRepository.findById(id).orElse(null);
            end = System.currentTimeMillis();
            times[1] = end - start;

            //resulting times is like: [3, 12]
            */
            MovieCompleteDto movieCompleteDto = modelMapper.map(movie, MovieCompleteDto.class);
            return new ResponseEntity<>(movieCompleteDto, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
