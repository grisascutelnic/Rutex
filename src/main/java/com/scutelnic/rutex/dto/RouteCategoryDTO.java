package com.scutelnic.rutex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RouteCategoryDTO {

    private String primaryCity;
    private String secondaryCity;
    private String routePath;
    private long viewCount;
}
