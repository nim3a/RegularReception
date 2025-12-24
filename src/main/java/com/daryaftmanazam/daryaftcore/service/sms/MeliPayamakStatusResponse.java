package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.Data;

/**
 * Response DTO for MeliPayamak GetDelivery (status check) API.
 */
@Data
class MeliPayamakStatusResponse {
    /**
     * Delivery status code.
     * 1 = Delivered, 2 = Failed, 8 = Sent, 16 = Pending
     */
    private int status;
}
