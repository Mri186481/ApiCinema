package com.svalero.cinemas.repository;

import com.svalero.cinemas.domain.Screening;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreeningRepository extends CrudRepository<Screening, Long> {

    List<Screening> findAll();

    // Método para buscar un usuario por su nopmbre
    List<Screening> findByMovieId(Long movieId);

    List<Screening> findBySubtitled(boolean subtitled);

    List<Screening> findByRoomId(Long roomId);

    List<Screening> findByMovieIdAndSubtitledAndRoomId(Long movieId, boolean subtitled, Long roomId);

    List<Screening> findByMovieIdAndSubtitled(Long movieId, boolean subtitled);

    List<Screening> findByMovieIdAndRoomId(Long movieId, Long roomId);

    List<Screening> findBySubtitledAndRoomId(boolean subtitled, Long roomId);

}
