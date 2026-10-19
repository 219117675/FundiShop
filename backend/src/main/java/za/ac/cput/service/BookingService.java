package za.ac.cput.service;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Booking;
import za.ac.cput.domain.FundiProfile;
import za.ac.cput.domain.enums.BookingStatus;
import za.ac.cput.domain.enums.VerificationStatus;
import za.ac.cput.repository.BookingRepository;
import za.ac.cput.repository.FundiProfileRepository;
import za.ac.cput.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService extends BaseService<Booking> {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FundiProfileRepository fundiProfileRepository;
    private final NotificationService notificationService;

    public BookingService(
            BookingRepository repository,
            UserRepository userRepository,
            FundiProfileRepository fundiProfileRepository,
            NotificationService notificationService) {

        super(repository);

        this.bookingRepository = repository;
        this.userRepository = userRepository;
        this.fundiProfileRepository = fundiProfileRepository;
        this.notificationService = notificationService;
    }

    /* =========================================================
       CREATE BOOKING
    ========================================================= */

    public Booking createBooking(
            Long customerId,
            Long fundiProfileId,
            LocalDate scheduledDate,
            LocalTime scheduledTime,
            String serviceRequired,
            String jobLocation,
            BigDecimal estimatedBudget,
            String jobDescription) {

        var customer = userRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer account not found."
                        )
                );

        FundiProfile fundiProfile =
                fundiProfileRepository
                        .findById(fundiProfileId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Fundi profile not found."
                                )
                        );

        if (fundiProfile.getVerificationStatus()
                != VerificationStatus.APPROVED) {

            throw new RuntimeException(
                    "This Fundi must be approved before bookings can be made."
            );
        }

        if (fundiProfile.getUser() == null) {
            throw new RuntimeException(
                    "This Fundi profile is not linked to a user account."
            );
        }

        if (fundiProfile.getUser().getId() == null) {
            throw new RuntimeException(
                    "The Fundi user account does not have a valid ID."
            );
        }

        if (scheduledDate == null) {
            throw new RuntimeException(
                    "Scheduled date is required."
            );
        }

        if (scheduledTime == null) {
            throw new RuntimeException(
                    "Scheduled time is required."
            );
        }

        if (serviceRequired == null ||
                serviceRequired.isBlank()) {

            throw new RuntimeException(
                    "Service required is missing."
            );
        }

        if (jobLocation == null ||
                jobLocation.isBlank()) {

            throw new RuntimeException(
                    "Job location is required."
            );
        }

        if (jobDescription == null ||
                jobDescription.isBlank()) {

            throw new RuntimeException(
                    "Job description is required."
            );
        }

        /* =====================================================
           CREATE BOOKING
        ===================================================== */

        Booking booking = Booking.builder()
                .customer(customer)
                .fundiProfile(fundiProfile)
                .scheduledDate(scheduledDate)
                .scheduledTime(scheduledTime)
                .serviceRequired(serviceRequired)
                .jobLocation(jobLocation)
                .estimatedBudget(estimatedBudget)
                .jobDescription(jobDescription)
                .status(BookingStatus.REQUESTED)
                .calloutFee(fundiProfile.getCalloutFee())
                .build();

        Booking savedBooking =
                bookingRepository.save(booking);

        /* =====================================================
           NOTIFY FUNDI
        ===================================================== */

        Long fundiUserId =
                fundiProfile.getUser().getId();

        notificationService.createNotification(
                fundiUserId,
                "You have received a new booking request for "
                        + serviceRequired
                        + ".",
                "BOOKING_CREATED"
        );

        return savedBooking;
    }

    /* =========================================================
       GET FUNDI BOOKINGS
    ========================================================= */

    public List<Booking> getBookingsByFundi(
            Long fundiProfileId) {

        return bookingRepository
                .findByFundiProfileIdOrderByCreatedAtDesc(
                        fundiProfileId
                );
    }

    /* =========================================================
       GET CUSTOMER BOOKINGS
    ========================================================= */

    public List<Booking> getBookingsByCustomer(
            Long customerId) {

        return bookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                );
    }

    /* =========================================================
       UPDATE BOOKING STATUS
    ========================================================= */

    public Booking updateStatus(
            Long bookingId,
            BookingStatus status) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found."
                        )
                );

        if (status == null) {
            throw new RuntimeException(
                    "Booking status is required."
            );
        }

        if (booking.getCustomer() == null) {
            throw new RuntimeException(
                    "Booking is not linked to a homeowner."
            );
        }

        if (booking.getCustomer().getId() == null) {
            throw new RuntimeException(
                    "Homeowner account does not have a valid ID."
            );
        }

        BookingStatus previousStatus =
                booking.getStatus();

        booking.setStatus(status);

        Booking savedBooking =
                bookingRepository.save(booking);

        /* =====================================================
           NOTIFY HOMEOWNER
        ===================================================== */

        Long customerId =
                booking.getCustomer().getId();

        if (status == BookingStatus.ACCEPTED) {

            if (previousStatus ==
                    BookingStatus.RESCHEDULED) {

                notificationService.createNotification(
                        customerId,
                        "Your rescheduled booking has been accepted by the Fundi.",
                        "RESCHEDULE_ACCEPTED"
                );

            } else {

                notificationService.createNotification(
                        customerId,
                        "Your booking for "
                                + booking.getServiceRequired()
                                + " has been accepted by the Fundi.",
                        "BOOKING_ACCEPTED"
                );
            }

        } else if (status == BookingStatus.DECLINED) {

            if (previousStatus ==
                    BookingStatus.RESCHEDULED) {

                notificationService.createNotification(
                        customerId,
                        "Your rescheduled booking has been declined by the Fundi.",
                        "RESCHEDULE_DECLINED"
                );

            } else {

                notificationService.createNotification(
                        customerId,
                        "Your booking for "
                                + booking.getServiceRequired()
                                + " has been declined by the Fundi.",
                        "BOOKING_DECLINED"
                );
            }

        } else if (status == BookingStatus.COMPLETED) {

            notificationService.createNotification(
                    customerId,
                    "Your "
                            + booking.getServiceRequired()
                            + " booking has been marked as completed. "
                            + "You can now leave a review and rating.",
                    "BOOKING_COMPLETED"
            );
        }

        return savedBooking;
    }

    /* =========================================================
       RESCHEDULE BOOKING

       REQUESTED
           → remains REQUESTED

       ACCEPTED
           → becomes RESCHEDULED

       RESCHEDULED
           → remains RESCHEDULED
    ========================================================= */

    public Booking rescheduleBooking(
            Long bookingId,
            LocalDate newDate,
            LocalTime newTime) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found."
                        )
                );

        if (newDate == null) {
            throw new RuntimeException(
                    "New scheduled date is required."
            );
        }

        if (newTime == null) {
            throw new RuntimeException(
                    "New scheduled time is required."
            );
        }

        if (booking.getFundiProfile() == null) {
            throw new RuntimeException(
                    "Booking is not linked to a Fundi profile."
            );
        }

        if (booking.getFundiProfile().getUser() == null) {
            throw new RuntimeException(
                    "Fundi profile is not linked to a user account."
            );
        }

        if (booking.getFundiProfile()
                .getUser()
                .getId() == null) {

            throw new RuntimeException(
                    "Fundi user account does not have a valid ID."
            );
        }

        BookingStatus previousStatus =
                booking.getStatus();

        booking.setScheduledDate(newDate);
        booking.setScheduledTime(newTime);

        /* =====================================================
           REQUESTED
        ===================================================== */

        if (booking.getStatus() ==
                BookingStatus.REQUESTED) {

            booking.setStatus(
                    BookingStatus.REQUESTED
            );

        }

        /* =====================================================
           ACCEPTED
        ===================================================== */

        else if (booking.getStatus() ==
                BookingStatus.ACCEPTED) {

            booking.setStatus(
                    BookingStatus.RESCHEDULED
            );

        }

        /* =====================================================
           RESCHEDULED
        ===================================================== */

        else if (booking.getStatus() ==
                BookingStatus.RESCHEDULED) {

            booking.setStatus(
                    BookingStatus.RESCHEDULED
            );

        }

        /* =====================================================
           INVALID STATUS
        ===================================================== */

        else {

            throw new RuntimeException(
                    "This booking cannot be rescheduled because its current status is "
                            + booking.getStatus()
            );
        }

        Booking savedBooking =
                bookingRepository.save(booking);

        /* =====================================================
           NOTIFY FUNDI
        ===================================================== */

        Long fundiUserId =
                booking.getFundiProfile()
                        .getUser()
                        .getId();

        String message;

        if (previousStatus ==
                BookingStatus.REQUESTED) {

            message =
                    "A homeowner has changed the date/time "
                            + "of a booking request for "
                            + booking.getServiceRequired()
                            + ".";

        } else {

            message =
                    "A homeowner has requested a new date/time "
                            + "for the accepted "
                            + booking.getServiceRequired()
                            + " booking.";
        }

        notificationService.createNotification(
                fundiUserId,
                message,
                "BOOKING_RESCHEDULED"
        );

        return savedBooking;
    }
}