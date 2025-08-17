package beans;

import entity.model.UserAccount;
import entity.model.Venue;
import entity.model.VenueOwner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import repositories.VenueOwnerRepository;
import repositories.VenueRepository;

@Named
@RequestScoped
public class VenueOwnerBean {

    @Inject
    private VenueOwnerRepository venueOwnerRepository;

    @Inject
    private VenueRepository venueRepository;

    @Inject
    private LoginBean loginBean;

    private Venue currentVenue = new Venue();
    private List<Venue> venues;
    private Part uploadedImage; 
    private Long selectedOrganizerId;

public Long getSelectedOrganizerId() {
    return selectedOrganizerId;
}

public void setSelectedOrganizerId(Long selectedOrganizerId) {
    this.selectedOrganizerId = selectedOrganizerId;
}

private List<UserAccount> organizerList;  
public List<UserAccount> getOrganizerList() {
    return organizerList;
}

public void setOrganizerList(List<UserAccount> organizerList) {
    this.organizerList = organizerList;
}


    public void loadVenues() {
        Long ownerId = getLoggedInVenueOwnerId();
        if (ownerId != null) {
            venues = venueRepository.findByOwnerId(ownerId);
        }
    }

   
    private Long getLoggedInVenueOwnerId() {
        String email = getLoggedInEmail();
        VenueOwner owner = venueOwnerRepository.findByEmail(email);
        return (owner != null) ? owner.getId() : null;
    }

    @Transactional
    public String saveVenueAction() {
        VenueOwner owner = venueOwnerRepository.findByEmail(getLoggedInEmail());
        currentVenue.setVenueOwner(owner);
        
        
         // Handle image upload
        if (uploadedImage != null) {
        try (InputStream is = uploadedImage.getInputStream()) {
            // Read the image bytes and store in the Event entity
            byte[] imageBytes = is.readAllBytes();
            currentVenue.setVenueImage(imageBytes); 

            // Optional: Save the image to the filesystem (if needed)
            String uploadDir = "C:\\Users\\DevSpark Studios\\uploads";
            File uploads = new File(uploadDir);
            if (!uploads.exists()) {
                uploads.mkdirs();
            }
            String fileName = "event_" + System.currentTimeMillis() + ".jpg";
            File uploadedFile = new File(uploads, fileName);
            Files.write(uploadedFile.toPath(), imageBytes); // Write bytes to file

        } catch (IOException e) {
            e.printStackTrace();
            // Handle error (e.g., show a Faces message)
        }
    }
        venueRepository.save(currentVenue);

      

        loadVenues(); 
        currentVenue = new Venue(); 
        return "homeVenueOwner.xhtml?faces-redirect=true";
    }
    
    public String getImageUrl(Long venueId) {
        return "/venueImageServlet?venueId=" + venueId;
    }

     public String getBase64Image(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) return "";
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
       public byte[] getVenueImage(Long venueId) {
        return venueRepository.findVenueImageById(venueId);
    }
  

    @Transactional
    public String deleteVenueAction(Venue venue) {
        venueRepository.delete(venue);
        deleteFile(venue.getId());
        loadVenues();
        return "homeVenueOwner.xhtml?faces-redirect=true";
    }

    private void deleteFile(Long venueId) {
        File file = new File("C:/venue-images/" + venueId + ".jpg");
        if (file.exists()) {
            file.delete();
        }
    }

    public void editVenue(Venue venue) {
        this.currentVenue = venue;
    }

    public String resetCurrentVenue() {
        this.currentVenue = new Venue();
        return null;
    }

    private String getLoggedInEmail() {
        return loginBean.getEmail();
    }

    public Venue getCurrentVenue() {
        return currentVenue;
    }

    public void setCurrentVenue(Venue currentVenue) {
        this.currentVenue = currentVenue;
    }

    public List<Venue> getVenues() {
        if (venues == null) {
            loadVenues();
        }
        return venues;
    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }

    public Part getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(Part uploadedImage) {
        this.uploadedImage = uploadedImage;
    }

    @Transactional
    public void registerVenueOwner(VenueOwner venueOwner) {
        venueOwnerRepository.save(venueOwner);
    }

    public boolean validateLogin(String email, String password) {
        VenueOwner venueOwner = venueOwnerRepository.findByEmail(email);
        return venueOwner != null && venueOwner.getPassword().equals(password);
    }

    public List<Venue> getVenuesByOwner(Long venueOwnerId) {
        return venueRepository.findByOwnerId(venueOwnerId);
    }

    @Transactional
    public void saveVenue(Venue venue) {
        venueRepository.save(venue);
    }

    @Transactional
    public void deleteVenue(Venue venue) {
        venueRepository.delete(venue);
    }
    
    public boolean emailExists(String email) {
    VenueOwner existing = venueOwnerRepository.findByEmail(email);
    return existing != null;
}
    
    /**
 * Return the full VenueOwner entity for the currently logged‐in email,
 * or null if not found.
 */
public VenueOwner getLoggedInVenueOwner() {
    String email = loginBean.getEmail();
    return (email != null)
         ? venueOwnerRepository.findByEmail(email)
         : null;
}


}
