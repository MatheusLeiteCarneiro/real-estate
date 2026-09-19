package com.mlcdev.realestate.api.mapper;

import com.mlcdev.realestate.api.dto.ImageDTO;
import com.mlcdev.realestate.api.entities.Image;

public class ImageMapper {

    private ImageMapper() {
    }

    public static ImageDTO entityToDTO(Image entity) {
        return ImageDTO.builder()
                .id(entity.getId())
                .fileIdentifier(entity.getFileIdentifier())
                .url(entity.getUrl())
                .isPrimary(entity.isPrimary())
                .build();
    }

}
