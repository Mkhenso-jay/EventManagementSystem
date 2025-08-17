package controllers;

import services.ChatService;
import services.SessionManager;
import entity.model.Message;
import entity.model.UserAccount;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Named("messageController")
@ViewScoped
public class MessageController implements Serializable {
    private static final long serialVersionUID = 1L;    

    @Inject
    private ChatService chatService;

    @Inject
    private SessionManager sessionManager;

    private Long selectedReceiverId;
    private String messageContent;
    private List<Message> messageList;
    private List<UserAccount> users;
    


  @PostConstruct
public void init() {
    UserAccount currentUser = sessionManager.getCurrentUser();
    if (currentUser != null) {
        users = chatService.getAllUsersExceptCurrent(currentUser.getId());
    } else {
        // Handle unauthenticated user (redirect to login?)
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("Not logged in!"));
    }
}

public void loadMessages() {
    UserAccount currentUser = sessionManager.getCurrentUser();
    if (currentUser == null || selectedReceiverId == null) return;
    
    UserAccount receiver = chatService.getUserById(selectedReceiverId);
    if (receiver != null) {
        messageList = chatService.getMessagesBetweenUsers(currentUser, receiver);
    }
} 

  public void sendMessage() {
    UserAccount sender = sessionManager.getCurrentUser();
    UserAccount receiver = chatService.getUserById(selectedReceiverId);

    if (sender != null && receiver != null && messageContent != null && !messageContent.trim().isEmpty()) {
        Message newMsg = new Message();
        newMsg.setSender(sender);
        newMsg.setReceiver(receiver);
        newMsg.setContent(messageContent);
        newMsg.setTimestamp(new Date());

        chatService.saveMessage(newMsg);
        messageContent = "";   
        loadMessages();      
    }
}


    // Getters and Setters

    public Long getSelectedReceiverId() {
        return selectedReceiverId;
    }

    public void setSelectedReceiverId(Long selectedReceiverId) {
        this.selectedReceiverId = selectedReceiverId;
        loadMessages(); // Automatically reload messages when receiver changes
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public UserAccount getLoggedInUser() {
        return sessionManager.getCurrentUser();
    }
    // Receives all messages again (same as loadMessages, but can be used separately in the UI)
public void receiveMessages() {
    loadMessages();
}

// Loads only new messages after the last timestamp
public void loadNewMessages() {
    UserAccount currentUser = sessionManager.getCurrentUser();
    if (selectedReceiverId != null && currentUser != null && messageList != null && !messageList.isEmpty()) {
        UserAccount receiver = chatService.getUserById(selectedReceiverId);
        LocalDateTime lastTimestamp = messageList.get(messageList.size() - 1).getTimestamp();
        List<Message> newMessages = chatService.getNewMessagesBetweenUsers(currentUser, receiver, lastTimestamp);
        if (newMessages != null && !newMessages.isEmpty()) {
            messageList.addAll(newMessages);
        }
    }
}


}

