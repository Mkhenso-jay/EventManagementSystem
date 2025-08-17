package repositories;

import entity.model.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

public class MessageRepository {

    @PersistenceContext
    private EntityManager em;

    // Save a message
    @Transactional
    public void save(Message message) {
        em.persist(message);
    }

    // Find messages between sender and receiver
    public List<Message> findMessagesBySenderAndReceiver(Long senderId, Long receiverId) {
        return em.createQuery(
                "SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) " +
                        "OR (m.sender.id = :receiverId AND m.receiver.id = :senderId) ORDER BY m.timestamp",
                Message.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId)
                .getResultList();
    }
}
