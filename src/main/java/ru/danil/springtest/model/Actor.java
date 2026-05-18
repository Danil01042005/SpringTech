package ru.danil.springtest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "actors", schema = "test")
public class Actor {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "actors_movies",
            joinColumns = @JoinColumn(name = "actor_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"),
            schema = "test"
    )
    private List<Movie> movies = new ArrayList<>();

    public void linkMovies(){
        this.movies.forEach(movie -> movie.getActors().add(this));
    }

    public void updateMovies(List<Movie> moviesOfActor) {
        this.movies.removeIf(thisMovie -> !moviesOfActor.contains(thisMovie));

        for (Movie movie : moviesOfActor) {
            if(!this.getMovies().contains(movie)){
                this.movies.add(movie);
            }
        }
    }
}
