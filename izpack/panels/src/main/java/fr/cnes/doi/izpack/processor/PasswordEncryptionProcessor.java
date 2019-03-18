/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
 * Crypting password
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
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
        final String textToCrypt = client.getFieldContents(0);
        final String result;
        if(textToCrypt.isEmpty()) {
            result = "";
        } else {
            result = crypt(textToCrypt);
        }
        return result;
    }
    
    private String crypt(final String textToCrypt) {
        try {
            //Generate the key bytes
            byte[] keyBytes = "16BYTESSECRETKEY".getBytes(StandardCharsets.UTF_8);
            SecretKeySpec specKey = new SecretKeySpec(keyBytes, "AES");
            //Initialize the encryption cipher
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, specKey);
            byte[] cryptedbytes = encryptCipher.doFinal(textToCrypt.getBytes("UTF-8"));
            return Base64.encodeBytes(cryptedbytes);            
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.log(Level.WARNING, "Failed to encrypt password: " + ex, ex);
            throw new IzPackException("Failed to encrypt password: " + ex.getMessage(), ex);
        }        
    }

}
