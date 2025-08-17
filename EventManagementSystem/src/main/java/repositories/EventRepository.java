    package repositories;

    import entity.model.Event;
    import jakarta.ejb.Stateless;
    import jakarta.persistence.EntityManager;
    import jakarta.persistence.NoResultException;
    import jakarta.persistence.PersistenceContext;
    import java.util.List;

    @Stateless
    public class EventRepository {

        @PersistenceContext
        private EntityManager entityManager;

        /**
         * Merge (update) an existing Event in the database.
         * @param event the Event entity to update
         * @return the managed instance of Event
         */
        public Event update(Event event) {
            return entityManager.merge(event);
        }

        /**
         * Persist or merge an Event.
         * If the ID is null, it’s a new Entity → persist().
         * Otherwise merge().
         */
        public Event save(Event event) {
            if (event.getId() == null) {
                entityManager.persist(event);
                return event;
            } else {
                return entityManager.merge(event);
            }
        }


        public void delete(Event event) {
            entityManager.remove(
                entityManager.contains(event) ? event : entityManager.merge(event)
            );
        }


        public Event findById(Long id) {
            return entityManager.find(Event.class, id);
        }


        public List<Event> findByOrganizerId(Long organizerId) {
            return entityManager.createQuery(
                    "SELECT e FROM Event e WHERE e.eventOrganizer.id = :organizerId", Event.class
                )
                .setParameter("organizerId", organizerId)
                .getResultList();
        }


        public List<Event> findAllEvents() {
            return entityManager.createQuery("SELECT e FROM Event e", Event.class)
                                .getResultList();
        }

        public List<Event> findEventsWithAvailableTickets() {
            return entityManager.createQuery(
                    "SELECT e FROM Event e WHERE e.ticketsAvailable > 0", Event.class
                )
                .getResultList();
        }

      public byte[] findEventImageById(Long eventId) {
        try {
            return entityManager.createQuery(
                    "SELECT e.eventImage FROM Event e WHERE e.id = :eventId", byte[].class)
                .setParameter("eventId", eventId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;  // Or handle it as needed
        }
    }


    }
