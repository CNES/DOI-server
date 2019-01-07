package fr.cnes.doi.persistence.util;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Encrypt {

    /**
     * @param args TODO
     */
    public static void main(String[] args) throws Exception{
        
        // TODO Auto-generated method stub
        System.out.println(
            DatatypeConverter.printBase64Binary(encryptPasswd("doiserver".getBytes())));
    }
    
    public static final byte[] encryptPasswd(byte[] pClearPasswd) throws Exception {
        // Encrypt the specified password so that it can be stored in memory 
        // for application life time
        Cipher lCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] lKsPwdSecret = "Doiserver2018!99".getBytes();
        final SecretKeySpec pwdEncryptKey = new SecretKeySpec(lKsPwdSecret, "AES");
        lCipher.init(Cipher.ENCRYPT_MODE, pwdEncryptKey);

        byte[] lEncryptedPwd = lCipher.doFinal(pClearPasswd);

        // Flush clear password from memory 
        Arrays.fill(pClearPasswd, (byte) 0);
        pClearPasswd = null;
        return lEncryptedPwd;
    }
}
