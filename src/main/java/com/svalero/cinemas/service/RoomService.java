package com.svalero.cinemas.service;



import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.dto.RoomInDto;
import com.svalero.cinemas.domain.dto.RoomOutDto;
import com.svalero.cinemas.exception.CustomerNotFoundException;
import com.svalero.cinemas.exception.RoomNotFoundException;
import com.svalero.cinemas.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos los usuarios
    public List<RoomOutDto> getAll(Boolean room3d, Boolean roomAtmos, Boolean roomLaser) {
        List<Room> roomList;
//
        boolean hasRoom3d = room3d != null;
        boolean hasRoomAtmos = roomAtmos != null;
        boolean hasRoomLaser = roomLaser != null;

        if (hasRoom3d && hasRoomAtmos && hasRoomLaser) {
            roomList = roomRepository.findByRoom3dAndRoomAtmosAndRoomLaser(room3d, roomAtmos, roomLaser);
        } else if (hasRoom3d && hasRoomAtmos) {
            roomList = roomRepository.findByRoom3dAndRoomAtmos(room3d,roomAtmos);
        } else if (hasRoom3d && hasRoomLaser) {
            roomList = roomRepository.findByRoom3dAndRoomLaser(room3d,roomLaser);
        } else if (hasRoomAtmos && hasRoomLaser) {
            roomList = roomRepository.findByRoomAtmosAndRoomLaser(roomAtmos, roomLaser);
        } else if (hasRoom3d) {
            roomList = roomRepository.findByRoom3d(room3d);
        } else if (hasRoomAtmos) {
            roomList = roomRepository.findByRoomAtmos(roomAtmos);
        } else if (hasRoomLaser) {
            roomList = roomRepository.findByRoomLaser(roomLaser);
        } else {
            roomList = roomRepository.findAll();
        }


        return modelMapper.map(roomList, new TypeToken<List<RoomOutDto>>() {}.getType());
    }
    public Room get(long id) throws RoomNotFoundException {
        return roomRepository.findById(id)
                .orElseThrow(RoomNotFoundException::new);
    }
    //
//    public List<CustomerOutDto> getAllAd(boolean admitsAdvertising) {
//        List<Customer> customers = customerRepository.findAllUsersByAdmitsAdvertising(admitsAdvertising);
//        return modelMapper.map(customers, new TypeToken<List<CustomerOutDto>>() {}.getType());
//    }
    // Dar de alta un usuario
    public RoomOutDto add(RoomInDto roomInDto) {
        Room room = modelMapper.map(roomInDto, Room.class);
        room = roomRepository.save(room);
        return modelMapper.map(room, RoomOutDto.class);
    }
    // Modificar un usuario
    public RoomOutDto modify(long roomId, RoomInDto roomInDto) throws RoomNotFoundException {
        Room room = roomRepository.findById(roomId).orElseThrow(CustomerNotFoundException::new);
        modelMapper.map(roomInDto, room);
        roomRepository.save(room);
        return modelMapper.map(room, RoomOutDto.class);
    }

    public void delete(long roomId) throws RoomNotFoundException {
        Room room = roomRepository.findById(roomId).orElseThrow(RoomNotFoundException::new);
        roomRepository.delete(room);
    }




}
