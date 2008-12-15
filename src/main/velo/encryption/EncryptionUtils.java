/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.encryption;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.log4j.Logger;

public class EncryptionUtils {
    private transient static Logger logger = Logger.getLogger(EncryptionUtils.class.getName());
    
    //private static final String KEY_STRING = "193-155-248-97-234-56-100-241";
    private static String KEY_STRING;
    
    public static String encrypt(String source, String keyString) {
        try {
            logger.trace("Encrypting string '" + source + "' with key: '" + keyString + "'");
            
            logger.trace("Creating a key...");
            // Get our secret key
            Key key = getKey(keyString);
            
            logger.trace("Key was created, creating a cipher...");
            // Create the cipher
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            
            logger.trace("Cipher created, initializing the cipher for encryption...");
            // Initialize the cipher for encryption
            desCipher.init(Cipher.ENCRYPT_MODE, key);
            
            // Our cleartext as bytes
            byte[] cleartext = source.getBytes();
            
            // Encrypt the cleartext
            byte[] ciphertext = desCipher.doFinal(cleartext);
            
            // Return a String representation of the cipher text
            return getString(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String generateKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("TripleDes");
            SecretKey desKey = keygen.generateKey();
            byte[] bytes = desKey.getEncoded();
            return getString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String decrypt(String source, String keyString) {
        try {
            // Get our secret key
            Key key = getKey(keyString);
            
            // Create the cipher
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            
            // Encrypt the cleartext
            byte[] ciphertext = getBytes(source);
            
            // Initialize the same cipher for decryption
            desCipher.init(Cipher.DECRYPT_MODE, key);
            
            // Decrypt the ciphertext
            byte[] cleartext = desCipher.doFinal(ciphertext);
            
            // Return the clear text
            return new String(cleartext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static Key getKey(String keyString) {
        try {
            //byte[] bytes = getBytes(KEY_STRING);
            byte[] bytes = getBytes(keyString);
            DESKeySpec pass = new DESKeySpec(bytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            SecretKey s = skf.generateSecret(pass);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Returns true if the specified text is encrypted, false otherwise
     */
    public static boolean isEncrypted( String text ) {
        // If the string does not have any separators then it is not
        // encrypted
        if( text.indexOf( '-' ) == -1 ) {
            ///System.out.println( "text is not encrypted: no dashes" );
            return false;
        }
        
        StringTokenizer st = new StringTokenizer( text, "-", false );
        while( st.hasMoreTokens() ) {
            String token = st.nextToken();
            if( token.length() > 3 ) {
                //System.out.println( "text is not encrypted: length of token greater than 3: " + token );
                return false;
            }
            for( int i=0; i<token.length(); i++ ) {
                if( !Character.isDigit( token.charAt( i ) ) ) {
                    //System.out.println( "text is not encrypted: token is not a digit" );
                    return false;
                }
            }
        }
        //System.out.println( "text is encrypted" );
        return true;
    }
    
    private static String getString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append((int) (0x00FF & b));
            if (i + 1 < bytes.length) {
                sb.append("-");
            }
        }
        return sb.toString();
    }
    
    private static byte[] getBytes(String str) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(str, "-", false);
        while (st.hasMoreTokens()) {
            int i = Integer.parseInt(st.nextToken());
            bos.write((byte) i);
        }
        return bos.toByteArray();
    }
    

    /*
    public static void main(String[] args) {
        String fileName = "c:/data/dev/edm/edm_conf/keys/admins_key.key";
        String key;
        try {
            key = FileUtils.readLine(fileName);
            System.out.print(EncryptionUtils.encrypt("asdfsadf",key));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        //System.out.print("lll");
        
    }
     */
    /*
    public static void main( String[] args ) {
        if( args.length < 1 ) {
            System.out.println( "Usage: EncryptionUtils <command> <args>" );
            System.out.println( "\t<command>: encrypt, decrypt, generate-key" );
            System.exit( 0 );
        }
        String command = args[ 0 ];
        if( command.equalsIgnoreCase( "generate-key" ) ) {
            System.out.println( "New key: " +
                EncryptionUtils.generateKey() );
        } else if( command.equalsIgnoreCase( "encrypt" ) ) {
            String plaintext = args[ 1 ];
            System.out.println( plaintext + " = " +
                EncryptionUtils.encrypt( plaintext ) );
        } else if( command.equalsIgnoreCase( "decrypt" ) ) {
            String ciphertext = args[ 1 ];
            System.out.println( ciphertext + " = " +
                EncryptionUtils.decrypt( ciphertext ) );
        }
    }
     */
    
    
    
    /* Works, for test porpuses
    public static void main( String[] args ) {
        String key = EncryptionUtils.generateKey();
     
        System.out.println("KEY: " + key);
        String clearPassword = "myPassw0rd!";
        System.out.println("Clear text password is: '" + clearPassword + "'");
        String encryptedPassword = EncryptionUtils.encrypt(clearPassword, key);
        System.out.println("Encrypted password is: '" + encryptedPassword + "'");
        String decryptedPassword = EncryptionUtils.decrypt(encryptedPassword, key);
        System.out.println("Decrypted password is: '" + decryptedPassword + "'");
    }
     */
     
    
    
    public static void setKey(String key) {
        KEY_STRING = key;
    }
    
    
    public static void showProviders() {
        try {
            Provider[] providers = Security.getProviders();
            for (int i = 0; i < providers.length; i++) {
                System.out.println("Provider: " + providers[i].getName() + ", "
                    + providers[i].getInfo());
                for (Iterator itr = providers[i].keySet().iterator(); itr
                    .hasNext();) {
                    String key = (String) itr.next();
                    String value = (String) providers[i].get(key);
                    System.out.println("\t" + key + " = " + value);
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}