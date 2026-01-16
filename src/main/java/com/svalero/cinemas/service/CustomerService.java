package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.Customer;
import com.svalero.cinemas.domain.dto.CustomerInDto;
import com.svalero.cinemas.domain.dto.CustomerOutDto;
import com.svalero.cinemas.exception.CustomerNotFoundException;
import com.svalero.cinemas.repository.CustomerRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos los usuarios
    public List<CustomerOutDto> getAll(String name, String address, String mail) {
        List<Customer> customerList;
//
        boolean hasName = !name.isEmpty();
        boolean hasAddress = !address.isEmpty();
        boolean hasMail = !mail.isEmpty();

        if (hasName && hasAddress && hasMail) {
            customerList = customerRepository.findByNameAndAddressAndMail(name, address, mail);
        } else if (hasName && hasAddress) {
            customerList = customerRepository.findByNameAndAddress(name,address);
        } else if (hasName && hasMail) {
            customerList = customerRepository.findByNameAndMail(name,mail);
        } else if (hasAddress && hasMail) {
            customerList = customerRepository.findByAddressAndMail(address, mail);
        } else if (hasName) {
            customerList = customerRepository.findByName(name);
        } else if (hasAddress) {
            customerList = customerRepository.findByAddress(address);
        } else if (hasMail) {
            customerList = customerRepository.findByMail(mail);
        } else {
            customerList = customerRepository.findAll();
        }

        return modelMapper.map(customerList, new TypeToken<List<CustomerOutDto>>() {}.getType());
    }

    // Por Id
    public Customer get(long id) throws CustomerNotFoundException {
        return customerRepository.findById(id)
                .orElseThrow(CustomerNotFoundException::new);
    }

    //Conseguir lista de clientes que admiten publicidad
    public List<CustomerOutDto> getAllAd(boolean admitsAdvertising) {
        List<Customer> customers = customerRepository.findAllUsersByAdmitsAdvertising(admitsAdvertising);
        return modelMapper.map(customers, new TypeToken<List<CustomerOutDto>>() {}.getType());
    }

    // Dar de alta un usuario
    public CustomerOutDto add(CustomerInDto customerInDto) {
        Customer customer = modelMapper.map(customerInDto, Customer.class);
        customer = customerRepository.save(customer);
        return modelMapper.map(customer, CustomerOutDto.class);
    }

    // Modificar un usuario
    public CustomerOutDto modify(long customerId, CustomerInDto customerInDto) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        modelMapper.map(customerInDto, customer);
        customerRepository.save(customer);
        return modelMapper.map(customer, CustomerOutDto.class);
    }

    // Modificacion parcial de un usuario
    public CustomerOutDto modifyPartial(Long customerId, Map<String, Object> updates) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        modelMapper.map(updates, customer);
        customerRepository.save(customer);
        return modelMapper.map(customer, CustomerOutDto.class);
    }

    public void delete(long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
        customerRepository.delete(customer);
    }

}