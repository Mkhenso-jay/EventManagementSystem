/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import entity.model.Category;
import entity.model.VenueBooking;
import entity.model.VenueOwner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import repositories.BookingRepository;

@Named
@RequestScoped
public class BookingBean implements Serializable {

    private int totalBookings;
    private double occupancyRate;
    private double estimatedRevenue;
    private Map<DayOfWeek, Double> bookingDistribution;
    private Map<Category, Double> eventTypeDistribution; // Was Map<String, Double>

    @Inject
    private VenueOwnerBean venueOwnerBean;

    @Inject
    private BookingRepository bookingRepository;

    private List<VenueBooking> bookings;

    private Map<LocalDate, List<VenueBooking>> bookingsByDate;

    @PostConstruct
    public void init() {

        VenueOwner owner = venueOwnerBean.getLoggedInVenueOwner();

        bookings = (owner != null)
                ? bookingRepository.findByOwner(owner)
                : List.of();

        groupBookingsByDate();
        calculateInsights();
    }

    private void groupBookingsByDate() {
        bookingsByDate = new HashMap<>();
        for (VenueBooking booking : bookings) {
            LocalDate date = booking.getStartDateTime().toLocalDate();
            bookingsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(booking);
        }
    }

    private void calculateInsights() {
        // Monthly Overview
        this.totalBookings = bookings.size();
        this.estimatedRevenue = bookings.stream()
                .mapToDouble(VenueBooking::getTotalCost)
                .sum();

        // Occupancy Rate (example: bookings vs total available days)
        int totalDaysInMonth = YearMonth.now().lengthOfMonth();
        long bookedDays = bookings.stream()
                .map(b -> b.getStartDateTime().toLocalDate())
                .distinct()
                .count();
        this.occupancyRate = (double) bookedDays / totalDaysInMonth * 100;

        // Booking Distribution by Day of Week
        this.bookingDistribution = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStartDateTime().getDayOfWeek(),
                        Collectors.summingDouble(b -> 1)
                ));
        normalizeDistribution(bookingDistribution);

        // Event Type Distribution (assuming Event is linked to VenueBooking)
        this.eventTypeDistribution = bookings.stream()
                .filter(b -> b.getVenue() != null && b.getVenue().getEvents() != null) // Check for null venue
                .flatMap(b -> b.getVenue().getEvents().stream())
                .filter(e -> e.getCategory() != null) // Ensure category is not null
                .collect(Collectors.groupingBy(
                        e -> e.getCategory(), // Use Category enum as key, not String
                        Collectors.summingDouble(e -> 1)
                ));
        normalizeDistribution(eventTypeDistribution);
    }

    public String categoryIcon(String categoryName) {
        try {
            return Category.valueOf(categoryName).getIconClass();
        } catch (IllegalArgumentException | NullPointerException e) {
            return "fa-calendar"; // fallback icon
        }
    }

    private void normalizeDistribution(Map<?, Double> distribution) {
        double total = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        distribution.replaceAll((k, v) -> (v / total) * 100);
    }

    public List<VenueBooking> getBookings() {
        return bookings;
    }

    public Map<LocalDate, List<VenueBooking>> getBookingsByDate() {
        return bookingsByDate;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public double getEstimatedRevenue() {
        return estimatedRevenue;
    }

    public Map<DayOfWeek, Double> getBookingDistribution() {
        return bookingDistribution;
    }

    public Map<Category, Double> getEventTypeDistribution() {
        return eventTypeDistribution;
    }
}
    