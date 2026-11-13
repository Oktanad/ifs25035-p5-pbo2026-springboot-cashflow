package org.delcom.ifs25035springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {

    @TempDir
    Path tmp;

    @Test
    void savesAllowedImage() throws Exception {
        FileStorage storage = new FileStorage(tmp.resolve("up").toString());
        String url = storage.save("Foto.PNG", new byte[]{1, 2, 3});

        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.endsWith(".png"));
        Path saved = storage.getRoot().resolve(url.substring("/uploads/".length()));
        assertTrue(Files.exists(saved));
    }

    @Test
    void rejectsInvalidNamesAndFormats() {
        FileStorage storage = new FileStorage(tmp.resolve("up").toString());
        byte[] data = {1};
        assertThrows(IOException.class, () -> storage.save(null, data));
        assertThrows(IOException.class, () -> storage.save("tanpaekstensi", data));
        assertThrows(IOException.class, () -> storage.save("berakhirtitik.", data));
        assertThrows(IOException.class, () -> storage.save("virus.exe", data));
    }

    @Test
    void constructorFailsWhenRootCannotBeCreated() throws Exception {
        Path file = Files.createFile(tmp.resolve("bukanfolder"));
        assertThrows(IllegalStateException.class,
                () -> new FileStorage(file.resolve("sub").toString()));
    }
}