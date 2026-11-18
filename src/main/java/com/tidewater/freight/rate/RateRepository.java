package com.tidewater.freight.rate;

public interface RateRepository {
    double findRate(String zone);
}
