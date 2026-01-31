package io.dobermoney.launchpool.repository;

import io.dobermoney.launchpool.entity.JpaCoin;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link JpaCoin} entities.
 */
public interface CoinRepository extends JpaRepository<JpaCoin, String> {
}
