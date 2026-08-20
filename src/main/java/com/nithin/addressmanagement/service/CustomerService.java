package com.nithin.addressmanagement.service;

import com.nithin.addressmanagement.dto.CustomerRequestDto;
import com.nithin.addressmanagement.dto.CustomerResponseDto;
import com.nithin.addressmanagement.entity.Customer;
import com.nithin.addressmanagement.exception.CustomerNotFoundException;
import com.nithin.addressmanagement.exception.DuplicateEmailException;
import com.nithin.addressmanagement.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // create customer
    public CustomerResponseDto createCustomer(CustomerRequestDto requestDto) {

        if (customerRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateEmailException("Customer with this email already exists");
        }

        Customer customer = new Customer();
        customer.setName(requestDto.getName());
        customer.setEmail(requestDto.getEmail());
        customer.setPhoneNumber(requestDto.getPhoneNumber());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponseDto(savedCustomer);
    }

    // get all customers
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::mapToResponseDto).toList();
    }

    // get customer by Id
    public CustomerResponseDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found with id " + id));

        return mapToResponseDto(customer);
    }




    // update customer
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto requestDto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found with id: " + id)
                );

        if (!customer.getEmail().equals(requestDto.getEmail())
                && customerRepository.existsByEmail(requestDto.getEmail())) {

            throw new DuplicateEmailException("Customer with this email already exists");
        }

        customer.setName(requestDto.getName());
        customer.setEmail(requestDto.getEmail());
        customer.setPhoneNumber(requestDto.getPhoneNumber());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);

        return mapToResponseDto(updatedCustomer);
    }


    // delete customer
    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found with id: " + id)
                );

        customerRepository.delete(customer);
    }

    // mapping to response DTO
    private CustomerResponseDto mapToResponseDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
