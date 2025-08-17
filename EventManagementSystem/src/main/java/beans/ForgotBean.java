package beans;

import entity.model.UserAccount;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import repositories.UserAccountBean;

@Named("forgotBean")
@SessionScoped
public class ForgotBean implements Serializable {

    private String mode; // "retrieveEmail", "changePassword", "changeDetails"
    private String userCode;
    private String name;
    private String lastName;
    private String newEmail;
    private String newPassword;
    private String retrievedEmail;

    @Inject
    private UserAccountBean userAccountBean;

    // === Navigation Logic ===
    public String processInitialStep() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (mode == null || mode.isEmpty() || userCode == null || userCode.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all fields.", null));
            return null;
        }

        switch (mode) {
            case "retrieveEmail":
                retrieveEmail(ctx);
                return "displayEmail";
            case "changePassword":
                return "enterNewPassword";
            case "changeDetails":
                return "enterNewDetails";
            default:
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid option.", null));
                return null;
        }
    }

    private void retrieveEmail(FacesContext ctx) {
        UserAccount acct = userAccountBean.findByUserCode(userCode);
        if (acct != null) {
            retrievedEmail = acct.getEmail();
        } else {
            retrievedEmail = null;
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No account found for that code.", null));
        }
    }

    public String submitNewPassword() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (newPassword == null || newPassword.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "New password is required.", null));
            return null;
        }

        String hashed = hash(newPassword);
        boolean updated = userAccountBean.updatePasswordByUserCode(userCode, hashed);

        if (!updated) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to update password.", null));
            return null;
        }

        return "success";
    }

    public String submitDetailsStep1() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (name == null || name.isEmpty() || lastName == null || lastName.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "First and Last Name are required.", null));
            return null;
        }

        return "enterNewEmailPassword";
    }

    public String submitDetailsStep2() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (newEmail == null || newEmail.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email and password are required.", null));
            return null;
        }

        // Debug logging
        System.out.println("Updating details for userCode: " + userCode);
        System.out.println("New Email: " + newEmail);
        String hashed = hash(newPassword);
        System.out.println("New Hashed Password: " + hashed);

        boolean updated = userAccountBean.updateEmailAndPasswordByUserCode(userCode, newEmail, hashed);
        if (!updated) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to update details. Please make sure the user code is correct.", null));
            return null;
        }

        return "success";
    }

    // === Simple SHA-256 hashing ===
    private String hash(String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(pw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    // === Getters & Setters ===
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRetrievedEmail() {
        return retrievedEmail;
    }

    public void setRetrievedEmail(String retrievedEmail) {
        this.retrievedEmail = retrievedEmail;
    }
}
