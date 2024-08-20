package com.bloomberg.fxdeals.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "deal")

public class Deal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dealId;

	@Column(name = "deal_unique_id", unique = true, nullable = false)
	private String dealUniqueId;

	@ManyToOne
	@JoinColumn(name = "from_currency_id", referencedColumnName = "currency_id")
	@NotNull
	@Cascade(CascadeType.SAVE_UPDATE)

	private Currency fromCurrency;

	@ManyToOne
	@JoinColumn(name = "to_currency_id", referencedColumnName = "currency_id")
	@NotNull
	@Cascade(CascadeType.SAVE_UPDATE)

	private Currency toCurrency;

	@NotNull
	@Column(name = "deal_timestamp")
	private Timestamp dealTimestamp;

	@NotNull
	@Column(name = "deal_amount")
	private BigDecimal dealAmount;

	public Long getDealId() {
		return dealId;
	}

	public void setDealId(Long dealId) {
		this.dealId = dealId;
	}

	public String getDealUniqueId() {
		return dealUniqueId;
	}

	public void setDealUniqueId(String dealUniqueId) {
		this.dealUniqueId = dealUniqueId;
	}

	public Currency getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(Currency fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public Currency getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(Currency toCurrency) {
		this.toCurrency = toCurrency;
	}

	public Timestamp getDealTimestamp() {
		return dealTimestamp;
	}

	public void setDealTimestamp(Timestamp dealTimestamp) {
		this.dealTimestamp = dealTimestamp;
	}

	public BigDecimal getDealAmount() {
		return dealAmount;
	}

	public void setDealAmount(BigDecimal dealAmount) {
		this.dealAmount = dealAmount;
	}

}
