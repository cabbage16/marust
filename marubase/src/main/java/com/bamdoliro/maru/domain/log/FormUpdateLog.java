package com.bamdoliro.maru.domain.log;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_form_update_log")
@Entity
public class FormUpdateLog extends BaseTimeEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false, name = "form_id")
    private Form form;

    @Builder
    public FormUpdateLog(Long id, String phoneNumber, String clientIp, String userAgent, User user, Form form) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.user = user;
        this.form = form;
    }
}
