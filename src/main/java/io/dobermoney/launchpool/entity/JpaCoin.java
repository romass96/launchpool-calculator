package io.dobermoney.launchpool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * JPA entity representing a cryptocurrency coin stored in the database.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "coin")
public class JpaCoin {

    /** Unique identifier (e.g. bitcoin, ethereum). */
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(length = 1000)
    private String image;

}
