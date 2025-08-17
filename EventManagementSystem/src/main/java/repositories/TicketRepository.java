package repositories;

import entity.model.Ticket;
import entity.model.Event;
import entity.model.TicketBuyer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TicketRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Ticket t) {
        em.persist(t);
    }

    @Transactional(Transactional.TxType.SUPPORTS) // Read operations use SUPPORTS
    public List<Ticket> findAvailableTickets() {
        return em.createQuery(
                "SELECT t FROM Ticket t WHERE t.ticketStatus = :status",
                Ticket.class)
                .setParameter("status", "AVAILABLE")
                .getResultList();
    }

    @Transactional
    public Ticket update(Ticket ticket) {
        return em.merge(ticket);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Ticket> findAvailableTicketsByEvent(Event event, int maxResults) {
        return em.createQuery(
                "SELECT t FROM Ticket t WHERE t.event = :event AND t.ticketStatus = 'AVAILABLE'",
                Ticket.class)
                .setParameter("event", event)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Ticket> findPurchasedTicketsForUser(String email) {
        return em.createQuery(
                "SELECT t FROM Ticket t WHERE t.ticketBuyer.email = :email AND t.ticketStatus = 'SOLD'",
                Ticket.class)
                .setParameter("email", email)
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Ticket> findByTicketBuyer(TicketBuyer ticketBuyer) {
        return em.createQuery(
                "SELECT t FROM Ticket t WHERE t.ticketBuyer = :buyer",
                Ticket.class)
                .setParameter("buyer", ticketBuyer)
                .getResultList();
    }
}