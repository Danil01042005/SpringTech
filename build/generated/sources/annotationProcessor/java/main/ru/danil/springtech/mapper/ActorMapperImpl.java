package ru.danil.springtech.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.dto.MovieDTO;
import ru.danil.springtech.model.Actor;
import ru.danil.springtech.model.Movie;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-01T15:48:23+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class ActorMapperImpl implements ActorMapper {

    @Override
    public ActorDTO toActorDTO(Actor actor) {
        if ( actor == null ) {
            return null;
        }

        ActorDTO actorDTO = new ActorDTO();

        actorDTO.setName( actor.getName() );
        actorDTO.setAge( actor.getAge() );
        actorDTO.setMovies( movieListToMovieDTOList( actor.getMovies() ) );

        return actorDTO;
    }

    @Override
    public Actor toActor(ActorDTO actorDTO) {
        if ( actorDTO == null ) {
            return null;
        }

        Actor actor = new Actor();

        actor.setName( actorDTO.getName() );
        if ( actorDTO.getAge() != null ) {
            actor.setAge( actorDTO.getAge() );
        }
        actor.setMovies( movieDTOListToMovieList( actorDTO.getMovies() ) );

        return actor;
    }

    @Override
    public void updateActor(ActorDTO updateActorDto, Actor actor) {
        if ( updateActorDto == null ) {
            return;
        }

        actor.setName( updateActorDto.getName() );
        if ( updateActorDto.getAge() != null ) {
            actor.setAge( updateActorDto.getAge() );
        }
        if ( actor.getMovies() != null ) {
            List<Movie> list = movieDTOListToMovieList( updateActorDto.getMovies() );
            if ( list != null ) {
                actor.getMovies().clear();
                actor.getMovies().addAll( list );
            }
            else {
                actor.setMovies( null );
            }
        }
        else {
            List<Movie> list = movieDTOListToMovieList( updateActorDto.getMovies() );
            if ( list != null ) {
                actor.setMovies( list );
            }
        }
    }

    protected MovieDTO movieToMovieDTO(Movie movie) {
        if ( movie == null ) {
            return null;
        }

        MovieDTO movieDTO = new MovieDTO();

        movieDTO.setName( movie.getName() );

        return movieDTO;
    }

    protected List<MovieDTO> movieListToMovieDTOList(List<Movie> list) {
        if ( list == null ) {
            return null;
        }

        List<MovieDTO> list1 = new ArrayList<MovieDTO>( list.size() );
        for ( Movie movie : list ) {
            list1.add( movieToMovieDTO( movie ) );
        }

        return list1;
    }

    protected Movie movieDTOToMovie(MovieDTO movieDTO) {
        if ( movieDTO == null ) {
            return null;
        }

        Movie movie = new Movie();

        movie.setName( movieDTO.getName() );

        return movie;
    }

    protected List<Movie> movieDTOListToMovieList(List<MovieDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Movie> list1 = new ArrayList<Movie>( list.size() );
        for ( MovieDTO movieDTO : list ) {
            list1.add( movieDTOToMovie( movieDTO ) );
        }

        return list1;
    }
}
