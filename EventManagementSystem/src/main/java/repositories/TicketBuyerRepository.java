package repositories;

import entity.model.TicketBuyer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TicketBuyerRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(TicketBuyer ticketBuyer) {
        em.persist(ticketBuyer);
    }

    public TicketBuyer findByEmail(String email) {
        try {
            return em.createQuery("SELECT t FROM TicketBuyer t WHERE t.email = :email", TicketBuyer.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public TicketBuyer update(TicketBuyer buyer) {
        return em.merge(buyer);
    }
}
