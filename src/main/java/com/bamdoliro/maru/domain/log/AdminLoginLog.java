package com.bamdoliro.maru.domain.log;

import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_admin_login_log")
@Entity
public class AdminLoginLog extends BaseTimeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String clientIp;

    @Column(nullable = false)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public AdminLoginLog(String phoneNumber, String clientIp, String userAgent, User user) {
        this.phoneNumber = phoneNumber;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.user = user;
    }
}
