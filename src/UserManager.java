import java.io.*;
import java.util.*;

public class UserManager {
    private static final String FILE = "users.txt";
    private Map<String, String> users = new HashMap<>();
    private Map<String, String> roles = new HashMap<>();

    public void load() throws IOException {
        users.clear(); roles.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split("\\|");
                if (s.length >= 3) {
                    users.put(s[0], s[1]);
                    roles.put(s[0], s[2]);
                }
            }
        }
    }
    public void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (String u : users.keySet()) {
                pw.println(u + "|" + users.get(u) + "|" + roles.get(u));
            }
        }
    }
    public boolean checkLogin(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
    public boolean addUser(String username, String password, String role) {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        roles.put(username, role);
        return true;
    }
    public String getRole(String username) {
        return roles.get(username);
    }
}