package beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@SessionScoped 
public class CalendarUtil implements Serializable {

    private static final List<String> dayHeaders = 
        Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    private YearMonth currentMonth = YearMonth.now();

    public List<String> getDayHeaders() {
        return dayHeaders;
    }

    public List<LocalDate> getDatesInMonth() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = currentMonth.atDay(1);

        // Backtrack to Monday
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.minusDays(1);
        }

        for (int i = 0; i < 42; i++) {
            dates.add(date);
            date = date.plusDays(1);
        }
        return dates;
    }

    public String getCurrentMonthName() {
        return currentMonth.getMonth() + " " + currentMonth.getYear();
    }

    public void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
    }

    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
    }
    
    public boolean isCurrentMonth(LocalDate date) {
  return YearMonth.from(date).equals(currentMonth);
}
}
