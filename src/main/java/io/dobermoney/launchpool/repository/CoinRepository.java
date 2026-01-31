package io.dobermoney.launchpool.repository;

import io.dobermoney.launchpool.entity.JpaCoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<JpaCoin, String> {
}
