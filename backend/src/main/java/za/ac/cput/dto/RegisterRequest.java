package za.ac.cput.dto;

import lombok.Getter;
import lombok.Setter;
import za.ac.cput.domain.enums.Role;

import java.math.BigDecimal;

@Getter
@Setter
public class RegisterRequest {

    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Role role;

    // Fundi-specific fields
    private String tradeCategory;
    private String bio;
    private BigDecimal calloutFee;
    private BigDecimal hourlyRate;
    private Integer serviceAreaRadiusKm;
}