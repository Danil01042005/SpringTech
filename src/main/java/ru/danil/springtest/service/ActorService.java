package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Actor;
import ru.danil.springtest.model.Movie;
import ru.danil.springtest.repository.ActorRepository;
import ru.danil.springtest.utill.UserExeption;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ActorService {

    private final ActorRepository actorRepository;
    private final MovieService movieService;

    @Transactional
    public Actor createActor(Actor actor) {
        if(actor.getMovies() == null || actor.getMovies().isEmpty()) {
            return actorRepository.save(actor);
        } else {
            List<Movie> allMovie = new ArrayList<>();
            Set<UUID> ids = new HashSet<>();
            actor.getMovies().forEach(xMovies -> {
                if(xMovies.getId() != null) {
                    ids.add(xMovies.getId());
                } else {
                    allMovie.add(xMovies);
                }
            });
            if(!ids.isEmpty()) {
                List<Movie> moviesOfActor = movieService.findByIdIn(ids);
                if (moviesOfActor.size() != ids.size()){
                    throw  new UserExeption("Был передан несуществующий айди фильма", HttpStatus.BAD_REQUEST);
                }
                allMovie.addAll(moviesOfActor);
            }

            actor.setMovies(allMovie);
            actor.linkMovies();
        }
        return actorRepository.save(actor);
    }

    public Actor getActorById(UUID id) {
        return actorRepository.findByIdWithMovies(id).orElseThrow(() -> new UserExeption("Актер с таким айди не найде", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Actor updateActor(UUID id, Actor updateActor) {
        Actor actor = getActorById(id);
        actor.setName(updateActor.getName());
        actor.setAge(updateActor.getAge());

        Set<UUID> ids = new HashSet<>();
        if (updateActor.getMovies() != null) {
            updateActor.getMovies().forEach(xMovie -> {
                if(xMovie.getId() != null) {
                    ids.add(xMovie.getId());
                }
            });
        } else return actorRepository.save(actor);
        List<Movie> moviesOfActor = movieService.findByIdIn(ids);

        if (moviesOfActor.size() != ids.size()){
            throw  new UserExeption("Был передан несуществующий айди фильма", HttpStatus.BAD_REQUEST);
        }

        actor.updateMovies(moviesOfActor);

        updateActor.getMovies().stream().filter(newMovie -> newMovie.getId() == null).forEach(newMovie -> {
            newMovie.setActors(new ArrayList<>());
            newMovie.getActors().add(actor);
            actor.getMovies().add(newMovie);
        });

        return actorRepository.save(actor);
    }

    @Transactional
    public void deleteActorById(UUID id) {
        actorRepository.deleteById(id);
    }
}
