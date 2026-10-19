package za.ac.cput;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.domain.User;
import za.ac.cput.domain.enums.Role;
import za.ac.cput.repository.UserRepository;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            String adminEmail = "admin@fundi.com";
            String adminPassword = "Admin@123";

            User admin = userRepository
                    .findByEmail(adminEmail)
                    .orElse(null);

            if (admin == null) {

                admin = User.builder()
                        .fullName("FundiShop Admin")
                        .email(adminEmail)
                        .phone("")
                        .role(Role.ADMIN)
                        .emailVerified(true)
                        .build();

                System.out.println(
                        "Creating FundiShop ADMIN account..."
                );

            } else {

                System.out.println(
                        "FundiShop ADMIN account already exists."
                );
            }

            /*
             * Ensure the account is definitely an ADMIN.
             */
            admin.setRole(Role.ADMIN);

            /*
             * Reset the development password.
             */
            admin.setPassword(
                    passwordEncoder.encode(
                            adminPassword
                    )
            );

            admin.setEmailVerified(true);

            userRepository.save(admin);

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "FundiShop ADMIN credentials"
            );

            System.out.println(
                    "Email: " + adminEmail
            );

            System.out.println(
                    "Password: " + adminPassword
            );

            System.out.println(
                    "Role: " + admin.getRole()
            );

            System.out.println(
                    "======================================"
            );
        };
    }
}