package com.example.Hospital.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final String medicineName;
    private final int available;
    private final int requested;

    public InsufficientStockException(String medicineName, int available, int requested) {
        super("Not enough stock for " + medicineName
                + ". Available: " + available + ", requested: " + requested);
        this.medicineName = medicineName;
        this.available = available;
        this.requested = requested;
    }
}