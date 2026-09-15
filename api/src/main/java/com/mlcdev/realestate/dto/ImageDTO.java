package com.mlcdev.realestate.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDTO {

    private UUID id;

    private String fileIdentifier;

    private String url;

    private Boolean isPrimary;
}