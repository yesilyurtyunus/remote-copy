import java.io.*;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileCommandHandler implements Runnable {
    private final Socket clientSocket;

    public FileCommandHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String command = dis.readUTF();

            if ("LIST".equalsIgnoreCase(command)) {
                String path = dis.readUTF();
                handleListCommand(path, dos);
            } else if ("DOWNLOAD".equalsIgnoreCase(command)) {
                String path = dis.readUTF();
                handleDownloadCommand(path, dos);
            }

        } catch (IOException e) {
            System.err.println("İstemci işlenirken hata oluştu: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handleListCommand(String path, DataOutputStream dos) throws IOException {
        File[] filesToSend = null;

        if ("DRIVES".equalsIgnoreCase(path)) {
            filesToSend = File.listRoots();
        } else {
            File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                filesToSend = directory.listFiles();
            }
        }

        if (filesToSend == null) {
            dos.writeInt(0);
            dos.flush();
            return;
        }

        // Önce kaç adet öğe göndereceğimizi yazıyoruz
        dos.writeInt(filesToSend.length);

        for (File file : filesToSend) {
            String name = ("DRIVES".equalsIgnoreCase(path)) ? file.getAbsolutePath() : file.getName();
            String absPath = file.getAbsolutePath();
            boolean isDir = file.isDirectory();
            long size = file.length();

            dos.writeUTF(name);
            dos.writeUTF(absPath);
            dos.writeBoolean(isDir);
            dos.writeLong(size);
        }
        dos.flush();
    }

    private void handleDownloadCommand(String path, DataOutputStream dos) throws IOException {
        File targetFile = new File(path);

        if (!targetFile.exists()) {
            dos.writeBoolean(false);
            dos.flush();
            return;
        }

        dos.writeBoolean(true);
        boolean isDirectory = targetFile.isDirectory();
        dos.writeBoolean(isDirectory);

        File fileToSend = targetFile;
        File tempZip = null;

        if (isDirectory) {
            tempZip = zipDirectory(targetFile);
            fileToSend = tempZip;
        }

        dos.writeLong(fileToSend.length());
        dos.flush();

        try (FileInputStream fis = new FileInputStream(fileToSend)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }

        if (tempZip != null && tempZip.exists()) {
            tempZip.delete();
        }
    }

    private File zipDirectory(File dir) throws IOException {
        File zipFile = File.createTempFile("transfer_", ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFileRec(dir, dir.getName(), zos);
        }
        return zipFile;
    }

    private void zipFileRec(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) return;
        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) {
                fileName += "/";
            }
            zos.putNextEntry(new ZipEntry(fileName));
            zos.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipFileRec(child, fileName + child.getName(), zos);
                }
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[8192];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            zos.closeEntry();
        }
    }
}