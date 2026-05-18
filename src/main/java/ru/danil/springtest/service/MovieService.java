package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Movie;
import ru.danil.springtest.repository.MovieRepository;
import ru.danil.springtest.utill.UserExeption;

import java.util.*;

@Service
@AllArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Optional<Movie> getMovieById(UUID id) {
        return movieRepository.findById(id);
    }

    public List<Movie> findByIdIn(Set<UUID> ids) {
        return movieRepository.findByIdIn(ids);
    }

    @Transactional
    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }
}
