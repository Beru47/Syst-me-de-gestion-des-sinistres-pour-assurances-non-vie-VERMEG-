package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sinistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDate date;
    private String lieu;
    private String description;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;
    private Integer priorityScore;  // New field for priority

    @ManyToOne
    @JoinColumn(name = "assure_id") // Explicit foreign key
    private Assure assure;

    @ManyToOne
    @JoinColumn(name = "expert_id") // Explicit foreign key
    private Expert expert;

    /*@ManyToOne
    private Assure assure;  // Assure associated with the Sinistre
    @ManyToOne
    private Expert expert;  // Expert associated with the Sinister.*/

    // Corrected Constructor
    public Sinistre(String type, LocalDate date, String lieu, String description, ClaimStatus claimStatus, int priority, Assure assure) {
        this.type = type;
        this.date = date;
        this.lieu = lieu;
        this.description = description;
        this.status = claimStatus;
        this.priorityScore = priority;
        this.assure = assure;
    }

    // Getters and Setters (auto-generated by Lombok)

    // Method to update the claim status
    public void mettreÀJourStatut(ClaimStatus newStatus) {
        this.status = newStatus;
    }

    // Method to automatically update the status based on priority
    public void autoUpdateStatus() {
        if (this.priorityScore >= 8) {
            this.status = ClaimStatus.URGENT;
        } else if (this.priorityScore >= 5) {
            this.status = ClaimStatus.IN_PROGRESS;
        } else {
            this.status = ClaimStatus.PENDING;
        }
    }

    // Method to calculate the compensation amount based on the claim type
    public BigDecimal calculerMontantIndemnisation() {
        if (this.type.equalsIgnoreCase("Accident")) {
            return BigDecimal.valueOf(1000);
        } else if (this.type.equalsIgnoreCase("Incendie")) {
            return BigDecimal.valueOf(5000);
        }
        return BigDecimal.ZERO;
    }

    // Setter method for Expert
    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    // Setter method for PriorityScore (automatically calculates priority score if not provided)
    public void setPriorityScore(Integer priorityScore) {
        if (priorityScore == null) {
            // Automatically calculate the priority based on business logic
            this.priorityScore = calculatePriorityScore();
        } else {
            this.priorityScore = priorityScore;
        }
    }

    // Method to calculate priority score (example)
    private Integer calculatePriorityScore() {
        // Replace with actual logic to calculate priority based on claim data
        return 5; // Example static value
    }
}
