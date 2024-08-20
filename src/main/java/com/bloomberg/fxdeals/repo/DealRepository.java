package com.bloomberg.fxdeals.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bloomberg.fxdeals.entity.Deal;

public interface DealRepository extends JpaRepository<Deal, Long> {

	Optional<Deal> findByDealUniqueId(String dealUniqueId);
}
