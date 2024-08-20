package com.bloomberg.fxdeals.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bloomberg.fxdeals.entity.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

	Optional<Currency> findByCurrencyCode(String currencyCode);

}
