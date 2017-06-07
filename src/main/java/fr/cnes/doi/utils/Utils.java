/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Utils {

    public static final String CIPHER_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM = "AES";
    public static final String DEFAULT_SECRET_KEY = "16BYTESSECRETKEY";

    
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Decrypts the input.
     *
     * @param encryptedInput string to decrypt.
     * @return the decrypted string
     */
    public static String decrypt(final String encryptedInput) {
        return decrypt(encryptedInput, DEFAULT_SECRET_KEY);
    }
    
    public static String decrypt(final String encryptedInput, final String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes("UTF-8"), KEY_ALGORITHM));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedInput)), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException |IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }    

    /**
     * Encrypts the string.
     *
     * @param str string to encrypt
     * @return the encrypted string.
     */
    public static String encrypt(final String str) {
        return encrypt(str, DEFAULT_SECRET_KEY);
    }
    
    public static String encrypt(final String str, final String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getBytes("UTF-8"), KEY_ALGORITHM));
            return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }    
}
