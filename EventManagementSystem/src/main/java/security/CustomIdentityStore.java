
package security;


import beans.EventOrganizerBean;
import beans.TicketBuyerBean;
import beans.VenueOwnerBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import java.util.Set;

@ApplicationScoped
public class CustomIdentityStore implements IdentityStore {

    @Inject
    private TicketBuyerBean ticketBuyerBean;

    @Inject
    private EventOrganizerBean eventOrganizerBean;

    @Inject
    private VenueOwnerBean venueOwnerBean;

    public CredentialValidationResult validate(UsernamePasswordCredential credential) {
        String email = credential.getCaller();
        String password = credential.getPasswordAsString();
        String hashedPassword = hashPassword(password);

        // Venue Owner
        if (venueOwnerBean.validateLogin(email, hashedPassword)) {
            return new CredentialValidationResult(email, Set.of("VENUE_OWNER"));
        }

        // Event Organizer
        if (eventOrganizerBean.validateLogin(email, hashedPassword)) {
            return new CredentialValidationResult(email, Set.of("EVENT_ORGANIZER"));
        }

        // Ticket Buyer
        if (ticketBuyerBean.validateLogin(email, hashedPassword)) {
            return new CredentialValidationResult(email, Set.of("TICKET_BUYER"));
        }

        return CredentialValidationResult.INVALID_RESULT;
    }

    private String hashPassword(String password) {
        // Same hashing as in your bean
        // Move this to a utility class if reused
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
