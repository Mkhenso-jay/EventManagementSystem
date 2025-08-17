
package repositories;

import entity.model.VenueOwner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class VenueOwnerRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(VenueOwner venueOwner) {
        em.persist(venueOwner);
    }

    public VenueOwner findByEmail(String email) {
        try {
            return em.createQuery("SELECT v FROM VenueOwner v WHERE v.email = :email", VenueOwner.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
