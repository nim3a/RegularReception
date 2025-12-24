package com.daryaftmanazam.daryaftcore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Register request DTO
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "نام کاربری الزامی است")
    @Size(min = 3, max = 50, message = "نام کاربری باید بین 3 تا 50 کاراکتر باشد")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "نام کاربری فقط می‌تواند شامل حروف، اعداد و _ باشد")
    private String username;
    
    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل نامعتبر است")
    private String email;
    
    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 8, message = "رمز عبور باید حداقل 8 کاراکتر باشد")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", 
            message = "رمز عبور باید شامل حروف و اعداد باشد")
    private String password;
}
