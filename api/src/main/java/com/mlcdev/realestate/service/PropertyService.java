package com.mlcdev.realestate.service;

import com.mlcdev.realestate.dto.*;
import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.exception.NotFoundException;
import com.mlcdev.realestate.mapper.PropertyMapper;
import com.mlcdev.realestate.repository.PropertyRepository;
import com.mlcdev.realestate.repository.UserRepository;
import com.mlcdev.realestate.security.OwnershipValidator;
import com.mlcdev.realestate.specifications.PropertySpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PropertySummaryDTO> findAllAvailable(Pageable pageable, PropertyFilter filter) {
        log.debug("Retrieving available properties page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Property> properties = propertyRepository.findBy(
                PropertySpecs.isAvailable().and(buildDefaultSpec(filter)),
                q -> q.page(pageable)
        );
        return toSummaryPage(properties);
    }

    @Transactional(readOnly = true)
    public Page<PropertySummaryDTO> findAll(Pageable pageable, PropertyFilter filter) {
        log.debug("Retrieving properties page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Property> properties = propertyRepository.findBy(
                buildDefaultSpec(filter),
                q -> q.page(pageable)
        );
        return toSummaryPage(properties);
    }

    @Transactional(readOnly = true)
    public PropertyDetailDTO findById(UUID id, boolean canViewUnavailable) {
        log.debug("Retrieving property with ID: {}", id);
        Property property = propertyByIdOrElseThrow(id);
        if(!property.isAvailable() && !canViewUnavailable){
            throw new NotFoundException("Property with ID: " + id + " not found");
        }
        return PropertyMapper.entityToDetailDTO(property);
    }

    @Transactional
    public PropertyDetailDTO create(PropertyCreateDTO createDTO, UUID brokerId) {
        log.info("Creating property with title: {}", createDTO.getTitle());
        Property property = PropertyMapper.createDTOToEntity(createDTO);
        property.setBroker(userRepository.getReferenceById(brokerId));
        Property savedProperty = propertyRepository.saveAndFlush(property);
        log.info("Property successfully created with ID: {}", savedProperty.getId());
        return PropertyMapper.entityToDetailDTO(savedProperty);
    }

    @Transactional
    public PropertyDetailDTO update(UUID propertyId, PropertyPatchDTO dto, UUID brokerId, boolean isAdmin) {
        log.info("Patching property with id: {}", propertyId);
        Property property = propertyByIdOrElseThrow(propertyId);
        OwnershipValidator.propertyVerifyBrokerPermission(property, brokerId, isAdmin);
        Property updatedProperty = propertyRepository.saveAndFlush(PropertyMapper.applyPatchDTOToEntity(dto, property));
        log.info("Property with ID: {} successfully patched", propertyId);
        return PropertyMapper.entityToDetailDTO(updatedProperty);
    }

    @Transactional(readOnly = true)
    public Page<PropertySummaryDTO> findBrokerProperties(Pageable pageable, PropertyFilter filter,UUID brokerId) {
        log.debug("Retrieving properties from the broker with id: {}", brokerId);
        if (!userRepository.existsById(brokerId)) {
            log.warn("Broker with ID {} doesn't exist", brokerId);
            throw new NotFoundException("Broker with Id: " + brokerId + " doesn't exist");
        }
        Page<Property> brokerProperties = propertyRepository.findBy(
                buildDefaultSpec(filter).and(PropertySpecs.brokerIdEquals(brokerId)),
                q -> q.page(pageable));

        log.debug("{} properties retrieved from broker with ID: {}", brokerProperties.getNumberOfElements(), brokerId);
        return toSummaryPage(brokerProperties);
    }

    @Transactional
    public PropertyDetailDTO toggleAvailable(UUID propertyId, UUID brokerId, boolean isAdmin) {
        log.info("Toggling property with Id: {} availability", propertyId);
        Property property = propertyByIdOrElseThrow(propertyId);
        OwnershipValidator.propertyVerifyBrokerPermission(property, brokerId, isAdmin);
        property.setAvailable(!property.isAvailable());
        Property savedProperty = propertyRepository.save(property);
        log.info("Property with ID: {} availability changed to: {}", propertyId, savedProperty.isAvailable());
        return PropertyMapper.entityToDetailDTO(savedProperty);
    }

    @Transactional
    public void delete(UUID propertyId, UUID brokerId, boolean isAdmin) {
        log.info("Deleting property with id: {}", propertyId);
        Property property = propertyByIdOrElseThrow(propertyId);
        OwnershipValidator.propertyVerifyBrokerPermission(property, brokerId, isAdmin);
        imageService.deleteAllImagesForProperty(propertyId);
        propertyRepository.delete(property);
        log.info("Property with ID: {} successfully deleted", propertyId);
    }


    private PredicateSpecification<Property> buildDefaultSpec(PropertyFilter filter){
        return PropertySpecs.searchContains(filter.search())
                .and(PropertySpecs.minPrice(filter.minPrice()))
                .and(PropertySpecs.maxPrice(filter.maxPrice()))
                .and(PropertySpecs.minArea(filter.minArea()))
                .and(PropertySpecs.maxArea(filter.maxArea()))
                .and(PropertySpecs.transactionTypeEquals(filter.transactionType()))
                .and(PropertySpecs.categoryEquals(filter.category()))
                .and(PropertySpecs.minBedrooms(filter.minBedrooms()))
                .and(PropertySpecs.maxBedrooms(filter.maxBedrooms()))
                .and(PropertySpecs.minBathrooms(filter.minBathrooms()))
                .and(PropertySpecs.minSuites(filter.minSuites()))
                .and(PropertySpecs.minParkingSpots(filter.minParkingSpots()));
    }

    private Property propertyByIdOrElseThrow(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property with ID: " + id + " not found"));
    }

    private Map<UUID, ImageDTO> fetchPrimaryImages(Page<Property> properties) {
        List<UUID> propertyIds = properties.map(Property::getId).toList();
        return imageService.findPrimaryImagesForProperties(propertyIds);
    }

    private Page<PropertySummaryDTO> toSummaryPage(Page<Property> properties) {
        Map<UUID, ImageDTO> primaryImages = fetchPrimaryImages(properties);
        return properties.map(property ->
                PropertyMapper.entityToSummaryDTO(property, primaryImages.get(property.getId()))
        );
    }




}


