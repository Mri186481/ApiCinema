package com.svalero.cinemas.repository;



import com.svalero.cinemas.domain.Room;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Long> {

    List<Room> findAll();

    Optional<Room> findById(Long id);

    Room findByRoomName(String name);

    List<Room> findByRoom3d(boolean room3d);

    List<Room> findByRoomAtmos(boolean roomAtmos);

    List<Room> findByRoomLaser(boolean roomlaser);

    List<Room> findByRoom3dAndRoomAtmosAndRoomLaser( boolean room3d, boolean roomAtmos, boolean roomLaser);

    List<Room> findByRoom3dAndRoomAtmos(boolean room3d,boolean roomAtmos);

    List<Room> findByRoom3dAndRoomLaser(boolean room3d, boolean roomLaser);

    List<Room> findByRoomAtmosAndRoomLaser(boolean roomAtmos, boolean roomLaser);

    @Query("select r FROM Room r WHERE r.openingDate = :openingDate")
    List<Room> findAllRoomsByOpeningDate(LocalDate openingdate);
}

