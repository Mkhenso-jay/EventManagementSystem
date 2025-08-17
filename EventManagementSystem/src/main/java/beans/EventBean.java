package beans;

import entity.model.Category;
import entity.model.Event;
import entity.model.Ticket;
import entity.model.Venue;
import entity.model.EventOrganizer;
import entity.model.Message;
import entity.model.UserAccount;
import entity.model.VenueBooking;
import entity.model.VenueOwner;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Base64;
import repositories.BookingRepository;
import repositories.EventRepository;
import repositories.VenueRepository;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Date;
import services.ChatService;
import services.SessionManager;

@Named

@ViewScoped
public class EventBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    private Event event = new Event();
    private List<Event> events;
    private List<Venue> availableVenues;
    private Event selectedEvent;
    private Venue selectedVenue;
    private Long venueId;
    private Part uploadedImage;
    private VenueOwner venueOwner;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Inject
    private LoginBean loginBean;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private BookingRepository bookingRepository;

    @Inject
    private VenueRepository venueRepository;
    @Inject
    private ChatService chatService;
    @Inject
    private SessionManager sessionManager;

    @PostConstruct
    public void init() {
        EventOrganizer currentOrganizer = loginBean.getLoggedInOrganizer();
        if (currentOrganizer != null) {
            events = em.createQuery("SELECT e FROM Event e WHERE e.eventOrganizer.id = :organizerId", Event.class)
                    .setParameter("organizerId", currentOrganizer.getId())
                    .getResultList();
        }
        availableVenues = em.createQuery("SELECT v FROM Venue v", Venue.class).getResultList();
    }

    public Category[] getCategories() {
        return Category.values();
    }

    @Transactional
    public String createEvent() {
        EventOrganizer currentOrganizer = loginBean.getLoggedInOrganizer();

        if (venueId != null) {
            Venue venue = em.find(Venue.class, venueId);
            event.setVenue(venue);
            event.setEventOrganizer(currentOrganizer);

            // ✅ Store the venue owner in the class-level field for chat use
            VenueOwner venueOwner = venue.getVenueOwner();
            event.setVenueOwner(venueOwner);

            VenueBooking booking = new VenueBooking();
            booking.setVenue(event.getVenue());
            booking.setOrganizer(event.getEventOrganizer());
            booking.setStartDateTime(startDateTime);
            booking.setEndDateTime(endDateTime);
            booking.setStatus("CONFIRMED");

            booking.setTotalCost(venue.getPrice());
            bookingRepository.save(booking);
        }

        event.setEventOrganizer(currentOrganizer);

        // Handle image upload
        if (uploadedImage != null) {
            try (InputStream is = uploadedImage.getInputStream()) {
                byte[] imageBytes = is.readAllBytes();
                event.setEventImage(imageBytes);

                String uploadDir = "C:\\Users\\DevSpark Studios\\uploads";
                File uploads = new File(uploadDir);
                if (!uploads.exists()) {
                    uploads.mkdirs();
                }
                String fileName = "event_" + System.currentTimeMillis() + ".jpg";
                File uploadedFile = new File(uploads, fileName);
                Files.write(uploadedFile.toPath(), imageBytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        em.persist(event);

        int count = event.getTicketsAvailable();
        List<Ticket> ticketList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setTicketPrice(event.getTicketPrice());
            ticket.setTicketStatus("AVAILABLE");
            em.persist(ticket);
            ticketList.add(ticket);
        }
        event.setTickets(ticketList);

        em.merge(event);

        init();
        event = new Event();
        venueId = null;
        uploadedImage = null;

        return "homeEventOrganizer.xhtml?faces-redirect=true";
    }

    public VenueOwner getVenueOwnerForChat() {
        return venueOwner;
    }

    public String getVenueOwnerDetails() {
        if (venueOwner != null) {
            Long id = venueOwner.getId();
            String name = venueOwner.getFirstName();
            String surname = venueOwner.getLastName();  // Assuming you have a 'surname' field in VenueOwner

            return id + ", " + name + ", " + surname; // Returning them as a formatted string
        } else {
            return "Venue Owner not available";
        }
    }

    @Transactional
    public String deleteEvent(Event ev) {
        Event toRemove = em.find(Event.class, ev.getId());
        if (toRemove != null) {
            em.remove(toRemove);
        }
        init();
        return null;
    }

    @Transactional
    public String bookVenue() {
        if (selectedEvent != null && selectedVenue != null) {
            selectedEvent.setVenue(selectedVenue);
            em.merge(selectedEvent);
            init();
        }
        return "homeEventOrganizer.xhtml?faces-redirect=true";
    }
    private List<VenueOwner> venueOwnerList = new ArrayList<>();

    /**
     *
     * @return
     */
    public List<VenueOwner> getVenueOwnerList() {
        EventOrganizer currentOrganizer = loginBean.getLoggedInOrganizer();
        if (currentOrganizer == null) {
            return new ArrayList<>();
        }

        return em.createQuery(
                "SELECT DISTINCT v.venueOwner FROM VenueBooking vb JOIN vb.venue v WHERE vb.organizer.id = :organizerId",
                VenueOwner.class
        )
                .setParameter("organizerId", currentOrganizer.getId())
                .getResultList();
    }

    public void setVenueOwnerList(List<VenueOwner> venueOwnerList) {
        this.venueOwnerList = venueOwnerList;
    }

    @Transactional
    public void saveTicket(Ticket ticket) {
        em.persist(ticket);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event update(Event event) {
        return eventRepository.update(event);
    }

    // Getters and Setters
    public Long getVenueId() {
        return venueId;
    }

    public String getImageUrl(Long eventId) {
        return "/imageServlet?eventId=" + eventId;
    }

    public String getBase64Image(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "";
        }
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    public byte[] getEventImage(Long eventId) {
        return eventRepository.findEventImageById(eventId);
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Venue> getAvailableVenues() {
        return availableVenues;
    }

    public void setAvailableVenues(List<Venue> availableVenues) {
        this.availableVenues = availableVenues;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public Venue getSelectedVenue() {
        return selectedVenue;
    }

    public void setSelectedVenue(Venue selectedVenue) {
        this.selectedVenue = selectedVenue;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAllEvents();
    }

    public Part getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(Part uploadedImage) {
        this.uploadedImage = uploadedImage;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }
    // chat state
    private Long selectedVenueOwnerId;
    private String chatContent;
    private List<Message> chatMessages = new ArrayList<>();

    /**
     * called by AJAX when you pick a new owner
     */
    public void loadChat() {
        if (selectedVenueOwnerId != null) {
            UserAccount me = sessionManager.getCurrentUser();
            // we assume receiver id == venueOwner.id
            UserAccount receiver = new UserAccount(selectedVenueOwnerId);
            chatMessages = chatService.getMessagesBetweenUsers(me, receiver);
        }
    }

    /**
     * called by the Send button
     */
    public void sendChatToVenueOwner() {
        UserAccount me = sessionManager.getCurrentUser();
        UserAccount receiver = new UserAccount(selectedVenueOwnerId);
        if (receiver != null && chatContent != null && !chatContent.isBlank()) {
            Message m = new Message();
            m.setSender(me);
            m.setReceiver(receiver);
            m.setContent(chatContent);
            m.setTimestamp(new Date());
            chatService.saveMessage(m);
            chatContent = "";
            loadChat();
        }
    }

// getters & setters for new fields
    public Long getSelectedVenueOwnerId() {
        return selectedVenueOwnerId;
    }

    public void setSelectedVenueOwnerId(Long id) {
        selectedVenueOwnerId = id;
    }

    public String getChatContent() {
        return chatContent;
    }

    public void setChatContent(String c) {
        chatContent = c;
    }

    public List<Message> getChatMessages() {
        return chatMessages;
    }

   

}
