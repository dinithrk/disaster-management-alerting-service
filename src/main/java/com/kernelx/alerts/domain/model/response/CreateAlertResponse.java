package com.kernelx.alerts.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAlertResponse {

    private Integer createdCount;
    private Integer resolvedCount;
    private String message;
}
