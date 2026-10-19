package za.ac.cput.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.FundiProfile;
import za.ac.cput.domain.User;
import za.ac.cput.domain.enums.Role;
import za.ac.cput.domain.enums.TradeCategory;
import za.ac.cput.repository.FundiProfileRepository;
import za.ac.cput.repository.UserRepository;
import za.ac.cput.dto.RegistrationRequest;

import java.math.BigDecimal;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final FundiProfileRepository fundiProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            FundiProfileRepository fundiProfileRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.fundiProfileRepository = fundiProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationRequest request) {

        // ---------------------------------------------------------
        // 1. Validate email
        // ---------------------------------------------------------

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Email address is required."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {

            throw new RuntimeException(
                    "Email address is already registered."
            );
        }

        // ---------------------------------------------------------
        // 2. Convert role
        // ---------------------------------------------------------

        if (request.getRole() == null ||
                request.getRole().isBlank()) {

            throw new RuntimeException(
                    "Account type is required."
            );
        }

        Role role;

        try {
            role = Role.valueOf(
                    request.getRole()
                            .trim()
                            .toUpperCase()
            );
        } catch (IllegalArgumentException exception) {

            throw new RuntimeException(
                    "Invalid account type: " + request.getRole()
            );
        }

        // ---------------------------------------------------------
        // 3. Create User
        // ---------------------------------------------------------

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone().trim())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(role)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // ---------------------------------------------------------
        // 4. Create Fundi Profile
        // ---------------------------------------------------------

        if (role == Role.FUNDI) {

            if (request.getTradeCategory() == null ||
                    request.getTradeCategory().isBlank()) {

                throw new RuntimeException(
                        "Trade category is required for Fundi registration."
                );
            }

            TradeCategory tradeCategory =
                    convertTradeCategory(
                            request.getTradeCategory()
                    );

            FundiProfile fundiProfile =
                    FundiProfile.builder()
                            .user(savedUser)
                            .tradeCategory(tradeCategory)
                            .bio(request.getBio())
                            .calloutFee(
                                    request.getCalloutFee() != null
                                            ? BigDecimal.valueOf(
                                            request.getCalloutFee()
                                    )
                                            : null
                            )
                            .hourlyRate(
                                    request.getHourlyRate() != null
                                            ? BigDecimal.valueOf(
                                            request.getHourlyRate()
                                    )
                                            : null
                            )
                            .serviceAreaRadiusKm(
                                    request.getServiceAreaRadiusKm()
                            )
                            .build();

            fundiProfileRepository.save(fundiProfile);
        }

        return savedUser;
    }

    // -------------------------------------------------------------
    // Convert frontend trade category to backend enum
    // -------------------------------------------------------------

    private TradeCategory convertTradeCategory(String value) {

        String category = value
                .trim()
                .toUpperCase();

        return switch (category) {

            case "PLUMBER",
                 "PLUMBING" ->
                    TradeCategory.PLUMBING;

            case "ELECTRICIAN",
                 "ELECTRICAL" ->
                    TradeCategory.ELECTRICAL;

            case "PAINTER",
                 "PAINTING" ->
                    TradeCategory.PAINTING;

            case "HANDYMAN" ->
                    TradeCategory.HANDYMAN;

            case "BUILDER",
                 "TILER",
                 "GARDENER",
                 "CLEANER",
                 "ROOFER",
                 "OTHER" ->
                    TradeCategory.GENERAL;

            default ->
                    throw new RuntimeException(
                            "Unsupported trade category: " + value
                    );
        };
    }
}