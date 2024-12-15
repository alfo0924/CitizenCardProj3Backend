package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String birthday;

    @Column(length = 10)
    private String gender;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(length = 500)
    private String address;

    @Column(length = 200)
    private String avatar;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version = 0;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieTicket> movieTickets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountCoupon> discountCoupons;

    // 基本業務方法
    public void updateLoginInfo(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }

    public boolean isEnabled() {
        return this.active;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) {
            role = "ROLE_USER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
