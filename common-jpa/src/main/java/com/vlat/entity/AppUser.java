package com.vlat.entity;

import com.vlat.entity.enums.UserState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column(name = "telegram_id")
    private Long telegramId;
    @CreationTimestamp
    @Column
    private LocalDateTime firstLoginDate;
    @Column
    private String firstname;
    @Column
    private String lastname;
    @Column
    private String username;
    @Column
    private String email;
    @Column
    private Boolean isActive;
    @Column
    @Enumerated(EnumType.STRING )
    private UserState state;
}
