package com.bloomberg.fxdeals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "currency")

public class Currency {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "currency_id")
	private Long id;

	@Column(name = "currency_code", unique = true, nullable = false)
	private String currencyCode; // ISO 4217 code

	@Column(name = "currency_name")
	private String currencyName;

	@Column(name = "currency_symbol")
	private String currencySymbol;

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
