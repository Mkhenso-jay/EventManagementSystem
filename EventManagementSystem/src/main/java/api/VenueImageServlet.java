
package api;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import repositories.VenueRepository;

@WebServlet("/venueImageServlet")
public class VenueImageServlet extends HttpServlet {

    @Inject
    private VenueRepository venueRepository;

   
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String venueIdParam = req.getParameter("venueId");
        if (venueIdParam != null) {
            try {
                Long venueId = Long.parseLong(venueIdParam);
                byte[] image = venueRepository.findVenueImageById(venueId);
                if (image != null && image.length > 0) {
                    resp.setContentType("image/jpeg");
                    resp.getOutputStream().write(image);
                }
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Venue ID");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Venue ID is required");
        }
    }
}


