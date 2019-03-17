/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.izpack.processor;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.util.Base64;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author malapert
 */
public class PasswordEncryptionProcessor implements Processor {
    private Cipher encryptCipher;

    private static final Logger logger = Logger.getLogger(fr.cnes.doi.izpack.processor.PasswordEncryptionProcessor.class.getName());

    /**
     * Processes the contend of an input field.
     *
     * @param client the client object using the services of this processor.
     * @return The result of the encryption.
     * @throws IzPackException if encryption fails
     */
    @Override
    public String process(ProcessingClient client)
    {                
        try {
            //Generate the key bytes
            byte[] keyBytes = "16BYTESSECRETKEY".getBytes(StandardCharsets.UTF_8);
            SecretKeySpec specKey = new SecretKeySpec(keyBytes, "AES");
            //Initialize the encryption cipher
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, specKey);
            byte[] cryptedbytes = encryptCipher.doFinal(client.getFieldContents(0).getBytes("UTF-8"));
            return Base64.encodeBytes(cryptedbytes);            
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.log(Level.WARNING, "Failed to encrypt password: " + ex, ex);
            throw new IzPackException("Failed to encrypt password: " + ex.getMessage(), ex);
        }

    }

}
