package controllers;

import services.ChatService;
import entity.model.Message;
import entity.model.UserAccount;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ChatController implements Serializable {

    @Inject
    private ChatService chatService;
    
    private Long currentUserId = 1L; 
    private Long selectedUserId;
    private String newMessage;
    private List<Message> conversation;
    private List<UserAccount> users;

    public void init() {
        users = chatService.getAllUsersExceptCurrent(currentUserId);
    }

    public void loadConversation() {
        if(selectedUserId != null) {
            conversation = chatService.getConversation(currentUserId, selectedUserId);
        }
    }

    public void sendMessage() {
        Message message = new Message();
        message.setSender(new UserAccount(currentUserId));
        message.setReceiver(new UserAccount(selectedUserId));
        message.setContent(newMessage);
        
        chatService.sendMessage(message);
        newMessage = "";
        loadConversation();
    }

    // Getters/setters
    public Long getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Long selectedUserId) { 
        this.selectedUserId = selectedUserId;
        loadConversation();
    }
    public String getNewMessage() { return newMessage; }
    public void setNewMessage(String newMessage) { this.newMessage = newMessage; }
    public List<Message> getConversation() { return conversation; }
    public List<UserAccount> getUsers() { return users; }
}