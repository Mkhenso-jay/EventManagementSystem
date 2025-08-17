package services;

import entity.model.Message;
import entity.model.UserAccount;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ChatService {

    @PersistenceContext(unitName = "event_managementPU") // Add unit name

    EntityManager em;

    public UserAccount getUserById(Long userId) {
        return em.find(UserAccount.class, userId);
    }

    public List<Message> getMessagesBetweenUsers(UserAccount user1, UserAccount user2) {
        return em.createQuery(
                "SELECT m FROM Message m WHERE "
                + "(m.sender = :user1 AND m.receiver = :user2) OR "
                + "(m.sender = :user2 AND m.receiver = :user1) "
                + "ORDER BY m.timestamp ASC", Message.class)
                .setParameter("user1", user1)
                .setParameter("user2", user2)
                .getResultList();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void sendMessage(Message message) {
        em.persist(message);
    }

    public List<UserAccount> getAllUsersExceptCurrent(Long currentUserId) {
        return em.createQuery(
                "SELECT u FROM UserAccount u WHERE u.id != :userId", UserAccount.class)
                .setParameter("userId", currentUserId)
                .getResultList();
    }

    public List<Message> getConversation(Long currentUserId, Long selectedUserId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Transactional
    public void saveMessage(Message newMsg) {
        em.persist(newMsg);
    }

    public List<UserAccount> getUsersByRole(String event_organizer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Message> getNewMessagesBetweenUsers(UserAccount currentUser, UserAccount receiver, LocalDateTime lastTimestamp) {
        return em.createQuery(
                "SELECT m FROM Message m WHERE "
                + "((m.sender = :currentUser AND m.receiver = :receiver) OR "
                + "(m.sender = :receiver AND m.receiver = :currentUser)) AND "
                + "m.timestamp > :lastTimestamp "
                + "ORDER BY m.timestamp ASC", Message.class)
                .setParameter("currentUser", currentUser)
                .setParameter("receiver", receiver)
                .setParameter("lastTimestamp", lastTimestamp)
                .getResultList();
    }

}
