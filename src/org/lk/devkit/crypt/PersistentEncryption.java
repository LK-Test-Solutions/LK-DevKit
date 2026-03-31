package org.lk.devkit.crypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PersistentEncryption {

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // GCM size (random part of the encryption)
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length in bits

    /**
     * Generate the AES key once and save it to a file
     *
     * @param keyFilePath storage path
     * @throws IOException              when write to file operation failed
     * @throws NoSuchAlgorithmException when the key generation failed
     */
    public static void generateAndStoreKey(String keyFilePath) throws IOException, NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();

        try (FileOutputStream fos = new FileOutputStream(keyFilePath)) {
            fos.write(secretKey.getEncoded());
        }
    }

    private static SecretKey loadKeyFromFile(String keyFilePath) throws IOException {
        byte[] keyBytes = Files.readAllBytes(new File(keyFilePath).toPath());
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plaintext, String keyFilePath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = loadKeyFromFile(keyFilePath);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

        // Generate initialize vector (IV)
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());

        // Encrypt IV and combine it with the encrypted text
        byte[] encryptedMessage = new byte[IV_SIZE + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedMessage, 0, IV_SIZE);
        System.arraycopy(encryptedBytes, 0, encryptedMessage, IV_SIZE, encryptedBytes.length);

        // Encode the encrypted key in Base64 format
        return Base64.getEncoder().encodeToString(encryptedMessage);

    }

    public static String decrypt(String base64Ciphertext, String keyFilePath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = loadKeyFromFile(keyFilePath);

        byte[] encryptedMessage = Base64.getDecoder().decode(base64Ciphertext);

        // Extract IV and encrypted text
        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedBytes = new byte[encryptedMessage.length - IV_SIZE];
        System.arraycopy(encryptedMessage, 0, iv, 0, IV_SIZE);
        System.arraycopy(encryptedMessage, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

}