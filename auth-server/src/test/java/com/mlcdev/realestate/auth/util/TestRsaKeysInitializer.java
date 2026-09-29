package com.mlcdev.realestate.auth.util;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Generates a fresh RSA key pair per test run
 */
public class TestRsaKeysInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            Path publicKeyFile = writePem("public-key", "PUBLIC KEY", keyPair.getPublic().getEncoded());
            Path privateKeyFile = writePem("private-key", "PRIVATE KEY", keyPair.getPrivate().getEncoded());

            TestPropertyValues.of(
                    "security.jwt.public-key=file:" + publicKeyFile,
                    "security.jwt.private-key=file:" + privateKeyFile
            ).applyTo(context);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Failed to generate test RSA key pair", e);
        }
    }

    private Path writePem(String prefix, String label, byte[] derEncoded) throws IOException {
        Path file = Files.createTempFile(prefix, ".pem");
        file.toFile().deleteOnExit();
        String base64 = Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(derEncoded);
        Files.writeString(file, "-----BEGIN " + label + "-----" + System.lineSeparator()
                + base64 + System.lineSeparator()
                + "-----END " + label + "-----" + System.lineSeparator());
        return file;
    }
}
