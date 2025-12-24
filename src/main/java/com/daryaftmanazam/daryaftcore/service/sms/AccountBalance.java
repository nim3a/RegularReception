package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the account balance from SMS provider.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

    /**
     * The balance amount.
     */
    private double balance;

    /**
     * The currency code (e.g., "IRR" for Iranian Rial).
     */
    private String currency;
}
