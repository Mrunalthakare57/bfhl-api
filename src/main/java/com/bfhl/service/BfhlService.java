package com.bfhl.service;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;

public interface BfhlService {
    BfhlResponse processData(BfhlRequest request, String requestId);
}
