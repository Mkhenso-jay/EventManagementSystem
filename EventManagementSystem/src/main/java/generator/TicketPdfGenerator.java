package generator;

import entity.model.Ticket;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPdfGenerator {

    // Design constants
    private static final float MARGIN = 40;
    private static final float LINE_HEIGHT = 22;
    private static final float TITLE_FONT_SIZE = 24;
    private static final float HEADER_FONT_SIZE = 16;
    private static final float BODY_FONT_SIZE = 12;
    private static final float SMALL_FONT_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // Color palette
    private static final Color PRIMARY_COLOR = new Color(46, 196, 182);
    private static final Color SECONDARY_COLOR = new Color(255, 87, 34);
    private static final Color LIGHT_BG = new Color(250, 250, 250);
    private static final Color DARK_TEXT = new Color(30, 30, 30);
    private static final Color LIGHT_TEXT = new Color(100, 100, 100);
    private static final Color WHITE = Color.WHITE;

    public static ByteArrayOutputStream generateTicketPdf(List<Ticket> tickets) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Load fonts
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font lightFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float yPosition = pageHeight - MARGIN;

            // Draw subtle background pattern
            drawBackgroundPattern(contentStream, pageWidth, pageHeight);

            // Draw header with gradient effect
            drawHeader(contentStream, pageWidth, yPosition, titleFont);
            yPosition -= 80;

            for (Ticket ticket : tickets) {
                // Check for page break
                if (yPosition - 250 < MARGIN) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = pageHeight - MARGIN;
                    drawBackgroundPattern(contentStream, pageWidth, pageHeight);
                    drawHeader(contentStream, pageWidth, yPosition, titleFont);
                    yPosition -= 80;
                }

                // Draw ticket container with shadow effect
                drawTicketContainer(contentStream, pageWidth, yPosition);

                // Ticket content
                float xPos = MARGIN + 25;
                float curY = yPosition - 50;

                // Event Title with accent
                drawEventTitle(contentStream, ticket, xPos, curY, headerFont);
                curY -= LINE_HEIGHT * 1.8f;

                // Draw divider line
                drawDivider(contentStream, xPos, curY, pageWidth - 2 * MARGIN - 50);
                curY -= LINE_HEIGHT * 0.8f;

                // Ticket details in two columns
                float column2X = pageWidth / 2 + 20;
                
                // Left column
                addDetailBlock(contentStream, "Ticket ID:", "#" + ticket.getId(), xPos, curY, headerFont, bodyFont);
                addDetailBlock(contentStream, "Date:", ticket.getEvent().getEventDate().format(DATE_FORMATTER), xPos, curY - LINE_HEIGHT, headerFont, bodyFont);
              
                // Right column
                addDetailBlock(contentStream, "Venue:", ticket.getEvent().getVenue().getVenueName(), column2X, curY, headerFont, bodyFont);
                addDetailBlock(contentStream, "Price:", "R " + String.format("%.2f", ticket.getTicketPrice()), column2X, curY - LINE_HEIGHT * 2, headerFont, bodyFont);
                
                curY -= LINE_HEIGHT * 3.5f;

             

                // Ticket footer with barcode placeholder
                drawTicketFooter(contentStream, xPos, yPosition - 220, pageWidth - 2 * MARGIN - 50, bodyFont, ticket);

                yPosition -= 270;
            }

            // Global footer
            drawGlobalFooter(contentStream, MARGIN, MARGIN, pageWidth - 2 * MARGIN, bodyFont, lightFont);

            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos;
        }
    }

    private static void drawBackgroundPattern(PDPageContentStream cs, float width, float height) throws IOException {
        cs.setNonStrokingColor(LIGHT_BG);
        cs.addRect(0, 0, width, height);
        cs.fill();

        // Subtle diagonal pattern
        cs.setStrokingColor(new Color(220, 220, 220));
        cs.setLineWidth(0.5f);
        for (int i = -200; i < width + height; i += 30) {
            cs.moveTo(i, 0);
            cs.lineTo(i - height, height);
            cs.stroke();
        }
    }

    private static void drawHeader(PDPageContentStream cs, float pageWidth, float yPos, PDType1Font font) throws IOException {
        // Gradient background
        float headerHeight = 50;
        for (int i = 0; i < headerHeight; i++) {
            float ratio = i / headerHeight;
            Color gradColor = new Color(
                (int) (PRIMARY_COLOR.getRed() * (1 - ratio) + SECONDARY_COLOR.getRed() * ratio),
                (int) (PRIMARY_COLOR.getGreen() * (1 - ratio) + SECONDARY_COLOR.getGreen() * ratio),
                (int) (PRIMARY_COLOR.getBlue() * (1 - ratio) + SECONDARY_COLOR.getBlue() * ratio)
            );
            cs.setStrokingColor(gradColor);
            cs.setLineWidth(1);
            cs.moveTo(MARGIN, yPos - i);
            cs.lineTo(pageWidth - MARGIN, yPos - i);
            cs.stroke();
        }

        // Header text with shadow
        cs.beginText();
        cs.setFont(font, TITLE_FONT_SIZE);
        cs.setNonStrokingColor(new Color(0, 0, 0, 50));
        cs.newLineAtOffset(MARGIN + 2, yPos - headerHeight / 2 - TITLE_FONT_SIZE / 2 - 2);
        cs.showText("EVENTIFY - E-TICKET");
        cs.endText();

        cs.beginText();
        cs.setFont(font, TITLE_FONT_SIZE);
        cs.setNonStrokingColor(WHITE);
        cs.newLineAtOffset(MARGIN, yPos - headerHeight / 2 - TITLE_FONT_SIZE / 2);
        cs.showText("EVENTIFY - E-TICKET");
        cs.endText();
    }

    private static void drawTicketContainer(PDPageContentStream cs, float pageWidth, float yPos) throws IOException {
        // Shadow effect
        cs.setNonStrokingColor(new Color(0, 0, 0, 20));
        cs.addRect(MARGIN + 3, yPos - 203 - 3, pageWidth - 2 * MARGIN, 230);
        cs.fill();

        // Ticket background
        cs.setNonStrokingColor(WHITE);
        cs.addRect(MARGIN, yPos - 200, pageWidth - 2 * MARGIN, 230);
        cs.fill();

        // Ticket border with rounded corners effect
        cs.setStrokingColor(PRIMARY_COLOR);
        cs.setLineWidth(1.5f);
        cs.addRect(MARGIN, yPos - 200, pageWidth - 2 * MARGIN, 230);
        cs.stroke();

        // Accent stripe
        cs.setNonStrokingColor(PRIMARY_COLOR);
        cs.addRect(MARGIN, yPos - 200, 8, 230);
        cs.fill();
    }

    private static void drawEventTitle(PDPageContentStream cs, Ticket ticket, float x, float y, PDType1Font font) throws IOException {
        // Accent underline
        cs.setNonStrokingColor(SECONDARY_COLOR);
        cs.addRect(x, y - 5, 40, 3);
        cs.fill();

        // Event title
        cs.beginText();
        cs.setFont(font, HEADER_FONT_SIZE);
        cs.setNonStrokingColor(DARK_TEXT);
        cs.newLineAtOffset(x, y);
        cs.showText(ticket.getEvent().getEventName().toUpperCase());
        cs.endText();

        // Event category/subtitle
        cs.beginText();
        cs.setFont(font, SMALL_FONT_SIZE);
        cs.setNonStrokingColor(SECONDARY_COLOR);
        cs.newLineAtOffset(x, y - 15);
        cs.endText();
    }

    private static void drawDivider(PDPageContentStream cs, float x, float y, float width) throws IOException {
        cs.setStrokingColor(new Color(200, 200, 200));
        cs.setLineWidth(0.5f);
        cs.setLineDashPattern(new float[]{2, 2}, 0);
        cs.moveTo(x, y);
        cs.lineTo(x + width, y);
        cs.stroke();
        cs.setLineDashPattern(new float[]{}, 0);
    }

    private static void drawQrPlaceholder(PDPageContentStream cs, float x, float y, float size) throws IOException {
        // QR background with subtle pattern
        cs.setNonStrokingColor(new Color(245, 245, 245));
        cs.addRect(x, y, size, size);
        cs.fill();

        // QR border
        cs.setStrokingColor(PRIMARY_COLOR);
        cs.setLineWidth(1);
        cs.addRect(x, y, size, size);
        cs.stroke();

        // QR placeholder text
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), SMALL_FONT_SIZE);
        cs.setNonStrokingColor(LIGHT_TEXT);
        cs.newLineAtOffset(x + size/2 - 20, y + size/2);
        cs.showText("[QR CODE]");
        cs.endText();
    }

    private static void drawTicketFooter(PDPageContentStream cs, float x, float y, float width, PDType1Font font, Ticket ticket) throws IOException {
        // Divider
        drawDivider(cs, x, y, width);

        // Barcode placeholder
        cs.setNonStrokingColor(new Color(230, 230, 230));
        cs.addRect(x, y - 25, width, 20);
        cs.fill();

        cs.setStrokingColor(LIGHT_TEXT);
        cs.setLineWidth(0.5f);
        cs.addRect(x, y - 25, width, 20);
        cs.stroke();

        // Barcode text
        cs.beginText();
        cs.setFont(font, SMALL_FONT_SIZE);
        cs.setNonStrokingColor(LIGHT_TEXT);
        cs.newLineAtOffset(x + width/2 - 30, y - 20);
        cs.endText();

        // Terms text
        cs.beginText();
        cs.setFont(font, 8);
        cs.setNonStrokingColor(LIGHT_TEXT);
        cs.newLineAtOffset(x, y - 45);
        cs.showText("Terms: This ticket is non-transferable. Please bring ID matching the ticket holder name.");
        cs.endText();
    }

    private static void drawGlobalFooter(PDPageContentStream cs, float x, float y, float width, PDType1Font font, PDType1Font lightFont) throws IOException {
        // Footer divider
        cs.setStrokingColor(PRIMARY_COLOR);
        cs.setLineWidth(1);
        cs.moveTo(x, y + 15);
        cs.lineTo(x + width, y + 15);
        cs.stroke();

        // Footer text
        cs.beginText();
        cs.setFont(font, BODY_FONT_SIZE);
        cs.setNonStrokingColor(DARK_TEXT);
        cs.newLineAtOffset(x, y);
        cs.showText("Thank you for choosing Eventify!");
        cs.endText();

        cs.beginText();
        cs.setFont(lightFont, SMALL_FONT_SIZE);
        cs.setNonStrokingColor(LIGHT_TEXT);
        cs.newLineAtOffset(x, y - 15);
        cs.showText("Present this ticket at the venue entrance. For support contact: support@eventify.com");
        cs.endText();

        // Logo placeholder
        cs.beginText();
        cs.setFont(font, 10);
        cs.setNonStrokingColor(PRIMARY_COLOR);
        cs.newLineAtOffset(x + width - 60, y);
        cs.showText("EVENTIFY");
        cs.endText();
    }

    private static void addDetailBlock(PDPageContentStream cs, String label, String value, 
                                     float x, float y, PDType1Font headerFont, PDType1Font bodyFont) throws IOException {
        // Label
        cs.beginText();
        cs.setFont(headerFont, BODY_FONT_SIZE);
        cs.setNonStrokingColor(LIGHT_TEXT);
        cs.newLineAtOffset(x, y);
        cs.showText(label);
        cs.endText();

        // Value
        cs.beginText();
        cs.setFont(bodyFont, BODY_FONT_SIZE);
        cs.setNonStrokingColor(DARK_TEXT);
        cs.newLineAtOffset(x + 70, y);
        cs.showText(value);
        cs.endText();
    }
}