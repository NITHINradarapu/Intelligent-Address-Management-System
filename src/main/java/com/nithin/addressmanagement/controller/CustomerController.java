package com.nithin.addressmanagement.controller;


import com.nithin.addressmanagement.dto.CustomerRequestDto;
import com.nithin.addressmanagement.dto.CustomerResponseDto;
import com.nithin.addressmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    // constructor for customer service
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    // Create customer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDto createCustomer(@Valid @RequestBody CustomerRequestDto requestDto){
        return customerService.createCustomer(requestDto);
    }

    // get customer
    @GetMapping
    public List<CustomerResponseDto> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    // get customer by Id
    @GetMapping("/{id}")
    public CustomerResponseDto getCustomerById(@PathVariable Long id){
        return customerService.getCustomerById(id);
    }

    // update customer
    @PutMapping("/{id}")
    public CustomerResponseDto updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequestDto requestDto){
        return customerService.updateCustomer(id, requestDto);
    }

    // delete customer
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id){
        customerService.deleteCustomer(id);
    }
}
