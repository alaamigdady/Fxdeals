package com.bloomberg.fxdeals.service;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bloomberg.fxdeals.entity.Currency;
import com.bloomberg.fxdeals.entity.Deal;
import com.bloomberg.fxdeals.repo.DealRepository;
import com.opencsv.CSVReader;

@Service
public class DealService {

	private static final Logger logger = LoggerFactory.getLogger(DealService.class);

	@Autowired
	private DealRepository dealRepository;

	@Autowired
	private CurrencyService currencyService;

	/**
	 * Validates the deal, ensuring all fields are valid, currency codes are correct, and the amount is positive.
	 * 
	 * @param deal The deal to validate.
	 * @return True if valid, false otherwise.
	 */
	public boolean validateSingleDeal(Deal deal) {
		if (deal.getDealUniqueId() == null || deal.getDealUniqueId().isEmpty()) {
			logger.error("Deal validation failed: unique ID is missing");
			return false;
		}

		if (!validateCurrency(deal.getFromCurrency().getCurrencyCode())) {
			logger.error("Deal validation failed: invalid from currency code");
			return false;
		}
		if (!validateCurrency(deal.getToCurrency().getCurrencyCode())) {
			logger.error("Deal validation failed: invalid to currency code");
			return false;
		}
		if (deal.getToCurrency().getCurrencyCode().equals(deal.getFromCurrency().getCurrencyCode())) {
			logger.warn("Invalid CSV row: 'from' currency and 'to' currency cannot be the same.");
			return false;
		}
		if (deal.getDealTimestamp() == null || !isValidTimestamp(deal.getDealTimestamp())) {
			logger.error("Deal validation failed: timestamp is missing or invalid");
			return false;
		}
		if (deal.getDealAmount() == null || !isNumeric(deal.getDealAmount().toString())
				|| deal.getDealAmount().compareTo(BigDecimal.ZERO) <= 0) {
			logger.error("Deal validation failed: amount is missing or not positive");
			return false;
		}
		return true;
	}

	/**
	 * Validates a currency code and ensures it's a valid ISO code.
	 * 
	 * @param currencyCode The currency code to validate.
	 * @return True if valid, false otherwise.
	 */
	public boolean validateCurrency(String currencyCode) {
		if (currencyCode == null || currencyCode.isEmpty()) {
			logger.warn("Currency code is missing");
			return false;
		}

		if (!currencyService.isValidCurrencyCode(currencyCode)) {
			logger.warn("Currency code {} is invalid.", currencyCode);
			return false;
		}
		return true;
	}

	private boolean isValidTimestamp(Timestamp timestamp) {
		try {
			return timestamp != null;
		} catch (Exception e) {
			logger.error("Invalid timestamp: {}", timestamp, e);
			return false;
		}
	}

	private boolean isNumeric(String str) {
		try {
			new BigDecimal(str);
			return true;
		} catch (NumberFormatException e) {
			logger.error("Invalid numeric value: {}", str, e);
			return false;
		}
	}

	/**
	 * Checks if the deal with the given unique ID already exists in the database.
	 * 
	 * @param dealUniqueId The unique ID of the deal to check.
	 * @return True if the deal exists, false otherwise.
	 */
	public boolean isDealAlreadyExists(String dealUniqueId) {
		Optional<Deal> existingDeal = dealRepository.findByDealUniqueId(dealUniqueId);
		return existingDeal.isPresent();
	}

	/**
	 * Saves multiple deals from a CSV file to the database, one by one, and returns the results.
	 *
	 * @param reader The reader for the CSV file.
	 * @return A summary of the results, including the number of successful and failed deals.
	 */
	public SaveResult saveDealsFromCsv(Reader reader) {
		int totalDeals = 0;
		int successfulDeals = 0;
		List<String> errors = new ArrayList<>();

		try (CSVReader csvReader = new CSVReader(reader)) {
			String[] values;

			while ((values = csvReader.readNext()) != null) {
				totalDeals++;
				Deal deal = validateAndParseCsvRow(values);
				if (deal != null) {
					try {
						saveDeal(deal);
						successfulDeals++;
					} catch (Exception e) {
						logger.error("Error saving deal: {}", deal.getDealUniqueId(), e);
						errors.add("Failed to save deal with ID " + deal.getDealUniqueId() + ": " + e.getMessage());
					}
				} else {
					String errorMsg = "Invalid or duplicate deal with unique ID: " + values[0];
					logger.warn(errorMsg);
					errors.add(errorMsg);
				}
			}

			logger.info("Finished processing CSV file: {} out of {} deals saved successfully.", successfulDeals, totalDeals);

		} catch (Exception e) {
			logger.error("Error processing CSV file for deals", e);
			errors.add("General error processing CSV file: " + e.getMessage());
		}

		return new SaveResult(successfulDeals, totalDeals, errors);
	}

	/**
	 * Saves a single deal to the database after validation.
	 *
	 * @param deal The deal to save.
	 * @return The saved deal entity.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Deal saveDeal(Deal deal) {
		if (isDealAlreadyExists(deal.getDealUniqueId())) {
			logger.warn("Deal with the same unique ID already exists: {}", deal.getDealUniqueId());
			throw new IllegalArgumentException("Deal with the same unique ID already exists: " + deal.getDealUniqueId());
		}
		return dealRepository.save(deal);
	}

	public Deal saveSingleDeal(Deal deal) {
		if (!validateSingleDeal(deal)) {
			logger.warn("Deal validation failed for unique ID: {}", deal.getDealUniqueId());
			throw new IllegalArgumentException("Deal validation failed for unique ID: " + deal.getDealUniqueId());
		}
		return saveDeal(deal);
	}

	/**
	 * Validates and parses a single row of data from the CSV file before creating a Deal object.
	 *
	 * @param values The values from a single row in the CSV file.
	 * @return A valid Deal object if validation passes, otherwise null.
	 */
	public Deal validateAndParseCsvRow(String[] values) {
		try {
			if (values.length < 5) {
				logger.warn("Invalid CSV row: incorrect number of fields.");
				return null;
			}

			Deal deal = new Deal();

			deal.setDealUniqueId(values[0]);
			if (deal.getDealUniqueId() == null || deal.getDealUniqueId().isEmpty()) {
				logger.warn("Invalid CSV row: deal unique ID is missing.");
				return null;
			}

			String fromCurrencyCode = values[1];
			if (!validateCurrency(fromCurrencyCode)) {
				return null;
			}
			Currency fromCurrency = currencyService.getCurrencyByCode(fromCurrencyCode);

			if (fromCurrency == null) {
				fromCurrency = new Currency();
				fromCurrency.setCurrencyCode(fromCurrencyCode);
				fromCurrency = currencyService.saveCurrency(fromCurrency);
			}
			deal.setFromCurrency(fromCurrency);

			String toCurrencyCode = values[2];
			if (!validateCurrency(toCurrencyCode)) {
				return null;
			}
			Currency toCurrency = currencyService.getCurrencyByCode(toCurrencyCode);
			if (toCurrency == null) {
				toCurrency = new Currency();
				toCurrency.setCurrencyCode(toCurrencyCode);
				toCurrency = currencyService.saveCurrency(toCurrency);
			}
			deal.setToCurrency(toCurrency);

			if (fromCurrencyCode.equals(toCurrencyCode)) {
				logger.warn("Invalid CSV row: 'from' currency and 'to' currency cannot be the same.");
				return null;
			}

			try {
				Timestamp dealTimestamp = Timestamp.valueOf(values[3]);
				deal.setDealTimestamp(dealTimestamp);
			} catch (IllegalArgumentException e) {
				logger.warn("Invalid CSV row: timestamp is invalid.", e);
				return null;
			}

			try {
				BigDecimal dealAmount = new BigDecimal(values[4]);
				if (dealAmount.compareTo(BigDecimal.ZERO) <= 0) {
					logger.warn("Invalid CSV row: deal amount must be positive.");
					return null;
				}
				deal.setDealAmount(dealAmount);
			} catch (NumberFormatException e) {
				logger.warn("Invalid CSV row: deal amount is not a valid number.", e);
				return null;
			}

			return deal;

		} catch (Exception e) {
			logger.error("Error validating CSV row", e);
			return null;
		}
	}

	public static class SaveResult {

		private final int successfulDeals;
		private final int totalDeals;
		private final List<String> errors;

		public SaveResult(int successfulDeals, int totalDeals, List<String> errors) {
			this.successfulDeals = successfulDeals;
			this.totalDeals = totalDeals;
			this.errors = errors;
		}

		public int getSuccessfulDeals() {
			return successfulDeals;
		}

		public int getTotalDeals() {
			return totalDeals;
		}

		public List<String> getErrors() {
			return errors;
		}
	}

}
