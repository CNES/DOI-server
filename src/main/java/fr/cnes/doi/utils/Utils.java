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

import fr.cnes.doi.logging.shell.ShellHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Utility Class.
 * @author Jean-Christophe Malapert
 * @author Claire
 */
public class Utils {

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
     * Default key to encrypt/decrypt.
     * The key is a 16 bits length key
     */
    public static final String DEFAULT_SECRET_KEY = "16BYTESSECRETKEY";

    /**
     * Name of the logger in console without date. 
     */
    public static final String SHELL_LOGGER_NAME = "fr.cnes.doi.logging.shell";

    /**
     * Name of the logger for http requests and answers.
     */
    public static final String HTTP_LOGGER_NAME = "fr.cnes.doi.logging.api";

    /**
     * Name of the logger for applicative logs.
     */
    public static final String APP_LOGGER_NAME = "fr.cnes.doi.logging.app";

    /**
     * Logger for applicative logs.
     */
    private static Logger appLogger;

    /**
     * Logger in the console to display msg info like help.
     */
    private static Logger shellLogger;

    /**
     * Checks whether the char sequence is empty.
     * @param cs the char sequence
     * @return True when the char sequence is empty otherwise False
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks whether the char sequence is not empty.
     * @param cs the char sequence
     * @return True when the char sequence is not empty otherwise False
     */    
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

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
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes("UTF-8"), KEY_ALGORITHM));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedInput)), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Encrypts the string with the DEFAULT_SECRET_KEY.     
     * @param str string to encrypt
     * @return the encrypted string.
     */
    public static String encrypt(final String str) {
        return encrypt(str, DEFAULT_SECRET_KEY);
    }

    /**
     * Encrypts the string with a specific secret key.
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

    /**
     * Gets the shell logger to log in the console without date     
     * @return the logger
     */
    public static Logger getShellLogger() {

        if (shellLogger == null) {
            shellLogger = Logger.getLogger(SHELL_LOGGER_NAME);
            shellLogger.addHandler(new ShellHandler());
            shellLogger.setUseParentHandlers(false);
        }
        return shellLogger;
    }

    /**
     * Gets the application logger to log in the specific file for applicative
     * messages     
     * @return the logger
     */
    public static Logger getAppLogger() {
        if (appLogger == null) {
            appLogger = Logger.getLogger(APP_LOGGER_NAME);
        }
        return appLogger;
    }
}
