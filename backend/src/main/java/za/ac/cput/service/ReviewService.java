package za.ac.cput.service;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Booking;
import za.ac.cput.domain.Review;
import za.ac.cput.domain.enums.BookingStatus;
import za.ac.cput.repository.BookingRepository;
import za.ac.cput.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService extends BaseService<Review> {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository) {

        super(reviewRepository);

        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    // =========================================================
    // GET REVIEWS FOR A FUNDI
    // =========================================================

    public List<Review> getByFundiProfileId(Long fundiProfileId) {
        return reviewRepository.findByFundiProfileId(fundiProfileId);
    }

    // =========================================================
    // GET REVIEWS BY CUSTOMER
    // =========================================================

    public List<Review> getByCustomerId(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    // =========================================================
    // GET REVIEW FOR A BOOKING
    // =========================================================

    public Optional<Review> getByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }

    // =========================================================
    // CREATE REVIEW
    // =========================================================

    public Review createReview(
            Long bookingId,
            Long customerId,
            Integer rating,
            String comment) {

        // -----------------------------------------------------
        // Validate rating
        // -----------------------------------------------------

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }

        // -----------------------------------------------------
        // Find booking
        // -----------------------------------------------------

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found"
                        )
                );

        // -----------------------------------------------------
        // Booking must be completed
        // -----------------------------------------------------

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed bookings can be reviewed"
            );
        }

        // -----------------------------------------------------
        // Verify customer owns the booking
        // -----------------------------------------------------

        if (booking.getCustomer() == null
                || !booking.getCustomer().getId().equals(customerId)) {

            throw new IllegalArgumentException(
                    "You can only review your own booking"
            );
        }

        // -----------------------------------------------------
        // Prevent duplicate review
        // -----------------------------------------------------

        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException(
                    "This booking has already been reviewed"
            );
        }

        // -----------------------------------------------------
        // Create review using booking relationships
        // -----------------------------------------------------

        Review review = Review.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .fundiProfile(booking.getFundiProfile())
                .rating(rating)
                .comment(comment)
                .build();

        return reviewRepository.save(review);
    }
}