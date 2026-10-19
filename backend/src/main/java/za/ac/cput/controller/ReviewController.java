package za.ac.cput.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Review;
import za.ac.cput.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController extends BaseController<Review> {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @Override
    protected Object getService() {
        return service;
    }

    @Override
    protected Review createEntity(Review entity) {
        return service.create(entity);
    }

    @Override
    protected Review updateEntity(Review entity) {
        return service.update(entity);
    }

    @Override
    protected Review getEntity(Long id) {
        return service.getById(id);
    }

    @Override
    protected List<Review> getEntities() {
        return service.getAll();
    }

    @Override
    protected void deleteEntity(Long id) {
        service.delete(id);
    }

    // =========================================================
    // GET REVIEWS FOR A FUNDI
    // =========================================================

    @GetMapping("/fundi/{fundiProfileId}")
    public ResponseEntity<List<Review>> getByFundiProfile(
            @PathVariable Long fundiProfileId) {

        return ResponseEntity.ok(
                service.getByFundiProfileId(fundiProfileId)
        );
    }

    // =========================================================
    // GET REVIEWS BY CUSTOMER
    // =========================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Review>> getByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                service.getByCustomerId(customerId)
        );
    }

    // =========================================================
    // GET REVIEW FOR A BOOKING
    // =========================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Review> getByBooking(
            @PathVariable Long bookingId) {

        return service.getByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================================================
    // CREATE REVIEW FOR COMPLETED BOOKING
    // =========================================================

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<Review> createReview(
            @PathVariable Long bookingId,
            @RequestParam Long customerId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {

        Review review = service.createReview(
                bookingId,
                customerId,
                rating,
                comment
        );

        return ResponseEntity.ok(review);
    }
}