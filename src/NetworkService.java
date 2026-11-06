import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkService {

    public List<FileInfo> fetchDirectoryContent(String ip, int port, String remotePath) throws Exception {
        List<FileInfo> fileList = new ArrayList<>();

        try (Socket socket = new Socket(ip, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            // Komutu ve yolu gönder
            dos.writeUTF("LIST");
            dos.writeUTF(remotePath);
            dos.flush();

            // Sunucudan gelen dosya sayısını oku
            int count = dis.readInt();

            for (int i = 0; i < count; i++) {
                String name = dis.readUTF();
                String absolutePath = dis.readUTF();
                boolean isDirectory = dis.readBoolean();
                long size = dis.readLong();

                fileList.add(new FileInfo(name, absolutePath, isDirectory, size));
            }
        }
        return fileList;
    }
}