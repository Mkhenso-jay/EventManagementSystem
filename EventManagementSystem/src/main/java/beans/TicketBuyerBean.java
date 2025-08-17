package beans;

import entity.model.Event;
import entity.model.Ticket;
import entity.model.TicketBuyer;
import generator.TicketPdfGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import repositories.TicketBuyerRepository;
import repositories.TicketRepository;
import repositories.EventRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@SessionScoped
public class TicketBuyerBean implements Serializable {

    @Inject
    private TicketBuyerRepository ticketBuyerRepository;

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private LoginBean loginBean;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private PaymentBean paymentBean; 

    private List<Ticket> availableTickets;
    private Ticket selectedTicket;
    private List<Event> availableEvents;
    private Map<Long, Integer> purchaseQuantities = new HashMap<>();
    private Map<Long, Double> totalAmounts = new HashMap<>();
    private TicketBuyer currentBuyer;

    @PostConstruct
    public void init() {
        this.availableTickets = ticketRepository.findAvailableTickets();
        this.availableEvents = eventRepository.findEventsWithAvailableTickets();
    }

    @Transactional
    public void registerTicketBuyer(TicketBuyer ticketBuyer) {
        ticketBuyerRepository.save(ticketBuyer);
    }

    public boolean validateLogin(String email, String password) {
        TicketBuyer ticketBuyer = ticketBuyerRepository.findByEmail(email);
        return ticketBuyer != null && ticketBuyer.getPassword().equals(password);
    }

    public List<Ticket> getAvailableTickets() {
        return availableTickets;
    }

    public Ticket getSelectedTicket() {
        return selectedTicket;
    }

    public void setSelectedTicket(Ticket selectedTicket) {
        this.selectedTicket = selectedTicket;
    }

    @Transactional
    public void downloadTicketsAsPdf() {
        try {
            List<Ticket> myTickets = getMyTickets(); 
            if (myTickets.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No tickets to download.", null));
                return;
            }

            // Generate the PDF
            ByteArrayOutputStream pdfStream = TicketPdfGenerator.generateTicketPdf(myTickets);

            // Set the response to prompt a download
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=tickets.pdf");
            response.setContentLength(pdfStream.size());

            // Write the PDF content to the response output stream
            response.getOutputStream().write(pdfStream.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();

            // Inform JSF that the response is complete
            facesContext.responseComplete();
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating PDF.", null));
        }
    }

    @Transactional
    public String purchase(Long eventId) {
        TicketBuyer buyer = ticketBuyerRepository.findByEmail(loginBean.getEmail());
        if (buyer == null) {
            return null;
        }

        Event ev = eventRepository.findById(eventId);
        Integer quantity = purchaseQuantities.get(eventId);

        if (quantity == null || quantity <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Quantity must be at least 1", null));
            return null;
        }

        if (ev.getTicketsAvailable() < quantity) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not enough tickets available", null));
            return null;
        }

        List<Ticket> newTickets = new ArrayList<>();
        double total = 0.0;

        // Create and assign multiple tickets
        for (int i = 0; i < quantity; i++) {
            Ticket newTicket = new Ticket();
            newTicket.setEvent(ev);
            newTicket.setTicketPrice(ev.getTicketPrice());
            newTicket.setTicketStatus("SOLD");
            newTicket.setTicketBuyer(buyer);

            ticketRepository.save(newTicket);
            newTickets.add(newTicket);
            total += ev.getTicketPrice();
        }

        // Update Event
        ev.setTicketsAvailable(ev.getTicketsAvailable() - quantity);
        ev.setTicketsSold(ev.getTicketsSold() + quantity);
        eventRepository.update(ev);

        // Persist Buyer if tickets added manually
        ticketBuyerRepository.update(buyer);

        // Refresh ticket list
        availableTickets = ticketRepository.findAvailableTickets();

        // Hand off to the PaymentBean
        paymentBean.setBuyer(buyer);
        paymentBean.initialize(ev, newTickets, total);

        return "payment-success.xhtml?faces-redirect=true";
    }

    public List<Event> getAvailableEvents() {
        return availableEvents;
    }

    public Map<Long, Integer> getPurchaseQuantities() {
        return purchaseQuantities;
    }

    public void setPurchaseQuantities(Map<Long, Integer> purchaseQuantities) {
        this.purchaseQuantities = purchaseQuantities;
    }

    public int getPurchaseQuantity(Long eventId) {
        return purchaseQuantities.getOrDefault(eventId, 0);
    }

    public void setPurchaseQuantity(Long eventId, int quantity) {
        purchaseQuantities.put(eventId, quantity);
        updateAmount(eventId, eventRepository.findById(eventId).getTicketPrice()); 
    }


    public void updateAmount(Long eventId, Double ticketPrice) {
        Integer quantity = purchaseQuantities.get(eventId);
        if (quantity != null && quantity > 0) {
            totalAmounts.put(eventId, quantity * ticketPrice);
        } else {
            totalAmounts.put(eventId, 0.0);
        }
    }

    public Map<Long, Double> getTotalAmount() {
        return totalAmounts;
    }

    public TicketBuyer findByEmail(String email) {
        return ticketBuyerRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        TicketBuyer existing = ticketBuyerRepository.findByEmail(email);
        return existing != null;
    }

    public List<Ticket> getMyTickets() {
        if (currentBuyer != null) {
            return ticketRepository.findByTicketBuyer(currentBuyer);
        }
        return Collections.emptyList();
    }

    public TicketBuyer getCurrentBuyer() {
        return currentBuyer;
    }

    public void setCurrentBuyer(TicketBuyer currentBuyer) {
        this.currentBuyer = currentBuyer;
    }

}
