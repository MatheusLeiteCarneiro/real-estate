package com.mlcdev.realestate.service;

import com.mlcdev.realestate.dto.AddressDTO;
import com.mlcdev.realestate.dto.PropertyCreateDTO;
import com.mlcdev.realestate.dto.PropertyDetailDTO;
import com.mlcdev.realestate.dto.PropertyFilter;
import com.mlcdev.realestate.dto.PropertyPatchDTO;
import com.mlcdev.realestate.entities.Address;
import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import com.mlcdev.realestate.entities.User;
import com.mlcdev.realestate.exception.NotFoundException;
import com.mlcdev.realestate.repository.PropertyRepository;
import com.mlcdev.realestate.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PropertyService propertyService;

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Should create a property and associate a broker to it")
        void createShouldCreateAPropertyAndAssociateABrokerToIt() {
            PropertyCreateDTO createDTO = buildPropertyCreateDTO();

            UUID userGeneratedId = UUID.randomUUID();
            UUID propertyGeneratedId = UUID.randomUUID();

            User broker = User.builder().id(userGeneratedId).username("user").build();

            when(userRepository.getReferenceById(userGeneratedId)).thenReturn(broker);

            when(propertyRepository.saveAndFlush(ArgumentMatchers.any())).thenAnswer(invocation -> {
                Property propertyToSave = invocation.getArgument(0, Property.class);
                propertyToSave.setId(propertyGeneratedId);
                return propertyToSave;
            });

            PropertyDetailDTO createdPropertyDTO = propertyService.create(createDTO, userGeneratedId);

            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

            verify(userRepository).getReferenceById(userGeneratedId);
            verify(propertyRepository).saveAndFlush(propertyCaptor.capture());

            Property savedProperty = propertyCaptor.getValue();

            Assertions.assertNotNull(createdPropertyDTO);
            Assertions.assertEquals(propertyGeneratedId, createdPropertyDTO.getId());
            Assertions.assertEquals(createDTO.getTitle(), createdPropertyDTO.getTitle());
            Assertions.assertEquals(createDTO.getPrice(), createdPropertyDTO.getPrice());

            Assertions.assertNotNull(savedProperty);
            Assertions.assertEquals(broker, savedProperty.getBroker());
        }


        @Test
        @DisplayName("Should return a property when ID exists")
        void findByIdShouldReturnPropertyWhenIdExists() {
            UUID propertyGeneratedId = UUID.randomUUID();

            Property property = buildGenericProperty(propertyGeneratedId);

            when(propertyRepository.findById(propertyGeneratedId)).thenReturn(Optional.of(property));

            PropertyDetailDTO responseDTO = propertyService.findById(propertyGeneratedId, false);

            verify(propertyRepository).findById(propertyGeneratedId);

            Assertions.assertNotNull(responseDTO);
            Assertions.assertEquals(propertyGeneratedId, responseDTO.getId());
            Assertions.assertEquals(property.getTitle(), responseDTO.getTitle());
            Assertions.assertEquals(property.getPrice(), responseDTO.getPrice());
        }

        @Test
        @DisplayName("Should return unavailable property when user is authenticated")
        void findByIdShouldReturnUnavailablePropertyWhenUserIsAuthenticated() {
            UUID propertyId = UUID.randomUUID();
            Property property = buildGenericProperty(propertyId);
            property.setAvailable(false);

            when(propertyRepository.findById(propertyId))
                    .thenReturn(Optional.of(property));

            PropertyDetailDTO result =
                    propertyService.findById(propertyId, true);

            verify(propertyRepository).findById(propertyId);
            Assertions.assertFalse(result.isAvailable());
        }

        @Test
        @DisplayName("Should update a property when broker is owner")
        void updateShouldPatchPropertyWhenBrokerIsOwner() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();

            User broker = User.builder().id(brokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(broker);

            PropertyPatchDTO patchDTO = PropertyPatchDTO.builder().title("Updated Title").price(new BigDecimal("650000.00")).bedrooms(4).build();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            when(propertyRepository.saveAndFlush(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0, Property.class));

            PropertyDetailDTO responseDTO = propertyService.update(propertyId, patchDTO, brokerId, false);

            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository).saveAndFlush(propertyCaptor.capture());


            Property savedProperty = propertyCaptor.getValue();

            Assertions.assertNotNull(responseDTO);
            Assertions.assertEquals(propertyId, responseDTO.getId());
            Assertions.assertEquals(patchDTO.getTitle(), responseDTO.getTitle());
            Assertions.assertEquals(patchDTO.getPrice(), responseDTO.getPrice());
            Assertions.assertEquals(patchDTO.getBedrooms(), responseDTO.getBedrooms());
            Assertions.assertEquals(propertyId, savedProperty.getId());
            Assertions.assertEquals(patchDTO.getTitle(), savedProperty.getTitle());
            Assertions.assertEquals(patchDTO.getPrice(), savedProperty.getPrice());
            Assertions.assertEquals(patchDTO.getBedrooms(), savedProperty.getBedrooms());
            Assertions.assertEquals(broker, savedProperty.getBroker());
        }

        @Test
        @DisplayName("Should update a property when user is admin")
        void updateShouldPatchPropertyWhenUserIsAdmin() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);

            PropertyPatchDTO patchDTO = PropertyPatchDTO.builder().title("Updated Title").price(new BigDecimal("650000.00")).bedrooms(4).build();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            when(propertyRepository.saveAndFlush(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0, Property.class));

            PropertyDetailDTO responseDTO = propertyService.update(propertyId, patchDTO, adminId, true);

            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository).saveAndFlush(propertyCaptor.capture());


            Property savedProperty = propertyCaptor.getValue();

            Assertions.assertNotNull(responseDTO);
            Assertions.assertEquals(propertyId, responseDTO.getId());
            Assertions.assertEquals(patchDTO.getTitle(), responseDTO.getTitle());
            Assertions.assertEquals(patchDTO.getPrice(), responseDTO.getPrice());
            Assertions.assertEquals(patchDTO.getBedrooms(), responseDTO.getBedrooms());
            Assertions.assertEquals(propertyId, savedProperty.getId());
            Assertions.assertEquals(patchDTO.getTitle(), savedProperty.getTitle());
            Assertions.assertEquals(patchDTO.getPrice(), savedProperty.getPrice());
            Assertions.assertEquals(patchDTO.getBedrooms(), savedProperty.getBedrooms());
            Assertions.assertEquals(ownerBroker, savedProperty.getBroker());
        }

        @Test
        @DisplayName("Should change available from true to false")
        void toggleAvailableShouldChangeAvailableFromTrueToFalse() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);
            property.setAvailable(true);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            when(propertyRepository.save(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0, Property.class));

            PropertyDetailDTO responseDTO = propertyService.toggleAvailable(propertyId, ownerBrokerId, false);

            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository).save(propertyCaptor.capture());

            Property savedProperty = propertyCaptor.getValue();

            Assertions.assertNotNull(responseDTO);
            Assertions.assertEquals(propertyId, responseDTO.getId());
            Assertions.assertFalse(responseDTO.isAvailable());
            Assertions.assertFalse(savedProperty.isAvailable());
            Assertions.assertEquals(ownerBroker, savedProperty.getBroker());
        }

        @Test
        @DisplayName("Should change available from false to true")
        void toggleAvailableShouldChangeAvailableFromFalseToTrue() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);
            property.setAvailable(false);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            when(propertyRepository.save(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0, Property.class));

            PropertyDetailDTO responseDTO = propertyService.toggleAvailable(propertyId, ownerBrokerId, false);

            ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository).save(propertyCaptor.capture());

            Property savedProperty = propertyCaptor.getValue();

            Assertions.assertNotNull(responseDTO);
            Assertions.assertEquals(propertyId, responseDTO.getId());
            Assertions.assertTrue(responseDTO.isAvailable());
            Assertions.assertTrue(savedProperty.isAvailable());
            Assertions.assertEquals(ownerBroker, savedProperty.getBroker());
        }


        @Test
        @DisplayName("Should delete property and all its images when broker is owner")
        void deleteShouldDeletePropertyAndImagesWhenBrokerIsOwner() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();

            User broker = User.builder().id(brokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(broker);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            propertyService.delete(propertyId, brokerId, false);

            verify(propertyRepository).findById(propertyId);
            verify(imageService).deleteAllImagesForProperty(propertyId);
            verify(propertyRepository).delete(property);
        }

    }

    @Nested
    @DisplayName("Error path")
    class ErrorPath {

        @Test
        @DisplayName("Should throw NotFoundException when property ID does not exist")
        void findByIdShouldThrowNotFoundExceptionWhenPropertyIdDoesNotExist() {
            UUID propertyId = UUID.randomUUID();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.findById(propertyId, false)
            );

            verify(propertyRepository).findById(propertyId);

            Assertions.assertEquals("Property with ID: " + propertyId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFoundException when property is unavailable and user is not authenticated")
        void findByIdShouldThrowNotFoundExceptionWhenPropertyUnavailableAndUserNotAuthenticated() {
            UUID propertyGeneratedId = UUID.randomUUID();

            Property property = buildGenericProperty(propertyGeneratedId);

            property.setAvailable(false);

            when(propertyRepository.findById(propertyGeneratedId)).thenReturn(Optional.of(property));

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.findById(propertyGeneratedId, false)
            );

            verify(propertyRepository).findById(propertyGeneratedId);

            Assertions.assertEquals("Property with ID: " + propertyGeneratedId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFoundException when updating a property that does not exist")
        void updateShouldThrowNotFoundExceptionWhenPropertyDoesNotExist() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            PropertyPatchDTO patchDTO = PropertyPatchDTO.builder().title("Updated Title").build();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.update(propertyId, patchDTO, brokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository, never()).saveAndFlush(ArgumentMatchers.any());

            Assertions.assertEquals("Property with ID: " + propertyId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when broker is not property owner on update")
        void updateShouldThrowAccessDeniedExceptionWhenBrokerIsNotOwner() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();
            UUID anotherBrokerId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);

            PropertyPatchDTO patchDTO = PropertyPatchDTO.builder().title("Updated Title").build();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            AccessDeniedException exception = Assertions.assertThrows(
                    AccessDeniedException.class,
                    () -> propertyService.update(propertyId, patchDTO, anotherBrokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository, never()).saveAndFlush(ArgumentMatchers.any());

            Assertions.assertEquals("User doesn't have the permission to modify the property", exception.getMessage());
            Assertions.assertEquals("Test Property", property.getTitle());
        }

        @Test
        @DisplayName("Should throw NotFoundException when broker does not exist")
        void findBrokerPropertiesShouldThrowNotFoundExceptionWhenBrokerDoesNotExist() {
            UUID brokerId = UUID.randomUUID();
            PropertyFilter filter = buildEmptyPropertyFilter();
            PageRequest pageable = PageRequest.of(0, 10);

            when(userRepository.existsById(brokerId)).thenReturn(false);

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.findBrokerProperties(pageable, filter, brokerId)
            );

            verify(userRepository).existsById(brokerId);
            verify(propertyRepository, never()).findBy(
                    ArgumentMatchers.<PredicateSpecification<Property>>any(),
                    ArgumentMatchers.any()
            );

            Assertions.assertEquals("Broker with Id: " + brokerId + " doesn't exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFoundException when toggling availability for property that does not exist")
        void toggleAvailableShouldThrowNotFoundExceptionWhenPropertyDoesNotExist() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.toggleAvailable(propertyId, brokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository, never()).save(ArgumentMatchers.any());

            Assertions.assertEquals("Property with ID: " + propertyId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when broker is not property owner on toggle availability")
        void toggleAvailableShouldThrowAccessDeniedExceptionWhenBrokerIsNotOwner() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();
            UUID anotherBrokerId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);
            property.setAvailable(true);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            AccessDeniedException exception = Assertions.assertThrows(
                    AccessDeniedException.class,
                    () -> propertyService.toggleAvailable(propertyId, anotherBrokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository, never()).save(ArgumentMatchers.any());

            Assertions.assertEquals("User doesn't have the permission to modify the property", exception.getMessage());
            Assertions.assertTrue(property.isAvailable());
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting a property that does not exist")
        void deleteShouldThrowNotFoundExceptionWhenPropertyDoesNotExist() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            NotFoundException exception = Assertions.assertThrows(
                    NotFoundException.class,
                    () -> propertyService.delete(propertyId, brokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verifyNoInteractions(imageService);
            verify(propertyRepository, never()).delete(ArgumentMatchers.any(Property.class));

            Assertions.assertEquals("Property with ID: " + propertyId + " not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when broker is not property owner on delete")
        void deleteShouldThrowAccessDeniedExceptionWhenBrokerIsNotOwner() {
            UUID propertyId = UUID.randomUUID();
            UUID ownerBrokerId = UUID.randomUUID();
            UUID anotherBrokerId = UUID.randomUUID();

            User ownerBroker = User.builder().id(ownerBrokerId).username("broker").build();

            Property property = buildGenericProperty(propertyId);
            property.setBroker(ownerBroker);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            AccessDeniedException exception = Assertions.assertThrows(
                    AccessDeniedException.class,
                    () -> propertyService.delete(propertyId, anotherBrokerId, false)
            );

            verify(propertyRepository).findById(propertyId);
            verifyNoInteractions(imageService);
            verify(propertyRepository, never()).delete(ArgumentMatchers.any(Property.class));

            Assertions.assertEquals("User doesn't have the permission to modify the property", exception.getMessage());
        }
    }

    private Property buildGenericProperty(UUID id) {
        return Property.builder().id(id).title("Test Property").description("A nice test property").price(new BigDecimal("500000.00")).transactionType(TransactionType.SALE).category(PropertyCategory.HOUSE).bedrooms(3).bathrooms(2).suites(1).area(new BigDecimal("120.50")).parkingSpots(2).address(buildGenericAddress()).build();
    }

    private Address buildGenericAddress() {
        return Address.builder().street("Test Street").number("100").neighborhood("Central District").city("Sample City").state("CA").zipCode("90000000").build();
    }

    private PropertyCreateDTO buildPropertyCreateDTO() {
        return PropertyCreateDTO.builder().title("Test Property").description("A nice test property").price(new BigDecimal("500000.00")).transactionType(TransactionType.SALE).category(PropertyCategory.HOUSE).bedrooms(3).bathrooms(2).suites(1).area(new BigDecimal("120.50")).parkingSpots(2).address(buildAddressDTO()).build();
    }

    private AddressDTO buildAddressDTO() {
        return AddressDTO.builder().street("Test Street").number("100").neighborhood("Central District").city("Sample City").state("CA").zipCode("90000000").build();
    }

    private PropertyFilter buildEmptyPropertyFilter() {
        return new PropertyFilter(null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
