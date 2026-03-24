package com.aditi.smartbuy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "platforms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "base_url")
    private String baseUrl;

    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductPlatformMapping> productMappings = new ArrayList<>();
}
