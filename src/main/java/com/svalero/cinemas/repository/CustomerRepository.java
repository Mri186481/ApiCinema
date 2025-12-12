package com.svalero.cinemas.repository;


import com.svalero.cinemas.domain.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    // Método para obtener todos los usuarios
    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    // Método para buscar un usuario por su nopmbre
    List<Customer> findByName(String name);

    List<Customer> findByMail(String mail);

    List<Customer> findByAddress(String address);

    List<Customer> findByNameAndAddressAndMail(String name, String address, String mail);

    List<Customer> findByNameAndAddress(String name, String address);

    List<Customer> findByNameAndMail(String name, String mail);

    List<Customer> findByAddressAndMail(String address, String mail);




    @Query("select u FROM Customer u WHERE u.admitsAdvertising = :admitsAdvertising")
    List<Customer> findAllUsersByAdmitsAdvertising(Boolean admitsAdvertising);

}

