import java.io.*;
import java.util.*;

public class TicketManager {
    private static final String FILE = "tickets.txt";
    public static class Ticket {
        public String ticketId, username, movieId, seat, status, time;
        public Ticket(String[] arr) {
            ticketId = arr[0]; username = arr[1]; movieId = arr[2];
            seat = arr[3]; status = arr[4]; time = arr[5];
        }
        public String toLine() {
            return String.join("|", ticketId, username, movieId, seat, status, time);
        }
    }
    private final List<Ticket> tickets = new ArrayList<>();
    public void load() throws IOException {
        tickets.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null)
                tickets.add(new Ticket(line.split("\\|")));
        }
    }
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Ticket t : tickets)
                pw.println(t.toLine());
        }
    }
    public void add(Ticket t) { tickets.add(t); }
    public List<Ticket> userTickets(String username) {
        List<Ticket> res = new ArrayList<>();
        for (Ticket t : tickets) if (t.username.equals(username)) res.add(t);
        return res;
    }
    // Thêm hủy vé, đổi vé, tìm kiếm ghế...
}