import java.io.*;
import java.util.*;

public class ReviewManager {
    private static final String FILE = "reviews.txt";
    public static class Review {
        public String reviewId, username, movieId, rating, comment, time;
        public Review(String[] arr) {
            reviewId = arr[0]; username = arr[1]; movieId = arr[2];
            rating = arr[3]; comment = arr[4]; time = arr[5];
        }
        public String toLine() {
            return String.join("|", reviewId, username, movieId, rating, comment, time);
        }
    }
    private final List<Review> reviews = new ArrayList<>();
    public void load() throws IOException {
        reviews.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null)
                reviews.add(new Review(line.split("\\|")));
        }
    }
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Review r : reviews)
                pw.println(r.toLine());
        }
    }
    public void add(Review r) { reviews.add(r); }
    public List<Review> getMovieReviews(String movieId) {
        List<Review> res = new ArrayList<>();
        for (Review r : reviews) if (r.movieId.equals(movieId)) res.add(r);
        return res;
    }
    // Thêm thống kê, xếp hạng trung bình...
}