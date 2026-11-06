import java.io.Serializable;

/**
 * Sunucu ve İstemci arasında dosya/klasör metadatasını taşımak için kullanılan model sınıfı.
 */
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String absolutePath;
    private final boolean isDirectory;
    private final long size;

    public FileInfo(String name, String absolutePath, boolean isDirectory, long size) {
        this.name = name;
        this.absolutePath = absolutePath;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        // Swing JList veya JTree içerisinde düzgün görünmesi için
        return (isDirectory ? "[KLASÖR] " : "[DOSYA] ") + name + (isDirectory ? "" : " (" + size + " byte)");
    }
}