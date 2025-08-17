package beans;

import services.ChatService;
import services.SessionManager;
import entity.model.Message;
import entity.model.UserAccount;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class VenueOwnerMessageController implements Serializable { // 1. Add Serializable

    private static final long serialVersionUID = 1L; // 2. Add serialVersionUID

    @Inject
    private transient ChatService chatService; // 3. Mark non-serializable service as transient

    @Inject
    private SessionManager sessionManager; // 4. Keep only if SessionManager is Serializable

    private Long selectedUserId;
    private String newMessage;
    private List<Message> messages;
    private List<UserAccount> availableUsers;

    @PostConstruct
    public void init() {
        UserAccount currentUser = sessionManager.getCurrentUser();
        availableUsers = chatService.getAllUsersExceptCurrent(currentUser.getId());
    }

    public void loadMessages() {
        UserAccount currentUser = sessionManager.getCurrentUser();
        UserAccount selectedUser = chatService.getUserById(selectedUserId);
        messages = chatService.getMessagesBetweenUsers(currentUser, selectedUser);
    }

    public void sendMessage() {
        UserAccount sender = sessionManager.getCurrentUser();
        UserAccount receiver = chatService.getUserById(selectedUserId);
        
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(newMessage);
        
        chatService.sendMessage(message);
        newMessage = "";
        loadMessages();
    }

    // Getters and setters
    public Long getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Long selectedUserId) { 
        this.selectedUserId = selectedUserId;
        loadMessages();
    }
    public String getNewMessage() { return newMessage; }
    public void setNewMessage(String newMessage) { this.newMessage = newMessage; }
    public List<Message> getMessages() { return messages; }
    public List<UserAccount> getAvailableUsers() { return availableUsers; }
}