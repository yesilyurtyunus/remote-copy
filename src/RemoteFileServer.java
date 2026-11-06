import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Sunucu uygulamasının ana giriş noktası (Main sınıfı).
 */
public class RemoteFileServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Sunucu başlatılıyor... Port: " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Sunucu dinlemede. İstemciler bağlanabilir.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni istemci bağlandı: " + clientSocket.getInetAddress());

                // Her istemciyi ayrı bir thread'de karşıla
                new Thread(new FileCommandHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu hatası: " + e.getMessage());
        }
    }
}