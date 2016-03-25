import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class AESEncryption {
    static String text = "01110TESTING TESTING\0\0\0";
    static String key = "1234567890qwerty";
    static String IV = "uiopasdfghjklzxc";

    public byte[] encrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("US-ASCII"), "AES");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes("US-ASCII"));

        cipher.init(Cipher.ENCRYPT_MODE, secret_key, iv);
        return cipher.doFinal(text.getBytes("US-ASCII"));
    }

    public String decrypt(byte[] text, String key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("US-ASCII"), "AES");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes("US-ASCII"));

        cipher.init(Cipher.DECRYPT_MODE, secret_key, iv);
        return new String(cipher.doFinal(text), "US-ASCII");
    }

    public print_cipher(byte[] cipher ,int length){
        for(int i = 0; i < lenght; i++){
            System.out.print(new Integer(cipher[i]));
        }
        System.out.println("");
    }

    public void main(String[] args) {
        try {
            System.out.println("Encryption Test Start\\n");
            System.out.println("text: " + text);

            byte[] cipher = encrypt(header, key);

            System.out.print("cipher:  ");
            print_cipher(cipher, cipher.length);

            String decryption = decrypt(cipher, key);
            System.out.println("decryption: " + decryption);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}