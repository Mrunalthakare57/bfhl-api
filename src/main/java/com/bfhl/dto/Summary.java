package com.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Summary {

    @JsonProperty("total_elements_received")
    private int totalElementsReceived;

    @JsonProperty("valid_elements_processed")
    private int validElementsProcessed;

    @JsonProperty("invalid_elements_ignored")
    private int invalidElementsIgnored;
}
