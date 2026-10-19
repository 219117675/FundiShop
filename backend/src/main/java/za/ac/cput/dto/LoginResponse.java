package za.ac.cput.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private boolean success;

    private String token;

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String role;

    private String message;
}