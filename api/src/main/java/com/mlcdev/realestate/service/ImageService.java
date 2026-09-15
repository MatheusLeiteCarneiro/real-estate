package com.mlcdev.realestate.service;

import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.entities.Image;
import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.exception.*;
import com.mlcdev.realestate.mapper.ImageMapper;
import com.mlcdev.realestate.repository.ImageRepository;
import com.mlcdev.realestate.repository.PropertyRepository;
import com.mlcdev.realestate.security.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageService {

    private final FileStorageService fileStorageService;
    private final ImageRepository imageRepository;
    private final PropertyRepository propertyRepository;
    @Value("${property.image.folder}")
    private String propertyImageFolder;

    @Transactional(readOnly = true)
    public List<ImageDTO> findAllImages(UUID propertyId, boolean canViewUnavailable) {
        verifyIfPropertyIsUnavailableAndUserHasPermission(propertyId,canViewUnavailable);
        log.debug("Retrieving all images from the property with ID: {}", propertyId);
        List<Image> imageList = imageRepository.findAllByPropertyId(propertyId);
        return imageList.stream().sorted(Comparator.comparing(Image::isPrimary).reversed()).map(ImageMapper::entityToDTO).toList();
    }

    @Transactional(readOnly = true)
    public ImageDTO findPrimaryImage(UUID propertyId, boolean canViewUnavailable) {
        verifyIfPropertyIsUnavailableAndUserHasPermission(propertyId,canViewUnavailable);
        log.debug("Retrieving primary image from the property with ID: {}", propertyId);
        Image primaryImage = imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId).orElseThrow(() -> new NotFoundException("Primary Image not found for property with Id: " + propertyId));
        return ImageMapper.entityToDTO(primaryImage);
    }

    @Transactional
    public List<ImageDTO> saveImages(UUID propertyId, List<MultipartFile> files, UUID brokerId, boolean isAdmin) {

        if (files.isEmpty()) {
            log.warn("Can't save with a empty file list");
            throw new EmptyResourceException("The file list is empty");
        }

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type uploaded: {}", contentType);
                throw new BusinessRuleException("Only image files are allowed");
            }
        }

        log.info("Saving {} images for the property with ID: {}", files.size(), propertyId);


        Property property = propertyRepository.findById(propertyId).orElseThrow(() -> new NotFoundException("Property with ID: " + propertyId + " not found"));
        OwnershipValidator.propertyVerifyBrokerPermission(property, brokerId, isAdmin);
        boolean noPrimary = imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId).isEmpty();

        List<Image> images = files.stream().map(_ -> Image.builder().isPrimary(false).property(property).build()).collect(Collectors.toCollection(ArrayList::new));

        if (noPrimary) {
            images.getFirst().setPrimary(true);
        }
        List<String> uploadedIdentifiers = new ArrayList<>();
        try {

            for (int i = 0; i < files.size(); i++) {
                Map<String, String> fileInformation = fileStorageService.uploadFile(files.get(i).getBytes(), propertyImageFolder);
                uploadedIdentifiers.add(fileInformation.get("fileIdentifier"));
                images.get(i).setUrl(fileInformation.get("url"));
                images.get(i).setFileIdentifier(fileInformation.get("fileIdentifier"));

            }

            List<Image> savedImages = imageRepository.saveAll(images);

            log.info("Successfully saved {} images for the property with ID: {}", savedImages.size(), propertyId);

            return savedImages.stream().map(ImageMapper::entityToDTO).toList();

        } catch (Exception e) {
            uploadedIdentifiers.forEach(fileIdentifier -> {
                try {
                    fileStorageService.deleteFile(fileIdentifier);
                } catch (Exception _) {
                    log.warn("Failed to delete file during rollback. Identifier: {}", fileIdentifier);
                }
            });
            throw new FileStorageException("Error occurred on the saving of the files", e);
        }

    }

    @Transactional
    public void deleteImage(UUID propertyId, UUID imageId, UUID brokerId, boolean isAdmin) {
        log.info("Deleting image with ID: {} from the property with ID: {}", imageId, propertyId);
        Image image = findImageByIdAndVerifyIfRelatedToProperty(propertyId, imageId);
        OwnershipValidator.propertyVerifyBrokerPermission(image.getProperty(), brokerId, isAdmin);
        String fileIdentifier = image.getFileIdentifier();
        imageRepository.delete(image);

        log.info("Image with ID: {} successfully deleted", imageId);


        if (image.isPrimary()) {
            log.info("The image with ID: {} was a primary image", imageId);
            List<Image> propertyImages = imageRepository.findAllByPropertyIdAndIsPrimaryFalse(propertyId);
            if (!propertyImages.isEmpty()) {
                Image newPrimaryImage = propertyImages.getFirst();
                newPrimaryImage.setPrimary(true);
                log.info("Primary image flag was set to the image with ID: {}", newPrimaryImage.getId());
            }

        }

        try {
            fileStorageService.deleteFile(fileIdentifier);
        } catch (Exception _) {
            log.warn("Failed to delete file during deletion. Identifier: {}", fileIdentifier);
        }
    }


    @Transactional
    public ImageDTO updateImageAsPrimary(UUID propertyId, UUID imageId, UUID brokerId, boolean isAdmin) {
        Image newPrimaryImage = findImageByIdAndVerifyIfRelatedToProperty(propertyId, imageId);
        OwnershipValidator.propertyVerifyBrokerPermission(newPrimaryImage.getProperty(), brokerId, isAdmin);
        if (newPrimaryImage.isPrimary()) {
            log.info("The image with ID: {} is already primary", imageId);
            return ImageMapper.entityToDTO(newPrimaryImage);
        }

        log.info("Setting image with ID: {} as primary", imageId);
        imageRepository.findByPropertyIdAndIsPrimaryTrue(propertyId).ifPresent(old ->
        {
            old.setPrimary(false);
            imageRepository.saveAndFlush(old);
        });
        newPrimaryImage.setPrimary(true);
        imageRepository.save(newPrimaryImage);
        log.info("Image with ID: {} successfully set as primary", imageId);
        return ImageMapper.entityToDTO(newPrimaryImage);
    }

    @Transactional
    public void deleteAllImagesForProperty(UUID propertyId) {
        log.info("Deleting all images for the property with ID: {}", propertyId);
        List<String> imagesFileIdentifiers = imageRepository.findAllByPropertyId(propertyId).stream().map(Image::getFileIdentifier).toList();
        imagesFileIdentifiers.forEach(fileIdentifier -> {
            try {
                fileStorageService.deleteFile(fileIdentifier);
            } catch (Exception _) {
                log.warn("Failed to delete file during deletion of all property files. Identifier: {}", fileIdentifier);
            }
        });
        log.info("All images for the property with ID: {} successfully deleted", propertyId);
    }


    @Transactional(readOnly = true)
    public Map<UUID, ImageDTO> findPrimaryImagesForProperties(List<UUID> propertyIds) {
        return imageRepository.findPrimaryImagesByPropertyIds(propertyIds)
                .stream()
                .collect(Collectors.toMap(image -> image.getProperty().getId(), ImageMapper::entityToDTO));
    }


    private Image findImageByIdAndVerifyIfRelatedToProperty(UUID propertyId, UUID imageId) {
        Image image = imageRepository.findById(imageId).orElseThrow(() -> new NotFoundException("Image with ID: " + imageId + " not found"));
        log.debug("Checking if the image with ID : {} is related to the property with ID: {}", imageId, propertyId);
        if (!image.getProperty().getId().equals(propertyId)) {
            log.warn("The image with ID: {} is not related to the property with ID: {}", imageId, propertyId);
            throw new ResourceMismatchException("Image with ID: " + imageId + " it's not from the property with id: " + propertyId);
        }
        log.debug("The property and the image are related");
        return image;
    }

    private void verifyIfPropertyIsUnavailableAndUserHasPermission(UUID propertyId, boolean canViewUnavailable){
        Optional<Boolean> propertyAvailable = propertyRepository.isAvailableById(propertyId);
        if(propertyAvailable.isEmpty() || (!propertyAvailable.get() && !canViewUnavailable)){
            throw new NotFoundException("Property with ID: " + propertyId + " not found");
        }
    }

}
