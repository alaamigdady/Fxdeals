package com.bloomberg.fxdeals.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloomberg.fxdeals.entity.Currency;
import com.bloomberg.fxdeals.repo.CurrencyRepository;

@Service
public class CurrencyService {

	private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

	@Autowired
	private CurrencyRepository currencyRepository;

	/**
	 * Saves a new currency to the database and caches the currency code and ID.
	 * 
	 * @param currency The currency entity to save.
	 * @return The saved currency entity.
	 */
	public Currency saveCurrency(Currency currency) {
		try {
			Currency savedCurrency = currencyRepository.save(currency);
			logger.info("Currency saved to database and cached: {}", savedCurrency.getCurrencyCode());
			return savedCurrency;
		} catch (Exception e) {
			logger.error("Error saving currency with code: {}", currency.getCurrencyCode(), e);
			throw e;
		}
	}

	public boolean isValidCurrencyCode(String currencyCode) {
		try {
			validateCurrencyCode(currencyCode);
			return true;
		} catch (Exception e) {
			logger.error("Error validating currency code: {}", currencyCode, e);
			return false;
		}
	}

	private void validateCurrencyCode(String currencyCode) {
		try {
			java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
			logger.info("Currency code {} is valid", currencyCode);
		} catch (IllegalArgumentException e) {
			logger.error("Invalid currency code: {}", currencyCode, e);
			throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
		}
	}

	public Currency getCurrencyByCode(String currencyCode) {
		return currencyRepository.findByCurrencyCode(currencyCode).orElse(null);

	}

	public Currency getCurrencyById(Long id) {

		return currencyRepository.findById(id).orElse(null);
	}

}
