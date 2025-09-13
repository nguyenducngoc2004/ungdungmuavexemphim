import java.io.*;
import java.util.*;

public class MovieManager {
    private static final String FILE = "movies.txt";
    public static class Movie {
        public String id, name, desc, director, genre, duration, seatCount, image;
        public Movie(String[] arr) {
            id = arr[0]; name = arr[1]; desc = arr[2]; director = arr[3];
            genre = arr[4]; duration = arr[5]; seatCount = arr[6]; image = arr[7];
        }
        public String toLine() {
            return String.join("|", id, name, desc, director, genre, duration, seatCount, image);
        }
    }
    private final List<Movie> movies = new ArrayList<>();
    public void load() throws IOException {
        movies.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null)
                movies.add(new Movie(line.split("\\|")));
        }
    }
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Movie m : movies)
                pw.println(m.toLine());
        }
    }
    public List<Movie> getAll() { return movies; }
    public void add(Movie m) { movies.add(m); }
    // Thêm các phương thức tìm kiếm, lọc, sửa, xóa...
}