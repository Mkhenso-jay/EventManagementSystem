
package repositories;

import entity.model.EventOrganizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EventOrganizerRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(EventOrganizer eventOrganizer) {
        em.persist(eventOrganizer);
    }

    public EventOrganizer findByEmail(String email) {
        try {
            return em.createQuery("SELECT e FROM EventOrganizer e WHERE e.email = :email", EventOrganizer.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
