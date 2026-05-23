package com.portfolio.tracker.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long account;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "asset_id", nullable = false)
    private Long asset;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "price_per_unit", precision = 15, scale = 4, nullable = false)
    private BigDecimal pricePerUnit;
}
