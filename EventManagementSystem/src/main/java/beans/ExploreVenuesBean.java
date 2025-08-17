/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import repositories.VenueRepository;
import entity.model.Venue;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class ExploreVenuesBean {
    
    @Inject
    private VenueRepository venueRepository;
    
    private List<Venue> venues;
    
    @PostConstruct
    public void init() {
        venues = venueRepository.findAll();
    }
    
    public List<Venue> getVenues() {
        return venues;
    }
}