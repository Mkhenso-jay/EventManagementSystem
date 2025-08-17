package beans;

import entity.model.EventOrganizer;
import entity.model.TicketBuyer;
import entity.model.UserAccount;
import entity.model.VenueOwner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import repositories.UserAccountBean;

@Named
@RequestScoped
public class SignupBean {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private String userType;
    private String generatedCode;

    @Inject
    private VenueOwnerBean venueOwnerBean;

    @Inject
    private EventOrganizerBean eventOrganizerBean;

    @Inject
    private TicketBuyerBean ticketBuyerBean;

    @Inject
    private UserAccountBean userAccountBean;

    public String registerUser() {
        FacesContext context = FacesContext.getCurrentInstance();

        // 1) Password match check
        if (!password.equals(confirmPassword)) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match.", null));
            return null;
        }

        // 2) Email uniqueness across all sign-up tables *and* central UserAccount
        if (emailExists(email) || userAccountBean.findByEmail(email) != null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email already in use.", null));
            return null;
        }

        // 3) Hash password
        String hashedPassword = hashPassword(password);

        // 4) Create role-specific entity & capture its generated userCode
        generatedCode = null;
        switch (userType) {
            case "VenueOwner": {
                VenueOwner vo = new VenueOwner();
                vo.setFirstName(firstName);
                vo.setLastName(lastName);
                vo.setEmail(email);
                vo.setPassword(hashedPassword);
                venueOwnerBean.registerVenueOwner(vo);
                generatedCode = vo.getUserCode();
                break;
            }
            case "EventOrganizer": {
                EventOrganizer eo = new EventOrganizer();
                eo.setFirstName(firstName);
                eo.setLastName(lastName);
                eo.setEmail(email);
                eo.setPassword(hashedPassword);
                eventOrganizerBean.registerEventOrganizer(eo);
                generatedCode = eo.getUserCode();
                break;
            }
            case "TicketBuyer": {
                TicketBuyer tb = new TicketBuyer();
                tb.setFirstName(firstName);
                tb.setLastName(lastName);
                tb.setEmail(email);
                tb.setPassword(hashedPassword);
                ticketBuyerBean.registerTicketBuyer(tb);
                generatedCode = tb.getUserCode();
                break;
            }
            default:
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a user type.", null));
                return null;
        }

        saveUserAccountRecord(
                firstName,
                lastName,
                email,
                hashedPassword,
                userType,
                generatedCode
        );

  
        return "confirmation.xhtml?faces-redirect=true";
    }

    private boolean emailExists(String email) {
        return venueOwnerBean.emailExists(email)
                || eventOrganizerBean.emailExists(email)
                || ticketBuyerBean.emailExists(email);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void saveUserAccountRecord(String firstName,
            String lastName,
            String email,
            String hashedPassword,
            String category,
            String userCode) {
        UserAccount acct = new UserAccount();
        acct.setFirstName(firstName);
        acct.setLastName(lastName);
        acct.setEmail(email);
        acct.setPassword(hashedPassword);
        acct.setCategory(category);
        acct.setUserCode(userCode);
        userAccountBean.create(acct);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getGeneratedCode() {
        return generatedCode;
    }
}
