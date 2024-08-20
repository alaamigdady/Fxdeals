package com.bloomberg.fxdeals.controller;

import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bloomberg.fxdeals.entity.Deal;
import com.bloomberg.fxdeals.service.DealService;
import com.bloomberg.fxdeals.service.DealService.SaveResult;

@RestController
@RequestMapping("/api/deals")
public class DealController {

	@Autowired
	private DealService dealService;

	@PostMapping("/addDeal")
	public ResponseEntity<String> addDeal(@RequestBody Deal deal) {
		dealService.saveDeal(deal);
		return ResponseEntity.ok("Deal added successfully");
	}

	@PostMapping("/addBatch")
	public ResponseEntity<String> addBatchDeals(@RequestParam("file") MultipartFile file) {
		try {
			SaveResult result = dealService.saveDealsFromCsv(new InputStreamReader(file.getInputStream()));
			String message = String.format("Batch deals processing complete: %d out of %d deals saved successfully.",
					result.getSuccessfulDeals(), result.getTotalDeals());

			if (!result.getErrors().isEmpty()) {
				message += "\nErrors:\n" + String.join("\n", result.getErrors());
				return ResponseEntity.status(207).body(message);
			}

			return ResponseEntity.ok(message);

		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error processing batch deals: " + e.getMessage());
		}
	}
}
