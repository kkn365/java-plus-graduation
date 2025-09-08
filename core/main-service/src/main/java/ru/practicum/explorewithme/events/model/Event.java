package ru.practicum.explorewithme.events.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.users.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "location_lat", nullable = false)
    private Float locationLat;

    @Column(name = "location_lon", nullable = false)
    private Float locationLon;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit = 0;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;
}