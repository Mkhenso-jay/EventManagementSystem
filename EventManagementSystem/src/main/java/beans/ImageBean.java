/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import entity.model.Event;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Named
@RequestScoped
public class ImageBean {

    @PersistenceContext
    private EntityManager em;

    public byte[] getEventImage(Long eventId) {
        Event event = em.find(Event.class, eventId);
        return (event != null) ? event.getEventImage() : null;
    }

    public InputStream getEventImageStream(Long eventId) {
        byte[] image = getEventImage(eventId);
        return (image != null) ? new ByteArrayInputStream(image) : null;
    }
}
