package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByFundiProfileId(Long fundiProfileId);

    List<Review> findByCustomerId(Long customerId);

    Optional<Review> findByBookingId(Long bookingId);
}