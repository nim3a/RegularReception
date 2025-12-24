package com.daryaftmanazam.daryaftcore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Login request DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "نام کاربری الزامی است")
    @Size(min = 3, max = 50, message = "نام کاربری باید بین 3 تا 50 کاراکتر باشد")
    private String username;
    
    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 6, message = "رمز عبور باید حداقل 6 کاراکتر باشد")
    private String password;
}
