package com.spring5.movieservice.common;

import org.springframework.stereotype.Component;

/**
 * Tax Calculator Utility
 * Calculates GST and service charges for bookings
 */
@Component
public class TaxCalculator {

    // Tax rates (configurable, can be moved to application.properties)
    private static final double GST_RATE = 0.18; // 18% GST
    private static final double SERVICE_CHARGE_RATE = 0.05; // 5% Service charge

    /**
     * Calculate tax and service charge for a booking
     * @param basePrice Base price before taxes
     * @return TaxCalculationResult containing tax amount, service charge, and total
     */
    public TaxCalculationResult calculateTaxes(Double basePrice) {
        if (basePrice == null || basePrice <= 0) {
            return new TaxCalculationResult(0.0, 0.0, 0.0, basePrice);
        }

        double taxAmount = basePrice * GST_RATE;
        double serviceCharge = basePrice * SERVICE_CHARGE_RATE;
        double totalPrice = basePrice + taxAmount + serviceCharge;

        return new TaxCalculationResult(basePrice, taxAmount, serviceCharge, totalPrice);
    }

    /**
     * Calculate taxes with discount
     * @param basePrice Base price before taxes
     * @param discountAmount Discount amount to apply
     * @return TaxCalculationResult
     */
    public TaxCalculationResult calculateTaxesWithDiscount(Double basePrice, Double discountAmount) {
        if (basePrice == null || basePrice <= 0) {
            return new TaxCalculationResult(0.0, 0.0, 0.0, basePrice);
        }

        double priceAfterDiscount = basePrice - (discountAmount != null ? discountAmount : 0.0);
        if (priceAfterDiscount < 0) {
            priceAfterDiscount = 0.0;
        }

        double taxAmount = priceAfterDiscount * GST_RATE;
        double serviceCharge = priceAfterDiscount * SERVICE_CHARGE_RATE;
        double totalPrice = priceAfterDiscount + taxAmount + serviceCharge;

        return new TaxCalculationResult(priceAfterDiscount, taxAmount, serviceCharge, totalPrice);
    }

    public static class TaxCalculationResult {
        private final double basePrice;
        private final double taxAmount;
        private final double serviceCharge;
        private final double totalPrice;

        public TaxCalculationResult(double basePrice, double taxAmount, double serviceCharge, double totalPrice) {
            this.basePrice = basePrice;
            this.taxAmount = taxAmount;
            this.serviceCharge = serviceCharge;
            this.totalPrice = totalPrice;
        }

        public double getBasePrice() {
            return basePrice;
        }

        public double getTaxAmount() {
            return taxAmount;
        }

        public double getServiceCharge() {
            return serviceCharge;
        }

        public double getTotalPrice() {
            return totalPrice;
        }
    }
}

