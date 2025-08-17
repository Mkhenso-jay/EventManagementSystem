
package repositories;

import entity.model.Venue;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;


@Stateless
public class VenueRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(Venue venue) {
        if (venue.getId() == null) {
            entityManager.persist(venue);
        } else {
            entityManager.merge(venue);
        }
    }

    public void delete(Venue venue) {
        entityManager.remove(entityManager.contains(venue) ? venue : entityManager.merge(venue));
    }

    public List<Venue> findByOwnerId(Long venueOwnerId) {
        return entityManager.createQuery("SELECT v FROM Venue v WHERE v.venueOwner.id = :ownerId", Venue.class)
                            .setParameter("ownerId", venueOwnerId)
                            .getResultList();
    }
    
        
  public byte[] findVenueImageById(Long venueId) {
    try {
        return entityManager.createQuery(
                "SELECT v.venueImage FROM Venue v WHERE v.id = :venueId", byte[].class)
            .setParameter("venueId", venueId)
            .getSingleResult();
    } catch (NoResultException e) {
        return null; 
    }
}
  
    public Venue findById(Long id) {
        return entityManager.find(Venue.class, id);
    }
    
public List<Venue> findAll() {
    return entityManager.createQuery("SELECT v FROM Venue v", Venue.class)
                       .getResultList();
}
}
