package beans;

import entity.model.Event;
import entity.model.EventOrganizer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import repositories.EventOrganizerRepository;
import repositories.EventRepository;

@Named
@RequestScoped
public class EventOrganizerBean {

    @Inject
    private EventOrganizerRepository eventOrganizerRepository;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private LoginBean loginBean;

    private Event currentEvent = new Event();
    private List<Event> events;
    private Part uploadedImage;

    @PostConstruct
    public void init() {
        loadEvents();
    }

    public void loadEvents() {
        Long organizerId = getLoggedInEventOrganizerId();
        if (organizerId != null) {
   
            events = eventRepository.findByOrganizerId(organizerId);
        }
    }

    private Long getLoggedInEventOrganizerId() {
        String email = getLoggedInEmail();
        EventOrganizer organizer = eventOrganizerRepository.findByEmail(email);
        return (organizer != null) ? organizer.getId() : null;
    }

    private String getLoggedInEmail() {
        return loginBean.getEmail();
    }

    @Transactional
    public String saveEventAction() {
        EventOrganizer organizer = eventOrganizerRepository.findByEmail(getLoggedInEmail());
        currentEvent.setEventOrganizer(organizer);
        eventRepository.save(currentEvent);

        if (uploadedImage != null) {
            saveFile(uploadedImage, currentEvent.getId());
        }

        loadEvents();
        currentEvent = new Event();
        return "manageEvents.xhtml?faces-redirect=true";
    }

    private void saveFile(Part file, Long eventId) {
        try (InputStream input = file.getInputStream()) {
            File uploadDir = new File("C:/event-images/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            File savedFile = new File(uploadDir, eventId + ".jpg");
            Files.copy(input, savedFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public String deleteEventAction(Event event) {
        eventRepository.delete(event);
        deleteFile(event.getId());
        loadEvents();
        return "manageEvents.xhtml?faces-redirect=true";
    }

    private void deleteFile(Long eventId) {
        File file = new File("C:/event-images/" + eventId + ".jpg");
        if (file.exists()) {
            file.delete();
        }
    }

    public void editEvent(Event event) {
        this.currentEvent = event;
    }

    public String resetCurrentEvent() {
        this.currentEvent = new Event();
        return null;
    }

    @Transactional
    public void registerEventOrganizer(EventOrganizer eventOrganizer) {
        eventOrganizerRepository.save(eventOrganizer);
    }

    public boolean validateLogin(String email, String password) {
        EventOrganizer eventOrganizer = eventOrganizerRepository.findByEmail(email);
        return eventOrganizer != null && eventOrganizer.getPassword().equals(password);
    }

    public EventOrganizer findByEmail(String email) {
        return eventOrganizerRepository.findByEmail(email);
    }

    public List<Event> getEvents() {
        if (events == null) {
            loadEvents();
        }
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Part getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(Part uploadedImage) {
        this.uploadedImage = uploadedImage;
    }
    
    public boolean emailExists(String email) {
    EventOrganizer existing = eventOrganizerRepository.findByEmail(email);
    return existing != null;
}

    
}
