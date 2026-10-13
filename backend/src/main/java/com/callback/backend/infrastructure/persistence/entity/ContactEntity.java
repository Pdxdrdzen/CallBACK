package com.callback.backend.infrastructure.persistence.entity;

import com.callback.backend.domain.model.PriorityRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ContactEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch= FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;

    @Column(nullable = false,length=150)
    private String name;

    @Column(name="phone_number",nullable=false,length=30)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name="priority_role",nullable=false,length=50)
    private PriorityRole priorityRole;

    @CreationTimestamp
    @Column(name = "created_at",nullable=false,updatable = false)
    private OffsetDateTime createdAt;

}
