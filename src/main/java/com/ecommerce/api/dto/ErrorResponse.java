package com.ecommerce.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ErrorResponse {
    private LocalDate timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
