import java.io.*;
import java.net.*;

public class MovieTicketClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);

            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
            ) {
                String fromServer;
                // Nhận danh sách phim và hướng dẫn từ server
                while ((fromServer = in.readLine()) != null) {
                    System.out.println(fromServer);
                    if (fromServer.contains("Nhập số thứ tự phim muốn mua vé:")) {
                        String selection = stdIn.readLine();
                        out.println(selection);
                    }
                    if (fromServer.contains("Nhập số lượng vé muốn mua:")) {
                        String quantity = stdIn.readLine();
                        out.println(quantity);
                    }
                    if (fromServer.startsWith("Mua thành công") || fromServer.startsWith("Không đủ vé") || fromServer.startsWith("Lựa chọn không hợp lệ") || fromServer.startsWith("Số lượng không hợp lệ")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Không thể kết nối tới server: " + e.getMessage());
        }
    }
}