package org.delcom.ifs25035springbootcashflow.adapter.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FileStorage {

    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat folder upload: " + root, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    /**
     * Menyimpan file dan mengembalikan path relatif URL, misalnya /uploads/uuid.png.
     */
    public String save(String originalName, byte[] data) throws IOException {
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new IOException("Format file harus gambar (png, jpg, jpeg, gif, webp)");
        }
        String name = UUID.randomUUID() + "." + ext;
        Files.write(root.resolve(name), data);
        return "/uploads/" + name;
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}