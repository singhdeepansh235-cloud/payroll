package com.srmcem.payroll.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String email;
    
    private String phone;
    
    private String website;
    
    private String logoPath;
    
    private String financialYear;
}
