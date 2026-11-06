import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * Uzak bilgisayardaki dosyaları listeleyen, diskler arası geçiş sağlayan gezgin ekranı.
 */
public class ExplorerFrame extends JFrame {
    private final String ip;
    private final int port;
    private String currentPath = "DRIVES"; // Başlangıçta tüm sürücüleri listeler

    private JTextField txtCurrentPath;
    private JList<FileInfo> fileJList;
    private DefaultListModel<FileInfo> listModel;
    private JButton btnDownload;
    private JButton btnUp;
    private JButton btnDrives;
    private final NetworkService networkService;

    public ExplorerFrame(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.networkService = new NetworkService();

        setTitle("Uzak Dosya Gezgini - " + ip);
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadDirectoryAsync(currentPath);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Üst Kısım: Navigasyon Paneli (Sürücüler, Üst Dizin, Adres Çubuğu, Git)
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        btnDrives = new JButton("💻 Sürücüler");
        btnUp = new JButton("⬆ Üst Dizin");
        navButtonPanel.add(btnDrives);
        navButtonPanel.add(btnUp);

        txtCurrentPath = new JTextField(currentPath);
        JButton btnGo = new JButton("Git");

        JPanel addressBarPanel = new JPanel(new BorderLayout());
        addressBarPanel.add(txtCurrentPath, BorderLayout.CENTER);
        addressBarPanel.add(btnGo, BorderLayout.EAST);

        topPanel.add(navButtonPanel, BorderLayout.WEST);
        topPanel.add(addressBarPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Orta Kısım: Dosya Listesi
        listModel = new DefaultListModel<>();
        fileJList = new JList<>(listModel);
        fileJList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(fileJList), BorderLayout.CENTER);

        // Alt Kısım: İndirme Butonu
        JPanel bottomPanel = new JPanel();
        btnDownload = new JButton("Seçili Öğeyi Bu Bilgisayara Kopyala");
        btnDownload.setFont(new Font("SansSerif", Font.BOLD, 12));
        bottomPanel.add(btnDownload);
        add(bottomPanel, BorderLayout.SOUTH);

        // Çift Tıklama İle Klasör İçine Girme
        fileJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    FileInfo selected = fileJList.getSelectedValue();
                    if (selected != null && selected.isDirectory()) {
                        currentPath = selected.getAbsolutePath();
                        txtCurrentPath.setText(currentPath);
                        loadDirectoryAsync(currentPath);
                    }
                }
            }
        });

        // Buton Aksiyonları
        btnGo.addActionListener(e -> {
            currentPath = txtCurrentPath.getText().trim();
            loadDirectoryAsync(currentPath);
        });

        btnDrives.addActionListener(e -> {
            currentPath = "DRIVES";
            txtCurrentPath.setText(currentPath);
            loadDirectoryAsync(currentPath);
        });

        btnUp.addActionListener(e -> {
            if ("DRIVES".equals(currentPath)) return;
            File parent = new File(currentPath).getParentFile();
            if (parent != null) {
                currentPath = parent.getAbsolutePath();
            } else {
                currentPath = "DRIVES";
            }
            txtCurrentPath.setText(currentPath);
            loadDirectoryAsync(currentPath);
        });

        btnDownload.addActionListener(e -> onSelectFile());
    }

    private void loadDirectoryAsync(String path) {
        listModel.clear();
        listModel.addElement(new FileInfo("Yükleniyor...", "", false, 0));

        SwingWorker<List<FileInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FileInfo> doInBackground() throws Exception {
                return networkService.fetchDirectoryContent(ip, port, path);
            }

            @Override
            protected void done() {
                try {
                    List<FileInfo> files = get();
                    listModel.clear();
                    for (FileInfo f : files) {
                        listModel.addElement(f);
                    }
                } catch (Exception e) {
                    listModel.clear();
                    JOptionPane.showMessageDialog(ExplorerFrame.this,
                            "Dizin okunamadı!\n" + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onSelectFile() {
        FileInfo selectedFile = fileJList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Lütfen kopyalanacak bir dosya veya klasör seçin!");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Kaydedilecek Yerel Klasörü Seçin");

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String localDestinationPath = chooser.getSelectedFile().getAbsolutePath();

            TransferFrame transferFrame = new TransferFrame(ip, port, selectedFile, localDestinationPath);
            transferFrame.setVisible(true);
        }
    }
}