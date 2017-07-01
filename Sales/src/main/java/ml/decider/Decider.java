package ml.decider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.*;

import api.contracts.ISale;
import api.preferences.SalesPreferences;
import api.types.sales.ProductSale;
import ml.common.property.saleproperty.ASaleProperty;

public class Decider {

	public static ASaleProperty decideBestSale(SalesPreferences salesPreferences, Set<ASaleProperty> properties){
		
		for (ASaleProperty aSaleProperty : properties) {
			return aSaleProperty;
		}
		
		
		return null;
	}
	public static ASaleProperty decideSaleSimilar(SalesPreferences salesPreferences, Set<ASaleProperty> properties
			,ISale saleToCompare){
		
		List<ASaleProperty> sales = properties.stream().filter(p -> p.getOffer().getProduct().equals(saleToCompare.getProduct()))
			.sorted((ASaleProperty p1, ASaleProperty p2) -> {
				ProductSale p1Sale = p1.getOffer();
				ProductSale p2Sale = p2.getOffer();
				int p1AmountDiff = Math.abs(saleToCompare.getTotalAmount() - p1Sale.getTotalAmount());
				int p2AmountDiff = Math.abs(saleToCompare.getTotalAmount() - p2Sale.getTotalAmount());
				
				if (p1AmountDiff != p2AmountDiff){
					return p1AmountDiff - p2AmountDiff;
				}
				
				
				double p1PriceDiff = Math.abs(saleToCompare.getTotalPrice() - p1Sale.getTotalPrice());
				double p2PriceDiff = Math.abs(saleToCompare.getTotalPrice() - p2Sale.getTotalPrice());
				
				if (p1PriceDiff < p2PriceDiff){
					return -1;
				} else if (p2PriceDiff < p1PriceDiff){
					return 1;
				} else {
					return 0;
				}
			}).collect(Collectors.toList());
		
		return sales.isEmpty() ? null : sales.get(0);

	}
}
