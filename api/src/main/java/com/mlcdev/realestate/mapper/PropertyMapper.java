package com.mlcdev.realestate.mapper;

import com.mlcdev.realestate.dto.*;
import com.mlcdev.realestate.entities.Property;


public class PropertyMapper {

    private PropertyMapper() {
    }

    public static PropertyDetailDTO entityToDetailDTO(Property entity) {

        return PropertyDetailDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .transactionType(entity.getTransactionType())
                .category(entity.getCategory())
                .suites(entity.getSuites())
                .bedrooms(entity.getBedrooms())
                .bathrooms(entity.getBathrooms())
                .area(entity.getArea())
                .parkingSpots(entity.getParkingSpots())
                .address(AddressMapper.entityToDTO(entity.getAddress()))
                .images(entity.getImages().stream().map(ImageMapper::entityToDTO).toList())
                .createdAt(entity.getCreatedAt())
                .available(entity.isAvailable())
                .build();


    }

    public static PropertySummaryDTO entityToSummaryDTO(Property entity, ImageDTO primaryImageDTO) {

        return PropertySummaryDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .transactionType(entity.getTransactionType())
                .category(entity.getCategory())
                .suites(entity.getSuites())
                .bedrooms(entity.getBedrooms())
                .bathrooms(entity.getBathrooms())
                .area(entity.getArea())
                .parkingSpots(entity.getParkingSpots())
                .neighborhood(entity.getAddress().getNeighborhood())
                .city(entity.getAddress().getCity())
                .state(entity.getAddress().getState())
                .primaryImage(primaryImageDTO)
                .available(entity.isAvailable())
                .build();


    }

    public static Property createDTOToEntity(PropertyCreateDTO dto) {
        return Property.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .transactionType(dto.getTransactionType())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .suites(dto.getSuites())
                .bedrooms(dto.getBedrooms())
                .bathrooms(dto.getBathrooms())
                .area(dto.getArea())
                .parkingSpots(dto.getParkingSpots())
                .address(AddressMapper.dtoToEntity(dto.getAddress()))
                .build();

    }


    public static Property applyPatchDTOToEntity(PropertyPatchDTO dto, Property entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        if(dto.getCategory() != null){
            entity.setCategory(dto.getCategory());
        }
        if(dto.getTransactionType() != null){
            entity.setTransactionType(dto.getTransactionType());
        }
        if (dto.getSuites() != null) {
            entity.setSuites(dto.getSuites());
        }
        if (dto.getBedrooms() != null) {
            entity.setBedrooms(dto.getBedrooms());
        }
        if (dto.getBathrooms() != null) {
            entity.setBathrooms(dto.getBathrooms());
        }
        if (dto.getArea() != null) {
            entity.setArea(dto.getArea());
        }
        if (dto.getParkingSpots() != null) {
            entity.setParkingSpots(dto.getParkingSpots());
        }
        if (dto.getAddress() != null) {
            entity.setAddress(AddressMapper.patchDtoToEntity(dto.getAddress(), entity.getAddress()));
        }
        return entity;
    }

}
