package beans;

import entity.model.UserAccount;
import entity.model.EventOrganizer;
import entity.model.TicketBuyer;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private String email;
    private String password;
    private String userType;

    @PersistenceContext
    private EntityManager em;

    // For bean-based validation
    @Inject
    private VenueOwnerBean venueOwnerBean;

    @Inject
    private EventOrganizerBean eventOrganizerBean;

    @Inject
    private TicketBuyerBean ticketBuyerBean;

    // Logged-in user references
    private EventOrganizer loggedInOrganizer;
    private TicketBuyer loggedInTicketBuyer;

    public String login() {
        try {
            String hashed = hashPassword(password);

            String category = userType;
            if (category == null || category.isEmpty()) {
                UserAccount ua = em.createQuery(
                        "SELECT u FROM UserAccount u WHERE u.email = :email", UserAccount.class)
                        .setParameter("email", email.trim().toLowerCase())
                        .getSingleResult();
                if (!ua.getPassword().equals(hashed)) {
                    showError("Invalid email or password.");
                    return "loginFailed.xhtml?faces-redirect=true";
                }
                category = ua.getCategory();
            }

            boolean authenticated = false;
            String target = "loginFailed.xhtml?faces-redirect=true";

            switch (category) {
                case "VenueOwner":
                    // Try bean
                    authenticated = venueOwnerBean.validateLogin(email, hashed);
                    if (!authenticated) {
                        showError("Invalid email or password.");
                    } else {
                        target = "homeVenueOwner.xhtml?faces-redirect=true";
                    }
                    break;

                case "EventOrganizer":
                    // Bean validation
                    authenticated = eventOrganizerBean.validateLogin(email, hashed);
                    if (authenticated) {
                        // load organizer for use
                        loggedInOrganizer = eventOrganizerBean.findByEmail(email);
                        target = "homeEventOrganizer.xhtml?faces-redirect=true";
                    } else {
                        showError("Invalid email or password.");
                    }
                    break;

                case "TicketBuyer":
                    // Bean validation
                    authenticated = ticketBuyerBean.validateLogin(email, hashed);
                    if (authenticated) {
                        loggedInTicketBuyer = ticketBuyerBean.findByEmail(email);
                        ticketBuyerBean.setCurrentBuyer(loggedInTicketBuyer);
                        target = "homeTicketBuyer.xhtml?faces-redirect=true";
                    } else {
                        showError("Invalid email or password.");
                    }
                    break;

                default:
                    showError("Unknown user type.");
            }

            return target;

        } catch (Exception e) {
            showError("Invalid email or password.");
            return "loginFailed.xhtml?faces-redirect=true";
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "landing.xhtml?faces-redirect=true";
    }

    private void showError(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private String hashPassword(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pwd.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public EventOrganizer getLoggedInOrganizer() {
        return loggedInOrganizer;
    }

    public TicketBuyer getLoggedInTicketBuyer() {
        return loggedInTicketBuyer;
    }

    public VenueOwnerBean getVenueOwnerBean() {
        return venueOwnerBean;
    }

    public EventOrganizerBean getEventOrganizerBean() {
        return eventOrganizerBean;
    }

    public TicketBuyerBean getTicketBuyerBean() {
        return ticketBuyerBean;
    }
}
