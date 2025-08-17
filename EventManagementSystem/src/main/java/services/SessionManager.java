package services;

import entity.model.UserAccount;
import entity.model.VenueOwner;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
@Named
@SessionScoped
public class SessionManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserAccount currentUser;

    public void login(UserAccount user) {
        this.currentUser = user;
        FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .put("currentUser", user);
    }

    public UserAccount getCurrentUser() {
        if (currentUser == null) {
            // restore from session map on bean re-creation
            currentUser = (UserAccount) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("currentUser");
        }
        return currentUser;
    }

    public VenueOwner getCurrentVenueOwner() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
