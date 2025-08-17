package entity.model;

import converter.LocalDateAttributeConverter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;
    private String eventDescription;

    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate eventDate;

    private double ticketPrice;
    private int ticketsAvailable;
    private int ticketsSold;

    @Lob
    @Column(name = "event_image", columnDefinition = "LONGBLOB")
    private byte[] eventImage;

    public byte[] getEventImage() {
        return eventImage;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
    
    @ManyToOne
    @JoinColumn(name = "venue_owner_id")
    private VenueOwner venueOwner;

    @ManyToOne
    @JoinColumn(name = "event_organizer_id")
    private EventOrganizer eventOrganizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    // --- Getters and Setters ---
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getTicketsAvailable() {
        return ticketsAvailable;
    }

    public void setTicketsAvailable(int ticketsAvailable) {
        this.ticketsAvailable = ticketsAvailable;
    }

    public int getTicketsSold() {
        return (int) tickets.stream()
                .filter(ticket -> "SOLD".equalsIgnoreCase(ticket.getTicketStatus()))
                .count();
    }

    public void setTicketsSold(int ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setEvent(this);
        ticketsAvailable++;
    }

    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
        ticket.setEvent(null);
        ticketsAvailable--;
    }

    // Method to update ticket sold count when a ticket's status changes
    public void updateTicketsSold() {
        ticketsSold = (int) tickets.stream()
                .filter(ticket -> "SOLD".equalsIgnoreCase(ticket.getTicketStatus()))
                .count();
    }

    public void setEventImage(byte[] eventImage) {
        this.eventImage = eventImage;
    }

    public VenueOwner getVenueOwner() {
        return venueOwner;
    }

    public void setVenueOwner(VenueOwner venueOwner) {
        this.venueOwner = venueOwner;
    }
    
    
}
