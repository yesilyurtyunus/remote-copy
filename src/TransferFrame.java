import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class TransferFrame extends JFrame {
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JButton btnClose;

    private final String ip;
    private final int port;
    private final FileInfo targetFile;
    private final String localDestinationPath;

    public TransferFrame(String ip, int port, FileInfo targetFile, String localDestinationPath) {
        this.ip = ip;
        this.port = port;
        this.targetFile = targetFile;
        this.localDestinationPath = localDestinationPath;

        setTitle("Dosya Aktarılıyor...");
        setSize(400, 160);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();
        startTransfer();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblStatus = new JLabel("Aktarılan: " + targetFile.getName(), JLabel.CENTER);
        panel.add(lblStatus);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        btnClose = new JButton("Kapat");
        btnClose.setEnabled(false);
        btnClose.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnClose);

        add(panel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startTransfer() {
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Socket socket = new Socket(ip, port);
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                    // İsteği gönder
                    dos.writeUTF("DOWNLOAD");
                    dos.writeUTF(targetFile.getAbsolutePath());
                    dos.flush();

                    // 1. Adım: Dosya/Klasör var mı?
                    boolean exists = dis.readBoolean();
                    if (!exists) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(TransferFrame.this, "Sunucuda dosya bulunamadı!")
                        );
                        return null;
                    }

                    // 2. Adım: Klasör mü dosya mı?
                    boolean isDirectory = dis.readBoolean();

                    // 3. Adım: Toplam boyut
                    long totalSize = dis.readLong();

                    // Hedef dosya yolunu belirle
                    File destinationFile;
                    if (isDirectory) {
                        destinationFile = new File(localDestinationPath, targetFile.getName() + ".zip");
                    } else {
                        destinationFile = new File(localDestinationPath, targetFile.getName());
                    }

                    // 4. Adım: Veriyi oku ve diske yaz (Progress barı güncelle)
                    try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                        byte[] buffer = new byte[8192];
                        long totalRead = 0;
                        int count;

                        while (totalRead < totalSize && (count = dis.read(buffer, 0, (int) Math.min(buffer.length, totalSize - totalRead))) != -1) {
                            fos.write(buffer, 0, count);
                            totalRead += count;

                            int progress = (int) ((totalRead * 100) / totalSize);
                            publish(progress);
                        }
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                progressBar.setValue(latestProgress);
            }

            @Override
            protected void done() {
                lblStatus.setText("Aktarım Tamamlandı!");
                progressBar.setValue(100);
                btnClose.setEnabled(true);
            }
        };

        worker.execute();
    }
}