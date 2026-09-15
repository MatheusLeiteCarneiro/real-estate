package com.mlcdev.realestate.mapper;

import com.mlcdev.realestate.dto.AddressDTO;
import com.mlcdev.realestate.dto.AddressPatchDTO;
import com.mlcdev.realestate.entities.Address;

import java.util.Locale;

public class AddressMapper {

    private AddressMapper() {
    }


    public static AddressDTO entityToDTO(Address address) {
        return AddressDTO.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .build();
    }

    public static Address dtoToEntity(AddressDTO dto) {
        return Address.builder()
                .street(dto.getStreet())
                .number(dto.getNumber())
                .complement(dto.getComplement())
                .neighborhood(dto.getNeighborhood())
                .city(dto.getCity())
                .state(normalizeState(dto.getState()))
                .zipCode(cleanZipCode(dto.getZipCode()))
                .build();
    }

    public static Address patchDtoToEntity(AddressPatchDTO dto, Address entity){
        if (dto.getStreet() != null && !dto.getStreet().isBlank()) {
            entity.setStreet(dto.getStreet());
        }
        if (dto.getNumber() != null && !dto.getNumber().isBlank()) {
            entity.setNumber(dto.getNumber());
        }
        if (dto.getComplement() != null) {
            entity.setComplement(dto.getComplement());
        }
        if (dto.getNeighborhood() != null && !dto.getNeighborhood().isBlank()) {
            entity.setNeighborhood(dto.getNeighborhood());
        }
        if (dto.getCity() != null && !dto.getCity().isBlank()) {
            entity.setCity(dto.getCity());
        }
        if (dto.getState() != null && !dto.getState().isBlank()) {
            entity.setState(normalizeState(dto.getState()));
        }
        if (dto.getZipCode() != null && !dto.getZipCode().isBlank()) {
            entity.setZipCode(cleanZipCode(dto.getZipCode()));
        }
        return entity;
    }

    private static String cleanZipCode(String zipCode) {
        return zipCode.replace("-", "");
    }

    private static String normalizeState(String state){
        return state.toUpperCase(Locale.ROOT);
    }
}
