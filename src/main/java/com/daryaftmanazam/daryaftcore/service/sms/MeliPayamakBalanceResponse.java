package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.Data;

/**
 * Response DTO for MeliPayamak GetCredit (balance check) API.
 */
@Data
class MeliPayamakBalanceResponse {
    /**
     * Account balance value in Iranian Rial.
     */
    private double value;
}
