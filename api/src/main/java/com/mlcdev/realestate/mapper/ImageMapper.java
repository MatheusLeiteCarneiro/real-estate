package com.mlcdev.realestate.mapper;

import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.entities.Image;

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
