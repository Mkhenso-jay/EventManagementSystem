package repositories;

import entity.model.UserAccount;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UserAccountBean {

    @PersistenceContext
    private EntityManager em;

    /**
     * Create a new account
     */
    public void create(UserAccount account) {
        em.persist(account);
    }

    /**
     * Find by primary key
     */
    public UserAccount findById(Long id) {
        return em.find(UserAccount.class, id);
    }

    /**
     * Find by email (username)
     */
    public UserAccount findByEmail(String email) {
        try {
            return em.createQuery(
                    "SELECT u FROM UserAccount u WHERE u.email = :email", UserAccount.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find by userCode (for account recovery)
     */
    public UserAccount findByUserCode(String userCode) {
        try {
            return em.createQuery(
                    "SELECT u FROM UserAccount u WHERE u.userCode = :code", UserAccount.class)
                    .setParameter("code", userCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retrieve all accounts
     */
    public List<UserAccount> findAll() {
        return em.createQuery("SELECT u FROM UserAccount u", UserAccount.class)
                .getResultList();
    }

    /**
     * Merge updates into an existing account
     */
    public UserAccount update(UserAccount account) {
        return em.merge(account);
    }

    /**
     * Remove an account by id
     */
    public void delete(Long id) {
        UserAccount acct = em.find(UserAccount.class, id);
        if (acct != null) {
            em.remove(acct);
        }
    }

    /**
     * Validate login credentials
     */
    public boolean validateLogin(String email, String password) {
        UserAccount u = findByEmail(email);
        return u != null && u.getPassword().equals(password);
    }

    /**
     * Update only the password (for "Forgot Password" flow)
     */
    public boolean updatePasswordByUserCode(String userCode, String newHashedPassword) {
        UserAccount u = findByUserCode(userCode);
        if (u != null) {
            u.setPassword(newHashedPassword);
            em.merge(u);
            return true;
        }
        return false;
    }

    /**
     * Update both email and password (for "Change Details" flow)
     */
    public boolean updateEmailAndPasswordByUserCode(String userCode,
            String newEmail,
            String newHashedPassword) {
        UserAccount u = findByUserCode(userCode);
        if (u != null) {
            u.setEmail(newEmail);
            u.setPassword(newHashedPassword);
            em.merge(u);
            return true;
        }
        return false;
    }
}
