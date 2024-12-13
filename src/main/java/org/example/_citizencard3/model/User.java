package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.example._citizencard3.model.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.ROLE_USER;

    @Column(length = 500)
    private String address;

    @Column(length = 200)
    private String avatar;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
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

    // 重置密碼相關欄位 - 使用現有欄位的額外功能
    @Transient
    private String resetToken;

    @Transient
    private LocalDateTime resetTokenExpiry;

    // 關聯映射
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovieTicket> movieTickets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DiscountCoupon> discountCoupons;

    // 業務方法
    public void updateLoginInfo(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ROLE_ADMIN;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.role.name()));
    }

    // 重置密碼相關方法
    public void setResetToken(String token) {
        this.resetToken = token;
    }

    public void setResetTokenExpiry(LocalDateTime expiry) {
        this.resetTokenExpiry = expiry;
    }

    public String getResetToken() {
        return this.resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return this.resetTokenExpiry;
    }

    // 錢包相關方法
    public void initializeWallet() {
        if (this.wallet == null) {
            this.wallet = new Wallet();
            this.wallet.setUser(this);
            this.wallet.setBalance(0.0);
        }
    }

    // 驗證方法
    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return this.active;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return this.active && this.emailVerified;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) {
            role = UserRole.ROLE_USER;
        }
        initializeWallet();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
