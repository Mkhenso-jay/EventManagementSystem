package entity.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String venueName;
    private String venueDescription;
    private String location;
    private int capacity;
    private double price;
    private boolean available;

    @ManyToOne
    @JoinColumn(name = "venue_owner_id")
    private VenueOwner venueOwner;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<VenueBooking> bookings;

    @OneToMany(mappedBy = "venue")
    private List<Event> events;

    @Lob
    @Column(name = "venue_image", columnDefinition = "LONGBLOB")
    private byte[] venueImage;

    public byte[] getVenueImage() {
        return venueImage;
    }

    public void setVenueImage(byte[] eventImage) {
        this.venueImage = eventImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueDescription() {
        return venueDescription;
    }

    public void setVenueDescription(String venueDescription) {
        this.venueDescription = venueDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public VenueOwner getVenueOwner() {
        return venueOwner;
    }

    public void setVenueOwner(VenueOwner venueOwner) {
        this.venueOwner = venueOwner;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getBase64Image() {
        if (venueImage != null) {
            return "data:image/*;base64," + java.util.Base64.getEncoder().encodeToString(venueImage);
        }
        return "placeholder-image-url"; // fallback
    }

    public List<VenueBooking> getBookings() {
        return bookings;
    }

    public void setBookings(List<VenueBooking> bookings) {
        this.bookings = bookings;
    }
    
    

}
