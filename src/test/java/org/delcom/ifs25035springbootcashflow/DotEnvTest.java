package org.delcom.ifs25035springbootcashflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DotEnvTest {

    @TempDir
    Path tmp;

    @Test
    void loadsValuesAndSkipsInvalidLines() throws Exception {
        Path file = tmp.resolve(".env");
        Files.write(file, List.of(
                "# komentar",
                "",
                "TANPA_SAMA_DENGAN",
                "CF_TEST_KEY = halo ",
                "PATH=harus-diabaikan"));
        try {
            DotEnv.load(file);
            assertEquals("halo", System.getProperty("CF_TEST_KEY"));
            assertNull(System.getProperty("PATH"));
            assertNull(System.getProperty("TANPA_SAMA_DENGAN"));

            Files.write(file, List.of("CF_TEST_KEY=baru"));
            DotEnv.load(file);
            assertEquals("halo", System.getProperty("CF_TEST_KEY"));
        } finally {
            System.clearProperty("CF_TEST_KEY");
        }
    }

    @Test
    void missingFileIsIgnored() {
        assertDoesNotThrow(() -> DotEnv.load(tmp.resolve("tidakada.env")));
    }

    @Test
    void defaultLoadDoesNotFail() {
        assertDoesNotThrow(() -> DotEnv.load());
    }
}