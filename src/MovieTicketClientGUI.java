import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class MovieTicketClientGUI extends JFrame {
    private JPanel loginPanel, mainPanel;
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton, registerButton;

    private JComboBox<String> movieComboBox;
    private JTextField quantityField;
    private JButton buyButton;
    private JTextArea resultArea, movieInfoArea;
    private JLabel imageLabel;
    private String[] movieList;
    private String[][] movieDetails; // [i][0]=name,...,[i][6]=img
    private String serverHost = "localhost";
    private int serverPort = 12345;
    private String username = "";

    public MovieTicketClientGUI() {
        setTitle("Bán vé xem phim");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Đăng nhập/Đăng ký Panel
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7,7,7,7);

        c.gridx = 0; c.gridy = 0; loginPanel.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx = 1; userField = new JTextField(15); loginPanel.add(userField, c);

        c.gridx = 0; c.gridy = 1; loginPanel.add(new JLabel("Mật khẩu:"), c);
        c.gridx = 1; passField = new JPasswordField(15); loginPanel.add(passField, c);

        c.gridx = 0; c.gridy = 2;
        loginButton = new JButton("Đăng nhập");
        loginPanel.add(loginButton, c);
        c.gridx = 1;
        registerButton = new JButton("Đăng ký");
        loginPanel.add(registerButton, c);

        add(loginPanel);

        // Sự kiện login/register
        loginButton.addActionListener(e -> handleLoginOrRegister(true));
        registerButton.addActionListener(e -> handleLoginOrRegister(false));

        // Main panel sau khi đăng nhập thành công
        mainPanel = new JPanel(new BorderLayout());

        // Top panel chọn phim, nhập số vé, nút mua
        JPanel inputPanel = new JPanel(new GridLayout(3,2,5,5));
        movieComboBox = new JComboBox<>();
        quantityField = new JTextField();
        buyButton = new JButton("Mua vé");
        inputPanel.add(new JLabel("Chọn phim:"));
        inputPanel.add(movieComboBox);
        inputPanel.add(new JLabel("Số lượng vé:"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(buyButton);

        // Khu vực hiện info và ảnh phim
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 220));
        movieInfoArea = new JTextArea(7, 40);
        movieInfoArea.setEditable(false);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(imageLabel, BorderLayout.WEST);
        infoPanel.add(new JScrollPane(movieInfoArea), BorderLayout.CENTER);

        // Kết quả
        resultArea = new JTextArea(3, 40);
        resultArea.setEditable(false);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        movieComboBox.addActionListener(e -> showMovieInfo(movieComboBox.getSelectedIndex()));
        buyButton.addActionListener(e -> buyTicket());
    }

    private void handleLoginOrRegister(boolean isLogin) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên và mật khẩu!");
            return;
        }
        try (
            Socket socket = new Socket(serverHost, serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println(isLogin ? "LOGIN" : "REGISTER");
            out.println(user);
            out.println(pass);

            String resp = in.readLine();
            if (("LOGIN_SUCCESS".equals(resp) && isLogin) || ("REGISTER_SUCCESS".equals(resp) && !isLogin)) {
                username = user;
                // Nhận danh sách phim
                String movieString = in.readLine();
                parseMovieList(movieString);
                setContentPane(mainPanel);
                revalidate();
                repaint();
                // Hiển thị info phim đầu tiên
                if (movieList.length > 0) showMovieInfo(0);
                resultArea.setText(isLogin ? "Đăng nhập thành công!" : "Đăng ký thành công! Đã tự động đăng nhập.");
            } else {
                JOptionPane.showMessageDialog(this, isLogin ? "Sai tài khoản hoặc mật khẩu!" : "Tên tài khoản đã tồn tại!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không kết nối được server!");
        }
    }

    private void parseMovieList(String movieString) {
        String[] movieStrArr = movieString.split(";");
        movieList = new String[movieStrArr.length];
        movieDetails = new String[movieStrArr.length][];
        for (int i = 0; i < movieStrArr.length; i++) {
            String[] details = movieStrArr[i].split("\\|");
            movieDetails[i] = details;
            movieList[i] = details[0];
        }
        movieComboBox.setModel(new DefaultComboBoxModel<>(movieList));
    }

    private void showMovieInfo(int index) {
        if (movieDetails != null && index >= 0 && index < movieDetails.length && movieDetails[index] != null) {
            String[] d = movieDetails[index];
            String info = "Tên phim: " + d[0] + "\n"
                        + "Mô tả: " + d[1] + "\n"
                        + "Đạo diễn: " + d[2] + "\n"
                        + "Thể loại: " + d[3] + "\n"
                        + "Thời lượng: " + d[4] + " phút\n"
                        + "Vé còn lại: " + d[5];
            movieInfoArea.setText(info);

            // Hiển thị ảnh
            try {
                ImageIcon icon;
                if (d[6].startsWith("http")) {
                    BufferedImage img = ImageIO.read(new URL(d[6]));
                    icon = new ImageIcon(img.getScaledInstance(150, 220, Image.SCALE_SMOOTH));
                } else {
                    BufferedImage img = ImageIO.read(new File(d[6]));
                    icon = new ImageIcon(img.getScaledInstance(150, 220, Image.SCALE_SMOOTH));
                }
                imageLabel.setIcon(icon);
            } catch (Exception ex) {
                imageLabel.setIcon(null);
            }
        } else {
            movieInfoArea.setText("Không có thông tin.");
            imageLabel.setIcon(null);
        }
    }

    private void buyTicket() {
        int idx = movieComboBox.getSelectedIndex();
        if (movieList == null || idx < 0 || movieDetails[idx] == null) {
            resultArea.setText("Không có phim hoặc không kết nối được server.");
            return;
        }
        String movieName = movieDetails[idx][0];
        String quantity = quantityField.getText().trim();
        if (quantity.isEmpty()) {
            resultArea.setText("Vui lòng nhập số lượng vé!");
            return;
        }
        try (
            Socket socket = new Socket(serverHost, serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Gửi lại thông tin đăng nhập để xác thực session (nếu muốn bảo mật hơn!)
            out.println("LOGIN"); out.println(username); out.println(""); // pass không cần vì đã đăng nhập

            String loginResp = in.readLine(); // Bỏ qua
            in.readLine(); // Bỏ qua danh sách phim

            // Gửi yêu cầu mua vé
            out.println(movieName);
            out.println(quantity);

            String response = in.readLine();
            resultArea.setText(response);

            // Sau khi mua, cập nhật lại danh sách phim
            // (Đăng nhập lại để lấy vé cập nhật số lượng)
            Socket socket2 = new Socket(serverHost, serverPort);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
            out2.println("LOGIN"); out2.println(username); out2.println("");
            in2.readLine(); // Đọc login resp
            String movieString = in2.readLine();
            parseMovieList(movieString);
            showMovieInfo(movieComboBox.getSelectedIndex());
            socket2.close();
        } catch (Exception ex) {
            resultArea.setText("Kết nối tới server thất bại!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MovieTicketClientGUI().setVisible(true);
        });
    }
}