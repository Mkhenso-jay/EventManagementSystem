/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import entity.model.UserAccount;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("chatBean")
@ViewScoped
public class ChatBean implements Serializable {

    private UserAccount selectedVenueOwner;

    public UserAccount getSelectedVenueOwner() {
        return selectedVenueOwner;
    }

    public void setSelectedVenueOwner(UserAccount selectedVenueOwner) {
        this.selectedVenueOwner = selectedVenueOwner;
    }
}

