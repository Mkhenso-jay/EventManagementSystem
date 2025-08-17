package api;

import entity.model.Event;
import entity.model.Ticket;
import entity.model.TicketBuyer;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import repositories.EventRepository;
import repositories.TicketRepository;
import repositories.TicketBuyerRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScoped
@Path("/payment/notify")
public class PayFastIPNResource {

    private static final String MF_MERCHANT_ID = "bpa8pw28r348u";
    private static final String MF_MERCHANT_KEY = "bpa8pw28r348u";

    private static final String PF_VALIDATION_URL
            = "https://sandbox.payfast.co.za/eng/query/validate";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private EventRepository eventRepo;

    @Inject
    private TicketRepository ticketRepo;

    @Inject
    private TicketBuyerRepository buyerRepo;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response receiveIPN(Map<String, String> formParams) throws Exception {

        String data = formParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        URL url = new URL(PF_VALIDATION_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.getOutputStream().write(data.getBytes("UTF-8"));

        int status = con.getResponseCode();
        if (status != 200) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Validation failed").build();
        }

        // Read response body
        String validationResponse;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            validationResponse = in.lines().collect(Collectors.joining("\n"));
        }
        con.disconnect();

        // 3) Check validation string
        if (!"VALID".equals(validationResponse.trim())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("IPN Validation failed: " + validationResponse)
                    .build();
        }

        // 4) Process the payment
        String paymentStatus = formParams.get("payment_status");
        String pfPaymentId = formParams.get("pf_payment_id");
        String mPaymentId = formParams.get("m_payment_id");
        String buyerEmail = formParams.get("email_address");
        String amountStr = formParams.get("amount_gross");
        String itemName = formParams.get("item_name");

        // Only process completed payments
        if (!"COMPLETE".equals(paymentStatus)) {
            return Response.ok("Ignored status: " + paymentStatus).build();
        }

        BigDecimal amount = new BigDecimal(amountStr);

        // 5) Locate the buyer
        TicketBuyer buyer = buyerRepo.findByEmail(buyerEmail);
        if (buyer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Buyer not found: " + buyerEmail)
                    .build();
        }

        Long eventId = extractEventId(itemName);
        Event event = eventRepo.findById(eventId);
        if (event == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Event not found: " + eventId)
                    .build();
        }

        // 7) Determine number of tickets purchased
        BigDecimal ticketPriceBd = BigDecimal.valueOf(event.getTicketPrice());
        int quantity = amount
                .divide(ticketPriceBd, 0, RoundingMode.DOWN)
                .intValue();

        // 8) Reserve tickets
        List<Ticket> tickets = ticketRepo.findAvailableTicketsByEvent(event, quantity);
        if (tickets.size() < quantity) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Not enough tickets for event " + eventId)
                    .build();
        }

        for (Ticket t : tickets) {
            t.setTicketStatus("SOLD");
            buyer.getTickets().add(t);
            ticketRepo.update(t);
        }

        // 9) Update event counts
        event.setTicketsAvailable(event.getTicketsAvailable() - quantity);
        event.setTicketsSold(event.getTicketsSold() + quantity);
        eventRepo.update(event);

        // 10) Update buyer record
        buyerRepo.update(buyer);

        // 11) Respond to PayFast
        return Response.ok("IPN Processed: " + pfPaymentId).build();
    }

    private Long extractEventId(String itemName) {
        try {
            int idx = itemName.lastIndexOf("ID:");
            String idStr = itemName.substring(idx + 3, itemName.length() - 1).trim();
            return Long.valueOf(idStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot parse event ID from item_name: " + itemName);
        }
    }
}
