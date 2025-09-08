package ru.practicum.explorewithme.comments.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.users.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private LocalDateTime publishedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;
}