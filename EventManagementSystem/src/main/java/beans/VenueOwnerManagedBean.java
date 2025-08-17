
package beans;

import entity.model.Venue;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;


@Named
@RequestScoped
public class VenueOwnerManagedBean {

    @Inject
    private VenueOwnerBean venueOwnerBean;

    private Long venueOwnerId; 
    private Venue venue; 
    private List<Venue> venues;

    @PostConstruct
    public void init() {
        venueOwnerId = getLoggedInVenueOwnerId();
        venues = venueOwnerBean.getVenuesByOwner(venueOwnerId);
        venue = new Venue(); 
    }

    public Long getLoggedInVenueOwnerId() {
        return 1L; 
    }

    public void addVenue() {
        venueOwnerBean.saveVenue(venue);
        venues = venueOwnerBean.getVenuesByOwner(venueOwnerId); 
    }

    public void deleteVenue(Venue venue) {
        venueOwnerBean.deleteVenue(venue);
        venues = venueOwnerBean.getVenuesByOwner(venueOwnerId); 
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }
}
