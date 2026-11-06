package com.example.Hospital.model;

import com.example.Hospital.exception.InsufficientStockException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "medicine")
@Check(constraints = "quantity >= 0")
@Getter
@Setter
public class Medicine {

    public static final int MAX_QUANTITY = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicineId;

    @Column(nullable = false, length = 150)
    private String medicineName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "requires_prescription", nullable = false)
    private boolean requiresPrescription = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Setter(AccessLevel.NONE)
    @Column(name = "quantity", nullable = false, columnDefinition = "integer default 0")
    private int quantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private Long version;

    public void updateStock(int newQuantity) {
        if (newQuantity < 0 || newQuantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity must be between 0 and " + MAX_QUANTITY);
        }
        this.quantity = newQuantity;
    }

    public void increaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > MAX_QUANTITY - this.quantity) {
            throw new IllegalArgumentException(
                    "Stock cannot exceed " + MAX_QUANTITY);
        }
        this.quantity += amount;
    }

    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > this.quantity) {
            throw new InsufficientStockException(
                    medicineName, this.quantity, amount);
        }
        this.quantity -= amount;
    }
}
