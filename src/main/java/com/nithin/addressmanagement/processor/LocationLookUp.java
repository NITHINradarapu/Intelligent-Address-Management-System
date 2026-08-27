package com.nithin.addressmanagement.processor;

public interface LocationLookUp {
    String findCity(String address);
    String findArea(String address);
}
