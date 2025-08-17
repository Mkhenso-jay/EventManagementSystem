
package beans;
import entity.model.Ticket;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TicketBean {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persist a new Ticket to the database
     * @param ticket the Ticket entity to persist
     */
    public void save(Ticket ticket) {
        entityManager.persist(ticket);
    }

    /**
     * Merge (update) an existing Ticket in the database
     * @param ticket the Ticket entity to update
     * @return the managed instance
     */
    public Ticket update(Ticket ticket) {
        return entityManager.merge(ticket);
    }

    /**
     * Find a Ticket by its primary key
     * @param id the Ticket id
     * @return the Ticket or null if not found
     */
    public Ticket findById(Long id) {
        return entityManager.find(Ticket.class, id);
    }

    /**
     * Retrieve all tickets for a given event
     * @param eventId the Event id
     * @return list of Tickets
     */
    public List<Ticket> findByEvent(Long eventId) {
        return entityManager.createQuery(
                "SELECT t FROM Ticket t WHERE t.event.id = :eventId", Ticket.class)
                .setParameter("eventId", eventId)
                .getResultList();
    }

    /**
     * Delete a Ticket
     * @param ticket the Ticket to remove
     */
    public void delete(Ticket ticket) {
        Ticket managed = entityManager.contains(ticket) ? ticket : entityManager.merge(ticket);
        entityManager.remove(managed);
    }
}
