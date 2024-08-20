package com.bloomberg.fxdeals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bloomberg.fxdeals.entity.Currency;
import com.bloomberg.fxdeals.entity.Deal;
import com.bloomberg.fxdeals.repo.DealRepository;
import com.bloomberg.fxdeals.service.CurrencyService;
import com.bloomberg.fxdeals.service.DealService;

class DealServiceTest {

	@Mock
	private DealRepository dealRepository;

	@Mock
	private CurrencyService currencyService;

	@InjectMocks
	private DealService dealService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void validateAndParseCsvRow_validRow_shouldReturnDeal() {
		String[] validRow = { "deal1", "USD", "EUR", "2024-08-20 12:30:00", "1000.00" };
		Currency usd = new Currency();
		usd.setCurrencyCode("USD");
		Currency eur = new Currency();
		eur.setCurrencyCode("EUR");

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);
		when(currencyService.isValidCurrencyCode("USD")).thenCallRealMethod();

		when(currencyService.getCurrencyByCode("EUR")).thenReturn(eur);
		when(currencyService.isValidCurrencyCode("EUR")).thenCallRealMethod();

		Deal deal = dealService.validateAndParseCsvRow(validRow);

		assertNotNull(deal);
		assertEquals("deal1", deal.getDealUniqueId());
		assertEquals(usd, deal.getFromCurrency());
		assertEquals(eur, deal.getToCurrency());
		assertEquals(Timestamp.valueOf("2024-08-20 12:30:00"), deal.getDealTimestamp());
		assertEquals(new BigDecimal("1000.00"), deal.getDealAmount());
	}

	@Test
	void validateAndParseCsvRow_missingFields_shouldReturnNull() {
		String[] missingFieldRow = { "deal2", "USD", "EUR", "2024-08-20 12:30:00" };
		Deal deal = dealService.validateAndParseCsvRow(missingFieldRow);
		assertNull(deal);
	}

	//
	@Test
	void validateAndParseCsvRow_invalidCurrencyCode_shouldReturnNull() {
		String[] invalidCurrencyRow = { "deal2", "INVALID", "EUR", "2024-08-20 12:30:00", "1000.00" };
		when(currencyService.getCurrencyByCode("INVALID")).thenReturn(null);

		Deal deal = dealService.validateAndParseCsvRow(invalidCurrencyRow);

		assertNull(deal);
	}

	//
	@Test
	void validateAndParseCsvRow_sameFromAndToCurrency_shouldReturnNull() {
		String[] sameCurrencyRow = { "deal3", "USD", "USD", "2024-08-20 12:30:00", "1000.00" };
		Currency usd = new Currency();
		usd.setCurrencyCode("USD");

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);

		Deal deal = dealService.validateAndParseCsvRow(sameCurrencyRow);

		assertNull(deal);
	}

	//
	@Test
	void validateAndParseCsvRow_invalidTimestamp_shouldReturnNull() {
		String[] invalidTimestampRow = { "deal4", "USD", "EUR", "invalid-timestamp", "1000.00" };
		Deal deal = dealService.validateAndParseCsvRow(invalidTimestampRow);
		assertNull(deal);
	}

	@Test
	void validateAndParseCsvRow_invalidNumericalAmount_shouldReturnNull() {
		String[] invalidAmountRow = { "deal5", "USD", "EUR", "2024-08-20 12:30:00", "not-a-number" };
		Deal deal = dealService.validateAndParseCsvRow(invalidAmountRow);
		assertNull(deal);
	}

	@Test
	void validateAndParseCsvRow_nonPositiveAmount_shouldReturnNull() {
		String[] nonPositiveAmountRow = { "deal5", "USD", "EUR", "2024-08-20 12:30:00", "-1000.00" };
		Deal deal = dealService.validateAndParseCsvRow(nonPositiveAmountRow);
		assertNull(deal);
	}

	@Test
	void saveDealsFromCsv_validDeals_shouldSaveAllDeals() {
		String csvContent = "deal6,AUD,EUR,2024-08-20 12:30:00,1000.00\n" +
				"deal7,GBP,USD,2024-08-20 13:30:00,1500.50";

		Currency aud = new Currency();
		aud.setCurrencyCode("AUD");
		aud.setId(1L); // Mocked ID for the currency

		Currency eur = new Currency();
		eur.setCurrencyCode("EUR");
		eur.setId(2L); // Mocked ID for the currency

		Currency gbp = new Currency();
		gbp.setCurrencyCode("GBP");
		gbp.setId(3L); // Mocked ID for the currency

		Currency usd = new Currency();
		usd.setCurrencyCode("USD");
		usd.setId(4L); // Mocked ID for the currency

		// Ensure that the service returns the correct Currency objects when queried
		when(currencyService.getCurrencyByCode("AUD")).thenReturn(aud);
		when(currencyService.isValidCurrencyCode("AUD")).thenCallRealMethod();

		when(currencyService.getCurrencyByCode("EUR")).thenReturn(eur);
		when(currencyService.isValidCurrencyCode("EUR")).thenCallRealMethod();

		when(currencyService.getCurrencyByCode("GBP")).thenReturn(gbp);
		when(currencyService.isValidCurrencyCode("GBP")).thenCallRealMethod();

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);
		when(currencyService.isValidCurrencyCode("USD")).thenCallRealMethod();

		DealService.SaveResult result = dealService.saveDealsFromCsv(new StringReader(csvContent));

		verify(dealRepository, times(2)).save(any(Deal.class));

		assertEquals(2, result.getSuccessfulDeals(), "Number of successful deals should be 2");
		assertEquals(2, result.getTotalDeals(), "Total number of deals should be 2");
		assertTrue(result.getErrors().isEmpty(), "There should be no errors");
	}

	@Test
	void testValidateCurrencyIsCalled() {
		Currency aud = new Currency();
		aud.setCurrencyCode("AUD");
		aud.setId(1L);

		when(currencyService.getCurrencyByCode("AUD")).thenReturn(aud);
		when(currencyService.isValidCurrencyCode("AUD")).thenCallRealMethod();

		boolean result = dealService.validateCurrency("AUD");

		System.out.println("Currency validation result: " + result);
		assertTrue(result, "Currency should be valid");
		verify(currencyService, times(1)).isValidCurrencyCode("AUD");
	}

	@Test
	void saveDealsFromCsv_invalidAndValidDeals_shouldSaveValidDealsOnly() {
		String csvContent = "deal8,USD,EUR,2024-08-20 12:30:00,1000.00\n" +
				"deal9,USD,USD,2024-08-20 13:30:00,1500.50"; // Invalid: same from/to currency

		Currency usd = new Currency();
		usd.setCurrencyCode("USD");
		Currency eur = new Currency();
		eur.setCurrencyCode("EUR");

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);
		when(currencyService.isValidCurrencyCode("USD")).thenCallRealMethod();

		when(currencyService.getCurrencyByCode("EUR")).thenReturn(eur);
		when(currencyService.isValidCurrencyCode("EUR")).thenCallRealMethod();

		DealService.SaveResult result = dealService.saveDealsFromCsv(new StringReader(csvContent));

		verify(dealRepository, times(1)).save(any(Deal.class)); // Only one deal should be saved
		assertEquals(1, result.getSuccessfulDeals());
		assertEquals(2, result.getTotalDeals());
		assertEquals(1, result.getErrors().size()); // One error expected due to invalid deal
	}

	//
	@Test
	void saveDealsFromCsv_dealAlreadyExists_shouldNotSaveDuplicate() {
		String csvContent = "deal1,USD,EUR,2024-08-20 12:30:00,1000.00";

		Currency usd = new Currency();
		usd.setCurrencyCode("USD");
		Currency eur = new Currency();
		eur.setCurrencyCode("EUR");

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);
		when(currencyService.getCurrencyByCode("EUR")).thenReturn(eur);
		when(dealRepository.findByDealUniqueId("deal1")).thenReturn(Optional.of(new Deal()));

		DealService.SaveResult result = dealService.saveDealsFromCsv(new StringReader(csvContent));

		verify(dealRepository, never()).save(any(Deal.class));
		assertEquals(0, result.getSuccessfulDeals());
		assertEquals(1, result.getTotalDeals());
		assertEquals(1, result.getErrors().size());
	}

	//

	@Test
	void saveSingleDeal_invalidDeal_shouldThrowException() {
		Currency usd = new Currency();
		usd.setCurrencyCode("USD");
		Currency eur = new Currency();
		eur.setCurrencyCode("EUR");

		Deal deal = new Deal();
		deal.setDealUniqueId("deal11");
		deal.setFromCurrency(usd);
		deal.setToCurrency(usd); // Invalid: same currency for from and to
		deal.setDealTimestamp(Timestamp.valueOf("2024-08-20 12:30:00"));
		deal.setDealAmount(new BigDecimal("1000.00"));

		when(currencyService.getCurrencyByCode("USD")).thenReturn(usd);

		assertThrows(IllegalArgumentException.class, () ->
			{
				dealService.saveSingleDeal(deal);
			});

		verify(dealRepository, never()).save(any(Deal.class));
	}
}
