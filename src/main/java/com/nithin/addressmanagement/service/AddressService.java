package com.nithin.addressmanagement.service;

import com.nithin.addressmanagement.dto.AddressRequestDto;
import com.nithin.addressmanagement.dto.AddressResponseDto;
import com.nithin.addressmanagement.entity.Address;
import com.nithin.addressmanagement.entity.AddressStatus;
import com.nithin.addressmanagement.entity.Customer;
import com.nithin.addressmanagement.exception.CustomerNotFoundException;
import com.nithin.addressmanagement.processor.AddressPreprocessor;
import com.nithin.addressmanagement.repository.AddressRepository;
import com.nithin.addressmanagement.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressPreprocessor addressPreprocessor;

    public AddressService(AddressRepository addressRepository,
                          CustomerRepository customerRepository,
                          AddressPreprocessor addressPreprocessor){
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.addressPreprocessor = addressPreprocessor;
    }

    public AddressResponseDto createAddress(
            Long customerId,
            AddressRequestDto requestDto
    ) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        String cleanedAddress = addressPreprocessor.preprocess(requestDto.getRawAddress());

        Address address = new Address();

        address.setRawAddress(requestDto.getRawAddress());

        // Intelligence will populate these fields later
        address.setHouseNumber(null);
        address.setBuildingName(null);
        address.setStreet(null);
        address.setLandmark(null);
        address.setArea(null);
        address.setCity(null);
        address.setState(null);
        address.setPostalCode(null);
        address.setCountry(null);

        // Initial state
        address.setConfidenceScore(0.0);
        address.setStatus(AddressStatus.PENDING);

        // connecting address to customer
        address.setCustomer(customer);

        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        Address savedAddress = addressRepository.save(address);

        return mapToResponseDto(savedAddress);
    }

    public List<AddressResponseDto> getAddressesByCustomerId(
            Long customerId
    ) {

        // Verify that the customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        return addressRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private AddressResponseDto mapToResponseDto(Address address) {

        return new AddressResponseDto(
                address.getId(),
                address.getRawAddress(),
                address.getHouseNumber(),
                address.getBuildingName(),
                address.getStreet(),
                address.getLandmark(),
                address.getArea(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getConfidenceScore(),
                address.getStatus(),
                address.getCustomer().getId(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

}
