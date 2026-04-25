package com.finSafe.idempotency_gateway.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String hashPayload(Object payload) {
        try {
            String jsonString = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(jsonString.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(encodedhash);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash payload", e);
        }
    }
}