import javax.swing.*;
import java.awt.*;

/**
 * Kullanıcıdan IP ve Port bilgilerini alan ilk giriş ekranı.
 */
public class ConnectionFrame extends JFrame {
    private JTextField txtIp;
    private JTextField txtPort;
    private JButton btnConnect;

    public ConnectionFrame() {
        setTitle("Uzak Bilgisayara Bağlan");
        setSize(350, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ekranın ortasında açılır
        setLayout(new GridLayout(4, 2, 10, 10)); // Satır sayısını 4 yaptık (boşluklar için)

        add(new JLabel("  IP Adresi:"));
        txtIp = new JTextField("127.0.0.1"); // Varsayılan Localhost
        add(txtIp);

        add(new JLabel("  Port:"));
        txtPort = new JTextField("5000");
        add(txtPort);

        add(new JLabel("")); // Boşluk
        btnConnect = new JButton("Bağlan");
        add(btnConnect);

        // ActionListener kesin olarak butona bağlanıyor
        btnConnect.addActionListener(e -> onConnect());
    }

    private void onConnect() {
        try {
            String ip = txtIp.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());

            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "IP adresi boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Gezgin ekranını açıyoruz
            ExplorerFrame explorer = new ExplorerFrame(ip, port);
            explorer.setVisible(true);

            // Giriş penceresini hafızadan ve ekrandan kaldır
            this.dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçersiz port numarası!", "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Bağlantı penceresi açılırken hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConnectionFrame().setVisible(true);
        });
    }
}