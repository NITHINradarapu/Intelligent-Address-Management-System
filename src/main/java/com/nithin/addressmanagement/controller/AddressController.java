package com.nithin.addressmanagement.controller;

import com.nithin.addressmanagement.dto.AddressRequestDto;
import com.nithin.addressmanagement.dto.AddressResponseDto;
import com.nithin.addressmanagement.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    // create address for a customer
    @PostMapping("/customers/{customerId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponseDto createAddress(@PathVariable Long customerId, @Valid @RequestBody AddressRequestDto requestDto){
        return addressService.createAddress(customerId, requestDto);
    }

    // get all addresses of a customer
    // nested resource API design.
    @GetMapping("/customers/{customerId}/addresses")
    public List<AddressResponseDto> getAddressesByCustomerId(@PathVariable Long customerId){
        return addressService.getAddressesByCustomerId(customerId);
    }
}
