package org.delcom.ifs25035springbootcashflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppMainTest {

    @Test
    void mainStartsApplication() {
        assertDoesNotThrow(() -> App.main(new String[]{
                "--spring.profiles.active=test",
                "--server.port=0"}));
    }
}