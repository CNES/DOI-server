/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.security;

import fr.cnes.doi.exception.DoiRuntimeException;
import fr.cnes.doi.utils.Utils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to encrypt/decrypt
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class UtilsCryptography {

    /**
     * Name of the transformation.
     */
    public static final String CIPHER_ALGORITHM = "AES";

    /**
     * Name of the secret-key algorithm to be associated with the given key material.
     */
    public static final String KEY_ALGORITHM = CIPHER_ALGORITHM;

    /**
     * Default key to encrypt/decrypt. The key is a 16 bits length key
     */
    public static final String DEFAULT_SECRET_KEY = "16BYTESSECRETKEY";

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(UtilsCryptography.class.getName());

    /**
     * Template message.
     */
    private static final String MSG_TPL = "Parameter : {}";

    /**
     * Template message.
     */
    private static final String MSG_TPL2 = "Parameters : {} and {}";

    /**
     * Private constructor
     */
    private UtilsCryptography() {
        //not called
    }

    /**
     * Decrypts the string with the DEFAULT_SECRET_KEY.
     *
     * @param encryptedInput string to decrypt.
     * @return the decrypted string
     */
    public static String decrypt(final String encryptedInput) {
        LOG.traceEntry(MSG_TPL, encryptedInput);
        return LOG.traceExit(decrypt(encryptedInput, DEFAULT_SECRET_KEY));
    }

    /**
     * Decrypts the string with a custom secret key.
     *
     * @param encryptedInput string to decrypt.
     * @param secretKey the secret key.
     * @return the decrypted string
     */
    public static String decrypt(final String encryptedInput,
            final String secretKey) {
        LOG.traceEntry(MSG_TPL2, encryptedInput, secretKey);
        if (Utils.isNotEmpty(encryptedInput)) {
            try {
                final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        new SecretKeySpec(
                                secretKey.getBytes(StandardCharsets.UTF_8),
                                KEY_ALGORITHM)
                );
                return LOG.traceExit(new String(
                        cipher.doFinal(Base64.getDecoder().decode(encryptedInput)),
                        StandardCharsets.UTF_8
                ));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidKeyException | IllegalBlockSizeException
                    | BadPaddingException ex) {
                throw LOG.throwing(new DoiRuntimeException(ex));
            }
        } else {
            throw LOG.throwing(new DoiRuntimeException("Cannot decrypt empty input"));
        }
    }

    /**
     * Encrypts the string with the DEFAULT_SECRET_KEY.
     *
     * @param str string to encrypt
     * @return the encrypted string.
     */
    public static String encrypt(final String str) {
        LOG.traceEntry(MSG_TPL, str);
        return LOG.traceExit(encrypt(str, DEFAULT_SECRET_KEY));
    }

    /**
     * Encrypts the string with a specific secret key.
     *
     * @param str string to encrypt
     * @param secretKey the secret key of 16 bits length
     * @return the decrypted string
     */
    public static String encrypt(final String str,
            final String secretKey) {
        LOG.traceEntry(MSG_TPL2, str, secretKey);
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM)
            );
            return LOG.traceExit(Base64.getEncoder().encodeToString(
                    cipher.doFinal(str.getBytes("UTF-8"))
            ));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | UnsupportedEncodingException
                | IllegalBlockSizeException | BadPaddingException ex) {
            throw LOG.throwing(new DoiRuntimeException(ex));
        }
    }
}
