package com.hrms.core.model.entity;

import com.hrms.core.model.enums.Position;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Table(name = "workers")
@NamedEntityGraph(
    name = "Worker.full",
    attributeNodes = {
        @NamedAttributeNode("organization"),
        @NamedAttributeNode(value = "person", subgraph = "person-subgraph")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "person-subgraph",
            attributeNodes = @NamedAttributeNode("location")
        )
    }
)
@Data
@Builder(toBuilder = true)
@With
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Координаты обязательны")
    @Embedded
    private Coordinates coordinates;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate;

    @NotNull(message = "Организация обязательна")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Positive(message = "Зарплата должна быть больше 0")
    @Column(nullable = false)
    private double salary;

    @Positive(message = "Рейтинг должен быть больше 0")
    @Column(nullable = false)
    private float rating;

    @NotNull(message = "Дата начала обязательна")
    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @NotNull(message = "Должность обязательна")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @NotNull(message = "Персона обязательна")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = ZonedDateTime.now();
        }
    }
}
