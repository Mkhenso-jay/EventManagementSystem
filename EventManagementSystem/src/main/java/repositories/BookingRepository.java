/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repositories;

import entity.model.VenueBooking;
import entity.model.VenueOwner;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class BookingRepository {
    @PersistenceContext private EntityManager em;

    public List<VenueBooking> findByOwner(VenueOwner owner) {
    return em.createQuery("""
        SELECT b 
          FROM VenueBooking b 
         WHERE b.venue.venueOwner = :owner 
      ORDER BY b.startDateTime
    """, VenueBooking.class)
    .setParameter("owner", owner)
    .getResultList();
}

public List<VenueBooking> findByVenueOwner(VenueOwner owner) {
    return em.createQuery("""
        SELECT b 
          FROM VenueBooking b 
         WHERE b.venue.venueOwner = :owner
        """, VenueBooking.class)
        .setParameter("owner", owner)
        .getResultList();
}
    
 

    public void save(VenueBooking booking) {
        em.persist(booking);
    }
}

