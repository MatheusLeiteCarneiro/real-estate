package com.mlcdev.realestate.service;

import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.entities.Image;
import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.entities.User;
import com.mlcdev.realestate.exception.BusinessRuleException;
import com.mlcdev.realestate.exception.EmptyResourceException;
import com.mlcdev.realestate.exception.FileStorageException;
import com.mlcdev.realestate.exception.NotFoundException;
import com.mlcdev.realestate.exception.ResourceMismatchException;
import com.mlcdev.realestate.repository.ImageRepository;
import com.mlcdev.realestate.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final String IMAGE_FOLDER = "test-property-images";

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "propertyImageFolder", IMAGE_FOLDER);
    }

    @Nested
    @DisplayName("Save images")
    class SaveImagesTests {

        @Test
        @DisplayName("Should reject an empty file list")
        void saveImagesShouldThrowEmptyResourceExceptionWhenFileListIsEmpty() {
            UUID propertyId = UUID.randomUUID();
            List<MultipartFile> files = List.of();
            UUID brokerId = UUID.randomUUID();

            assertThatThrownBy(() -> imageService.saveImages(propertyId, files, brokerId, false))
                    .isInstanceOf(EmptyResourceException.class)
                    .hasMessage("The file list is empty");

            verifyNoInteractions(propertyRepository, imageRepository, fileStorageService);
        }

        @Test
        @DisplayName("Should reject non-image files")
        void saveImagesShouldThrowBusinessRuleExceptionWhenFileIsNotAnImage() {
            UUID propertyId = UUID.randomUUID();
            List<MultipartFile> files = List.of(new MockMultipartFile(
                    "files",
                    "document.pdf",
                    "application/pdf",
                    "fake pdf content".getBytes()
            ));
            UUID brokerId = UUID.randomUUID();

            assertThatThrownBy(() -> imageService.saveImages(propertyId, files, brokerId, false))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Only image files are allowed");

            verifyNoInteractions(propertyRepository, imageRepository, fileStorageService);
        }

        @Test
        @DisplayName("Should reject files without content type")
        void saveImagesShouldThrowBusinessRuleExceptionWhenContentTypeIsNull() {
            UUID propertyId = UUID.randomUUID();
            List<MultipartFile> files = List.of(new MockMultipartFile(
                    "files",
                    "unknown-file",
                    null,
                    new byte[]{1, 2, 3}
            ));
            UUID brokerId = UUID.randomUUID();

            assertThatThrownBy(() -> imageService.saveImages(propertyId, files, brokerId, false))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Only image files are allowed");

            verifyNoInteractions(propertyRepository, imageRepository, fileStorageService);
        }

        @Test
        @DisplayName("Should set the first image as primary")
        void saveImagesShouldSetFirstImageAsPrimaryWhenPropertyHasNoPrimaryImage() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            List<MultipartFile> files = List.of(imageFile("first.jpg", 1), imageFile("second.jpg", 2));

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId)).thenReturn(Optional.empty());
            when(fileStorageService.uploadFile(any(byte[].class), eq(IMAGE_FOLDER)))
                    .thenReturn(uploadResult("first"))
                    .thenReturn(uploadResult("second"));
            when(imageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            List<ImageDTO> result = imageService.saveImages(propertyId, files, brokerId, false);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Image>> imagesCaptor = ArgumentCaptor.forClass(List.class);
            verify(imageRepository).saveAll(imagesCaptor.capture());

            List<Image> savedImages = imagesCaptor.getValue();

            assertThat(savedImages).hasSize(2);
            assertThat(savedImages).extracting(Image::isPrimary).containsExactly(true, false);
            assertThat(savedImages).extracting(Image::getProperty).containsOnly(property);
            assertThat(result).extracting(ImageDTO::getIsPrimary).containsExactly(true, false);
        }

        @Test
        @DisplayName("Should keep new images non-primary")
        void saveImagesShouldKeepNewImagesNonPrimaryWhenPropertyAlreadyHasPrimaryImage() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            Image currentPrimary = Image.builder().id(UUID.randomUUID()).property(property).isPrimary(true).build();
            List<MultipartFile> files = List.of(imageFile("first.jpg", 1), imageFile("second.jpg", 2));

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId)).thenReturn(Optional.of(currentPrimary));
            when(fileStorageService.uploadFile(any(byte[].class), eq(IMAGE_FOLDER)))
                    .thenReturn(uploadResult("first"))
                    .thenReturn(uploadResult("second"));
            when(imageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            List<ImageDTO> result = imageService.saveImages(propertyId, files, brokerId, false);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Image>> imagesCaptor = ArgumentCaptor.forClass(List.class);
            verify(imageRepository).saveAll(imagesCaptor.capture());

            assertThat(imagesCaptor.getValue()).extracting(Image::isPrimary).containsOnly(false);
            assertThat(result).extracting(ImageDTO::getIsPrimary).containsOnly(false);
            assertThat(currentPrimary.isPrimary()).isTrue();
        }

        @Test
        @DisplayName("Should clean up files after an upload failure")
        void saveImagesShouldDeletePreviouslyUploadedFilesWhenLaterUploadFails() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            List<MultipartFile> files = List.of(imageFile("first.jpg", 1), imageFile("second.jpg", 2));

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId)).thenReturn(Optional.empty());
            when(fileStorageService.uploadFile(any(byte[].class), eq(IMAGE_FOLDER)))
                    .thenReturn(uploadResult("first"))
                    .thenThrow(new IllegalStateException("Upload failed"));

            assertThatThrownBy(() -> imageService.saveImages(propertyId, files, brokerId, false))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Error occurred on the saving of the files")
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(fileStorageService).deleteFile("first-id");
            verify(imageRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should clean up files after a persistence failure")
        void saveImagesShouldDeleteUploadedFilesWhenRepositorySaveFails() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            List<MultipartFile> files = List.of(imageFile("first.jpg", 1), imageFile("second.jpg", 2));

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId)).thenReturn(Optional.empty());
            when(fileStorageService.uploadFile(any(byte[].class), eq(IMAGE_FOLDER)))
                    .thenReturn(uploadResult("first"))
                    .thenReturn(uploadResult("second"));
            when(imageRepository.saveAll(anyList())).thenThrow(new IllegalStateException("Persistence failed"));

            assertThatThrownBy(() -> imageService.saveImages(propertyId, files, brokerId, false))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Error occurred on the saving of the files")
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(fileStorageService).deleteFile("first-id");
            verify(fileStorageService).deleteFile("second-id");
        }
    }

    @Nested
    @DisplayName("Update primary image")
    class UpdatePrimaryImageTests {

        @Test
        @DisplayName("Should reject an image from another property")
        void updateImageAsPrimaryShouldThrowResourceMismatchExceptionWhenImageBelongsToAnotherProperty() {
            UUID propertyId = UUID.randomUUID();
            UUID otherPropertyId = UUID.randomUUID();
            UUID imageId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Image image = Image.builder()
                    .id(imageId)
                    .property(buildProperty(otherPropertyId, brokerId))
                    .build();

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

            assertThatThrownBy(() -> imageService.updateImageAsPrimary(propertyId, imageId, brokerId, false))
                    .isInstanceOf(ResourceMismatchException.class)
                    .hasMessage("Image with ID: " + imageId + " it's not from the property with id: " + propertyId);

            verify(imageRepository, never()).save(any(Image.class));
            verify(imageRepository, never()).saveAndFlush(any(Image.class));
        }

        @Test
        @DisplayName("Should replace the primary image")
        void updateImageAsPrimaryShouldDemoteCurrentPrimaryAndPromoteSelectedImage() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            Image currentPrimary = Image.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .isPrimary(true)
                    .build();
            Image selectedImage = Image.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .isPrimary(false)
                    .build();

            when(imageRepository.findById(selectedImage.getId())).thenReturn(Optional.of(selectedImage));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId)).thenReturn(Optional.of(currentPrimary));

            ImageDTO result = imageService.updateImageAsPrimary(
                    propertyId,
                    selectedImage.getId(),
                    brokerId,
                    false
            );

            verify(imageRepository).saveAndFlush(currentPrimary);
            verify(imageRepository).save(selectedImage);

            assertThat(currentPrimary.isPrimary()).isFalse();
            assertThat(selectedImage.isPrimary()).isTrue();
            assertThat(result.getId()).isEqualTo(selectedImage.getId());
            assertThat(result.getIsPrimary()).isTrue();
        }
    }

    @Nested
    @DisplayName("Delete image")
    class DeleteImageTests {

        @Test
        @DisplayName("Should reject deletion for another property")
        void deleteImageShouldThrowResourceMismatchExceptionWhenImageBelongsToAnotherProperty() {
            UUID propertyId = UUID.randomUUID();
            UUID otherPropertyId = UUID.randomUUID();
            UUID imageId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Image image = Image.builder()
                    .id(imageId)
                    .property(buildProperty(otherPropertyId, brokerId))
                    .build();

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

            assertThatThrownBy(() -> imageService.deleteImage(propertyId, imageId, brokerId, false))
                    .isInstanceOf(ResourceMismatchException.class)
                    .hasMessage("Image with ID: " + imageId + " it's not from the property with id: " + propertyId);

            verify(imageRepository, never()).delete(any(Image.class));
            verifyNoInteractions(fileStorageService);
        }

        @Test
        @DisplayName("Should select a new primary image after deletion")
        void deleteImageShouldPromoteAnotherImageWhenDeletingPrimaryImage() {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            Property property = buildProperty(propertyId, brokerId);
            Image primaryImage = Image.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .fileIdentifier("primary-id")
                    .isPrimary(true)
                    .build();
            Image remainingImage = Image.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .isPrimary(false)
                    .build();

            when(imageRepository.findById(primaryImage.getId())).thenReturn(Optional.of(primaryImage));
            when(imageRepository.findAllByPropertyIdAndIsPrimaryFalse(propertyId))
                    .thenReturn(List.of(remainingImage));

            imageService.deleteImage(propertyId, primaryImage.getId(), brokerId, false);

            verify(imageRepository).delete(primaryImage);
            verify(fileStorageService).deleteFile("primary-id");
            assertThat(remainingImage.isPrimary()).isTrue();
        }
    }

    @Nested
    @DisplayName("Find images")
    class FindImagesTests {

        @Test
        @DisplayName("Should enforce visibility when listing images")
        void findAllImagesShouldRespectUnavailablePropertyVisibility() {
            UUID availablePropertyId = UUID.randomUUID();
            UUID unavailablePropertyId = UUID.randomUUID();
            Image regularImage = Image.builder().id(UUID.randomUUID()).isPrimary(false).build();
            Image primaryImage = Image.builder().id(UUID.randomUUID()).isPrimary(true).build();

            when(propertyRepository.isAvailableById(availablePropertyId)).thenReturn(Optional.of(true));
            when(propertyRepository.isAvailableById(unavailablePropertyId)).thenReturn(Optional.of(false));
            when(imageRepository.findAllByPropertyId(availablePropertyId))
                    .thenReturn(List.of(regularImage, primaryImage));
            when(imageRepository.findAllByPropertyId(unavailablePropertyId))
                    .thenReturn(List.of(primaryImage));

            List<ImageDTO> publicResult = imageService.findAllImages(availablePropertyId, false);

            assertThatThrownBy(() -> imageService.findAllImages(unavailablePropertyId, false))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Property with ID: " + unavailablePropertyId + " not found");

            List<ImageDTO> authenticatedResult = imageService.findAllImages(unavailablePropertyId, true);

            assertThat(publicResult).extracting(ImageDTO::getId)
                    .containsExactly(primaryImage.getId(), regularImage.getId());
            assertThat(authenticatedResult).extracting(ImageDTO::getId)
                    .containsExactly(primaryImage.getId());
            verify(imageRepository).findAllByPropertyId(unavailablePropertyId);
        }

        @Test
        @DisplayName("Should enforce visibility for the primary image")
        void findPrimaryImageShouldRespectUnavailablePropertyVisibility() {
            UUID availablePropertyId = UUID.randomUUID();
            UUID unavailablePropertyId = UUID.randomUUID();
            Image availablePrimary = Image.builder().id(UUID.randomUUID()).isPrimary(true).build();
            Image unavailablePrimary = Image.builder().id(UUID.randomUUID()).isPrimary(true).build();

            when(propertyRepository.isAvailableById(availablePropertyId)).thenReturn(Optional.of(true));
            when(propertyRepository.isAvailableById(unavailablePropertyId)).thenReturn(Optional.of(false));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(availablePropertyId))
                    .thenReturn(Optional.of(availablePrimary));
            when(imageRepository.findByPropertyIdAndIsPrimaryTrue(unavailablePropertyId))
                    .thenReturn(Optional.of(unavailablePrimary));

            ImageDTO publicResult = imageService.findPrimaryImage(availablePropertyId, false);

            assertThatThrownBy(() -> imageService.findPrimaryImage(unavailablePropertyId, false))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Property with ID: " + unavailablePropertyId + " not found");

            ImageDTO authenticatedResult = imageService.findPrimaryImage(unavailablePropertyId, true);

            assertThat(publicResult.getId()).isEqualTo(availablePrimary.getId());
            assertThat(authenticatedResult.getId()).isEqualTo(unavailablePrimary.getId());
            verify(imageRepository).findByPropertyIdAndIsPrimaryTrue(unavailablePropertyId);
        }
    }

    private Property buildProperty(UUID propertyId, UUID brokerId) {
        User broker = User.builder().id(brokerId).build();
        return Property.builder().id(propertyId).broker(broker).build();
    }

    private MockMultipartFile imageFile(String filename, int content) {
        return new MockMultipartFile(
                "files",
                filename,
                "image/jpeg",
                new byte[]{(byte) content}
        );
    }

    private Map<String, String> uploadResult(String name) {
        return Map.of(
                "url", "https://images.test/" + name + ".jpg",
                "fileIdentifier", name + "-id"
        );
    }
}
