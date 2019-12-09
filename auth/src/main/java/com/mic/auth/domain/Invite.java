package com.mic.auth.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "INVITE")
@Data
@ToString(exclude = "invitedUser")
@EqualsAndHashCode(exclude = "invitedUser")
@AllArgsConstructor
@NoArgsConstructor
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true)
    private Long id;

    @Column(name = "BY_USER")
    private Long byUser;

    @Column(name = "EVENT_ID")
    private Long eventId;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="invited_user")
    private User invitedUser;
}
