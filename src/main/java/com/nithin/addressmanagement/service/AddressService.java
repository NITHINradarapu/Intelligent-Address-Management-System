package com.nithin.addressmanagement.service;

import com.nithin.addressmanagement.dto.AddressRequestDto;
import com.nithin.addressmanagement.dto.AddressResponseDto;
import com.nithin.addressmanagement.entity.Address;
import com.nithin.addressmanagement.entity.AddressStatus;
import com.nithin.addressmanagement.entity.Customer;
import com.nithin.addressmanagement.exception.CustomerNotFoundException;
import com.nithin.addressmanagement.exception.DuplicateAddressException;
import com.nithin.addressmanagement.processor.*;
import com.nithin.addressmanagement.repository.AddressRepository;
import com.nithin.addressmanagement.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressPreprocessor addressPreprocessor;
    private final AddressParser addressParser;
    private final ConfidenceCalculator confidenceCalculator;
    private final AddressValidator addressValidator;
    private final AddressDecisionEngine addressDecisionEngine;
    private final AddressNormalizer addressNormalizer;
    private final DuplicateAddressDetector duplicateAddressDetector;

    public AddressService(AddressRepository addressRepository,
                          CustomerRepository customerRepository,
                          AddressPreprocessor addressPreprocessor,
                          AddressParser addressParser,
                          ConfidenceCalculator confidenceCalculator,
                          AddressValidator addressValidator,
                          AddressDecisionEngine addressDecisionEngine,
                          AddressNormalizer addressNormalizer,
                          DuplicateAddressDetector duplicateAddressDetector){
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.addressPreprocessor = addressPreprocessor;
        this.addressParser = addressParser;
        this.confidenceCalculator = confidenceCalculator;
        this.addressValidator = addressValidator;
        this.addressDecisionEngine = addressDecisionEngine;
        this.addressNormalizer = addressNormalizer;
        this.duplicateAddressDetector = duplicateAddressDetector;
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

        ParsedAddress parsedAddress = addressParser.parse(cleanedAddress);

        ValidationResult validationResult = addressValidator.validate(parsedAddress);

        double confidenceScore =
                confidenceCalculator.calculate(parsedAddress);

        DecisionResult decisionResult =
                addressDecisionEngine.decide(
                        validationResult,
                        confidenceScore
                );

        String formattedAddress = addressNormalizer.normalize(parsedAddress);

        List<Address> existingAddresses = addressRepository.findByCustomerId(customerId);
        Optional<Address> duplicate = duplicateAddressDetector.findDuplicate(
                existingAddresses,
                parsedAddress,
                formattedAddress
        );

        if (duplicate.isPresent()) {
            throw new DuplicateAddressException(
                    "Address already exists for this customer with id: " + duplicate.get().getId()
            );
        }

        Address address = new Address();

        address.setRawAddress(requestDto.getRawAddress());
        address.setFormattedAddress(formattedAddress);

        // Intelligence will populate these fields later
        address.setHouseNumber(parsedAddress.getHouseNumber());
        address.setBuildingName(parsedAddress.getBuildingName());
        address.setStreet(parsedAddress.getStreet());
        address.setLandmark(parsedAddress.getLandmark());
        address.setArea(parsedAddress.getArea());
        address.setCity(parsedAddress.getCity());
        address.setState(parsedAddress.getState());
        address.setPostalCode(parsedAddress.getPostalCode());
        address.setCountry(parsedAddress.getCountry());

        // Initial state
//        address.setConfidenceScore(0.0);
//        address.setStatus(AddressStatus.PENDING);

        address.setConfidenceScore(confidenceScore);

        address.setStatus(decisionResult.getStatus());

        address.setProcessingMessage(
                decisionResult.getMessage()
        );

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
                address.getFormattedAddress(),
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
                address.getProcessingMessage(),
                address.getStatus(),
                address.getCustomer().getId(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

}
