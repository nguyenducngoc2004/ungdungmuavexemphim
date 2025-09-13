import java.io.*;
import java.net.*;
import java.util.*;

class Movie {
    String name, description, director, genre, imagePath;
    int duration, tickets;
    public Movie(String name, String description, String director, String genre, int duration, int tickets, String imagePath) {
        this.name = name;
        this.description = description;
        this.director = director;
        this.genre = genre;
        this.duration = duration;
        this.tickets = tickets;
        this.imagePath = imagePath;
    }
    public String toJson() {
        // name|desc|director|genre|duration|tickets|imagePath
        return name + "|" + description + "|" + director + "|" + genre + "|" + duration + "|" + tickets + "|" + imagePath;
    }
}

public class MovieTicketServer {
    private static Map<String, Movie> movies = Collections.synchronizedMap(new LinkedHashMap<>());
    private static Map<String, String> users = Collections.synchronizedMap(new HashMap<>());
    private static final String USER_FILE = "users.txt";

    public static void main(String[] args) throws IOException {
        loadUsers();

        movies.put("Avengers: Endgame", new Movie("Avengers: Endgame", "Hành động, siêu anh hùng", "Anthony Russo", "Hành động", 181, 10, "images/avengers.jpg"));
        movies.put("Inception", new Movie("Inception", "Phiêu lưu, tâm lý", "Christopher Nolan", "Khoa học viễn tưởng", 148, 8, "images/inception.jpg"));
        movies.put("Interstellar", new Movie("Interstellar", "Du hành không gian, cảm động", "Christopher Nolan", "Khoa học viễn tưởng", 169, 7, "images/interstellar.jpg"));

        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server is running...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    private static void loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] sp = line.split("\\|");
                if (sp.length == 2) users.put(sp[0], sp[1]);
            }
        } catch (IOException e) {}
    }

    private static void saveUser(String username, String password) {
        users.put(username, password);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(username + "|" + password);
            bw.newLine();
        } catch (IOException e) {}
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        public ClientHandler(Socket socket) { this.socket = socket; }
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Xử lý đăng nhập/đăng ký
                String loginCommand = in.readLine(); // "LOGIN" hoặc "REGISTER"
                String username = in.readLine();
                String password = in.readLine();

                synchronized (users) {
                    if ("REGISTER".equals(loginCommand)) {
                        if (users.containsKey(username)) {
                            out.println("REGISTER_FAIL");
                            return;
                        }
                        saveUser(username, password);
                        out.println("REGISTER_SUCCESS");
                    } else if ("LOGIN".equals(loginCommand)) {
                        if (!users.containsKey(username) || !users.get(username).equals(password)) {
                            out.println("LOGIN_FAIL");
                            return;
                        }
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("FAIL");
                        return;
                    }
                }

                // Gửi danh sách phim và thông tin chi tiết
                StringBuilder movieList = new StringBuilder();
                for (Movie m : movies.values()) {
                    movieList.append(m.toJson()).append(";");
                }
                out.println(movieList.toString());

                // Xử lý đặt vé
                String movieName = in.readLine();
                String soLuongStr = in.readLine();
                int soLuong = 0;
                try { soLuong = Integer.parseInt(soLuongStr.trim()); } catch (Exception e) {
                    out.println("Số lượng không hợp lệ."); return;
                }

                synchronized (movies) {
                    if (!movies.containsKey(movieName)) {
                        out.println("Phim không tồn tại.");
                    } else {
                        Movie m = movies.get(movieName);
                        if (soLuong <= m.tickets && soLuong > 0) {
                            m.tickets -= soLuong;
                            out.println("Mua thành công " + soLuong + " vé phim '" + movieName + "'. Cảm ơn bạn!");
                        } else {
                            out.println("Không đủ vé hoặc số lượng không hợp lệ. Vé còn lại: " + m.tickets);
                        }
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
            finally { try { socket.close(); } catch (IOException e) {} }
        }
    }
}