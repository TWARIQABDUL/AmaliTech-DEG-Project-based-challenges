package com.finSafe.idempotency_gateway.Dto;

import lombok.Data;

@Data
public class IdopRecord {
    

    public enum Status {
        PROCESSING,
        COMPLETED,
    }

     private String idempotencyKey;
 
    private String requestBodyHash;
 
    private Status state;
 
    private int responseStatus;
 
    private String responseBody;
    private long createdAt;
 

}
