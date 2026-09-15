package com.mlcdev.realestate.mapper;

import com.mlcdev.realestate.dto.AddressDTO;
import com.mlcdev.realestate.dto.AddressPatchDTO;
import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.dto.PropertyCreateDTO;
import com.mlcdev.realestate.dto.PropertyDetailDTO;
import com.mlcdev.realestate.dto.PropertyPatchDTO;
import com.mlcdev.realestate.dto.PropertySummaryDTO;
import com.mlcdev.realestate.entities.Address;
import com.mlcdev.realestate.entities.Image;
import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

class PropertyMapperTest {

    @Test
    @DisplayName("Should map create DTO to entity")
    void createDTOToEntityShouldMapFieldsAndAddress() {
        PropertyCreateDTO dto = buildPropertyCreateDTO();

        Property entity = PropertyMapper.createDTOToEntity(dto);

        Assertions.assertEquals(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getDescription(), entity.getDescription());
        Assertions.assertEquals(dto.getPrice(), entity.getPrice());
        Assertions.assertEquals(dto.getTransactionType(), entity.getTransactionType());
        Assertions.assertEquals(dto.getCategory(), entity.getCategory());
        Assertions.assertEquals(dto.getSuites(), entity.getSuites());
        Assertions.assertEquals(dto.getBedrooms(), entity.getBedrooms());
        Assertions.assertEquals(dto.getBathrooms(), entity.getBathrooms());
        Assertions.assertEquals(dto.getArea(), entity.getArea());
        Assertions.assertEquals(dto.getParkingSpots(), entity.getParkingSpots());
        Assertions.assertEquals(dto.getAddress().getStreet(), entity.getAddress().getStreet());
        Assertions.assertEquals(dto.getAddress().getZipCode(), entity.getAddress().getZipCode());
    }

    @Test
    @DisplayName("Should map entity to detail DTO")
    void entityToDetailDTOShouldMapFieldsAddressAndImages() {
        UUID propertyId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");

        Property property = buildProperty(propertyId);
        property.setCreatedAt(createdAt);
        property.setAvailable(false);

        Image image = Image.builder()
                .id(imageId)
                .fileIdentifier("properties/sample")
                .url("https://example.com/sample.jpg")
                .isPrimary(true)
                .property(property)
                .build();
        property.getImages().add(image);

        PropertyDetailDTO dto = PropertyMapper.entityToDetailDTO(property);

        Assertions.assertEquals(propertyId, dto.getId());
        Assertions.assertEquals(property.getTitle(), dto.getTitle());
        Assertions.assertEquals(property.getAddress().getCity(), dto.getAddress().getCity());
        Assertions.assertEquals(createdAt, dto.getCreatedAt());
        Assertions.assertFalse(dto.isAvailable());
        Assertions.assertEquals(1, dto.getImages().size());
        Assertions.assertEquals(imageId, dto.getImages().getFirst().getId());
        Assertions.assertTrue(dto.getImages().getFirst().getIsPrimary());
    }

    @Test
    @DisplayName("Should map entity to summary DTO")
    void entityToSummaryDTOShouldMapPrimaryImageAndLocation() {
        Property property = buildProperty(UUID.randomUUID());
        ImageDTO primaryImage = ImageDTO.builder()
                .id(UUID.randomUUID())
                .fileIdentifier("properties/primary")
                .url("https://example.com/primary.jpg")
                .isPrimary(true)
                .build();

        PropertySummaryDTO dto = PropertyMapper.entityToSummaryDTO(property, primaryImage);

        Assertions.assertEquals(property.getId(), dto.getId());
        Assertions.assertEquals(property.getTitle(), dto.getTitle());
        Assertions.assertEquals(property.getAddress().getNeighborhood(), dto.getNeighborhood());
        Assertions.assertEquals(property.getAddress().getCity(), dto.getCity());
        Assertions.assertEquals(property.getAddress().getState(), dto.getState());
        Assertions.assertEquals(primaryImage, dto.getPrimaryImage());
        Assertions.assertTrue(dto.isAvailable());
    }

    @Test
    @DisplayName("Should apply patch DTO and keep unpatched fields unchanged")
    void applyPatchDTOToEntityShouldUpdateOnlyProvidedFields() {
        Property property = buildProperty(UUID.randomUUID());
        Address originalAddress = property.getAddress();

        PropertyPatchDTO patchDTO = PropertyPatchDTO.builder()
                .title("Updated Property")
                .price(new BigDecimal("650000.00"))
                .bedrooms(4)
                .address(AddressPatchDTO.builder()
                        .city("Updated City")
                        .zipCode("91000000")
                        .build())
                .build();

        Property patchedProperty = PropertyMapper.applyPatchDTOToEntity(patchDTO, property);

        Assertions.assertSame(property, patchedProperty);
        Assertions.assertSame(originalAddress, patchedProperty.getAddress());
        Assertions.assertEquals("Updated Property", patchedProperty.getTitle());
        Assertions.assertEquals("Original description", patchedProperty.getDescription());
        Assertions.assertEquals(new BigDecimal("650000.00"), patchedProperty.getPrice());
        Assertions.assertEquals(TransactionType.SALE, patchedProperty.getTransactionType());
        Assertions.assertEquals(PropertyCategory.HOUSE, patchedProperty.getCategory());
        Assertions.assertEquals(4, patchedProperty.getBedrooms());
        Assertions.assertEquals(2, patchedProperty.getBathrooms());
        Assertions.assertEquals("Updated City", patchedProperty.getAddress().getCity());
        Assertions.assertEquals("91000000", patchedProperty.getAddress().getZipCode());
        Assertions.assertEquals("Test Street", patchedProperty.getAddress().getStreet());
    }

    private Property buildProperty(UUID id) {
        return Property.builder()
                .id(id)
                .title("Original Property")
                .description("Original description")
                .price(new BigDecimal("500000.00"))
                .transactionType(TransactionType.SALE)
                .category(PropertyCategory.HOUSE)
                .suites(1)
                .bedrooms(3)
                .bathrooms(2)
                .area(new BigDecimal("120.50"))
                .parkingSpots(2)
                .address(buildAddress())
                .build();
    }

    private PropertyCreateDTO buildPropertyCreateDTO() {
        return PropertyCreateDTO.builder()
                .title("Created Property")
                .description("Created property description")
                .price(new BigDecimal("450000.00"))
                .transactionType(TransactionType.RENT)
                .category(PropertyCategory.APARTMENT)
                .suites(1)
                .bedrooms(2)
                .bathrooms(2)
                .area(new BigDecimal("80.00"))
                .parkingSpots(1)
                .address(buildAddressDTO())
                .build();
    }

    private Address buildAddress() {
        return Address.builder()
                .street("Test Street")
                .number("100")
                .complement("Apt 10")
                .neighborhood("Central District")
                .city("Sample City")
                .state("CA")
                .zipCode("90000000")
                .build();
    }

    private AddressDTO buildAddressDTO() {
        return AddressDTO.builder()
                .street("Create Street")
                .number("200")
                .complement("House")
                .neighborhood("Garden District")
                .city("Create City")
                .state("NY")
                .zipCode("10000000")
                .build();
    }
}
