
package fr.cnes.doi.persistence.util;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Classe d'encryptage de mot de passe
 */
public class PasswordEncrypter {
    /** Password encryption key */
    private final transient SecretKeySpec pwdEncryptKey;

    /** Unique static instance (Singleton pattern) */
    private static PasswordEncrypter instance = null;

    /**
     * Private constructor (Singleton pattern)
     */
    private PasswordEncrypter() {
        byte[] lKsPwdSecret = "Doiserver2018!99".getBytes();
        pwdEncryptKey = new SecretKeySpec(lKsPwdSecret, "AES");
        Arrays.fill(lKsPwdSecret, (byte) 0); // flush secret key
        lKsPwdSecret = null;
    }

    /**
     * Get the static unique instance of PasswordEncrypter
     * 
     * @return Static unique instance of PasswordEncrypter
     */
    public static PasswordEncrypter getInstance() {
        if (instance == null) {
            instance = new PasswordEncrypter();
        }
        return instance;
    }

    /**
     * Get a Cipher to encrypt/decrypt passwords
     * 
     * @return Cipher to encrypt/decrypt passwords
     * @throws Exception
     *             If Cipher could not be created
     */
    private Cipher getCipher() throws Exception {
        return Cipher.getInstance("AES/ECB/PKCS5Padding");
    }

    /**
     * Decrypt the given password WARNING! For security reasons, the caller MUST flush the returned
     * clear-text password as soon it can
     * 
     * @param pEncryptedPasswd
     *            Encrypted password to decrypt
     * @return Clear decrypted password
     * @throws Exception
     *             Exception upon password retrieving.
     */
    public final byte[] decryptPasswd(byte[] pEncryptedPasswd) throws Exception {
        // Decrypt the given password
        Cipher lCipher = getCipher();
        lCipher.init(Cipher.DECRYPT_MODE, pwdEncryptKey);
        return lCipher.doFinal(pEncryptedPasswd);
    }

    /**
     * Décrypte le mot de passe.
     * 
     * @param pEncryptedPasswd
     *            Encrypted password to decrypt
     * @return Clear decrypted password
     * @throws Exception
     *             Exception upon password retrieving.
     */
    public String decryptPasswd(String pEncryptedPasswd) throws Exception {
        return new String(decryptPasswd(DatatypeConverter.parseBase64Binary(pEncryptedPasswd)));
    }

    /**
     * Encrypts the given password WARNING! Specified plain-text password is flushed by this method
     * for security reasons
     * 
     * @param pClearPasswd
     *            The clear password to encrypt
     * @return Encrypted password
     * @throws Exception
     *             Exception upon password setting
     */
    public final byte[] encryptPasswd(byte[] pClearPasswd) throws Exception {
        // Encrypt the specified password so that it can be stored in memory 
        // for application life time
        Cipher lCipher = getCipher();
        lCipher.init(Cipher.ENCRYPT_MODE, pwdEncryptKey);

        byte[] lEncryptedPwd = lCipher.doFinal(pClearPasswd);

        // Flush clear password from memory 
        Arrays.fill(pClearPasswd, (byte) 0);
        pClearPasswd = null;
        return lEncryptedPwd;
    }

    /**
     * Encrypte le mot de passe.
     * 
     * @param pClearPasswd
     *            The clear password to encrypt
     * @return Encrypted password
     * @throws Exception
     *             Exception upon password setting
     */
    public String encryptPasswd(String pClearPasswd) throws Exception {
        return DatatypeConverter.printBase64Binary(encryptPasswd(pClearPasswd.getBytes()));
    }
}
