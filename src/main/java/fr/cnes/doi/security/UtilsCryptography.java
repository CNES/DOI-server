/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.security;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.utils.Utils;
import static fr.cnes.doi.utils.Utils.isNotEmpty;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to encrypt/decrypt
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class UtilsCryptography {
    
    /**
     * Name of the transformation.
     */
    public static final String CIPHER_ALGORITHM = "AES";

    /**
     * Name of the secret-key algorithm to be associated with the given key
     * material.
     */
    public static final String KEY_ALGORITHM = "AES";

    /**
     * Default key to encrypt/decrypt. The key is a 16 bits length key
     */
    public static final String DEFAULT_SECRET_KEY = "16BYTESSECRETKEY";

    /**
     * Decrypts the string with the DEFAULT_SECRET_KEY.
     *
     * @param encryptedInput string to decrypt.
     * @return the decrypted string
     */
    public static String decrypt(final String encryptedInput) {
        return decrypt(encryptedInput, DEFAULT_SECRET_KEY);
    }

    /**
     * Decrypts the string with a custom secret key.
     *
     * @param encryptedInput string to decrypt.
     * @param secretKey the secret key.
     * @return the decrypted string
     */
    public static String decrypt(final String encryptedInput, final String secretKey) {
        if (isNotEmpty(encryptedInput)) {
            try {
                Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes("UTF-8"), KEY_ALGORITHM));
                return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedInput)), "UTF-8");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                throw new DoiRuntimeException(ex);
            }
        } else {
            throw new DoiRuntimeException("Cannot decrypt empty input");
        }

    }

    /**
     * Encrypts the string with the DEFAULT_SECRET_KEY.
     *
     * @param str string to encrypt
     * @return the encrypted string.
     */
    public static String encrypt(final String str) {
        return encrypt(str, DEFAULT_SECRET_KEY);
    }

    /**
     * Encrypts the string with a specific secret key.
     *
     * @param str string to encrypt
     * @param secretKey the secret key of 16 bits length
     * @return the decrypted string
     */
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
