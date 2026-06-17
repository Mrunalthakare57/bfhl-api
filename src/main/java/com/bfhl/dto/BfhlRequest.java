package com.bfhl.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BfhlRequest {

    @NotNull(message = "data field is required and must not be null")
    private List<String> data;
}
