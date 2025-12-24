package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.Data;

/**
 * Response DTO for MeliPayamak SendSMS API.
 */
@Data
class MeliPayamakResponse {
    /**
     * Return status code (1 = success, other values = error).
     */
    private int retStatus;

    /**
     * Return status description.
     */
    private String strRetStatus;

    /**
     * Message ID or error code.
     */
    private long value;
}
