package beans;

import entity.model.Event;
import entity.model.Ticket;
import entity.model.TicketBuyer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
@RequestScoped
public class PaymentBean implements Serializable {

    private Long eventId;
    private String buyerEmail;

    private TicketBuyer buyer;
    private Event event;
    private List<Ticket> purchasedTickets;
    private double totalAmount;
    
    @Inject
    private TicketBean ticketBean;

    @Inject
    private TicketBuyerBean ticketBuyerBean;

    @Inject
    private EventBean eventBean;

    @PostConstruct
    public void init() {
        try {
            Map<String, String> params = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequestParameterMap();

            if (params.containsKey("eventId")) {
                try {
                    this.eventId = Long.parseLong(params.get("eventId"));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid eventId format: " + params.get("eventId"));
                    return;
                }
            }

            if (params.containsKey("buyerEmail")) {
                this.buyerEmail = params.get("buyerEmail");
            }

            if (eventId == null || buyerEmail == null || buyerEmail.isEmpty()) {
                System.err.println("Missing eventId or buyerEmail in request parameters.");
                return;
            }

            this.event = eventBean.findById(eventId);
            if (this.event == null) {
                System.err.println("Event not found for ID: " + eventId);
                return;
            }

            this.buyer = ticketBuyerBean.findByEmail(buyerEmail);
            if (this.buyer == null) {
                System.err.println("Buyer not found for email: " + buyerEmail);
                return;
            }

            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setTicketBuyer(buyer);
            ticket.setTicketStatus("SOLD");
            ticket.setTicketPrice(event.getTicketPrice());

            this.purchasedTickets = Collections.singletonList(ticket);
            this.totalAmount = ticket.getTicketPrice();
               finalizePurchase();

        } catch (Exception ex) {
            ex.printStackTrace(); 
        }
    }

    public void initialize(Event event, List<Ticket> tickets, double total) {
        this.event = event;
        this.purchasedTickets = tickets;
        this.totalAmount = total;
        System.out.println("Payment details initialized: Event - " + event.getEventName() + ", Total - " + total);
    }

    public void initialize(ComponentSystemEvent event) {
        try {
            System.out.println("Initializing payment page...");
        } catch (Exception e) {
            e.printStackTrace();  
        }
    }

    
public void finalizePurchase() {
    try {
        if (event.getTicketsAvailable() > 0) {
            // Reduce ticket availability
            int currentAvailable = event.getTicketsAvailable();
            event.setTicketsAvailable(currentAvailable - 1);

            // Create and configure ticket
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setTicketBuyer(buyer);
            ticket.setTicketStatus("SOLD");
            ticket.setTicketPrice(event.getTicketPrice());

            // Add ticket to event and recalculate tickets sold
            event.getTickets().add(ticket);
            event.updateTicketsSold();

            // Persist changes
            ticketBean.save(ticket);     
            eventBean.update(event);     
            
            // Set purchase data
            this.purchasedTickets = Collections.singletonList(ticket);
            this.totalAmount = ticket.getTicketPrice();
        } else {
            System.err.println("No tickets available for this event.");
       
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // --- Getters and Setters ---
    public TicketBuyer getBuyer() {
        return buyer;
    }

    public void setBuyer(TicketBuyer buyer) {
        this.buyer = buyer;
    }

    public Event getEvent() {
        return event;
    }

    public List<Ticket> getPurchasedTickets() {
        return purchasedTickets;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
