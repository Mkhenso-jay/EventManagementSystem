
package api;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import repositories.EventRepository;


@WebServlet("/imageServlet")
public class ImageServlet extends HttpServlet {
    
    @Inject
    private EventRepository eventRepository;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        
        try {
            Long eventId = Long.parseLong(request.getParameter("eventId"));
            byte[] imageBytes = eventRepository.findEventImageById(eventId);
            
            if (imageBytes != null && imageBytes.length > 0) {
                response.setContentType("image/jpeg");
                response.setContentLength(imageBytes.length);
                response.getOutputStream().write(imageBytes);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    
}