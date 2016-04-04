package team22.messagingapp;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

class AESEncryption {
    static String text = "TESTINGWITH16BTS"; // 16 byte block
    static String key; // 16 bytes
    static String iv; // 16 bytes
    public static final String FORMAT = "US-ASCII";

    // requires 16 bytes key and iv
    public static byte[] encrypt(String text, String key, String iv) throws Exception {
        System.out.println("Key:" + key + ", IV:" + iv);
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(FORMAT), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes(FORMAT));

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        return cipher.doFinal(text.getBytes(FORMAT));
    }

    public static String decrypt(byte[] text, String key, String iv) throws Exception{
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(FORMAT), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes(FORMAT));

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        return new String(cipher.doFinal(text), FORMAT);
    }

    /*
        Print the encrypted cipher.
     */
    public static void print_cipher(byte[] cipher ,int length){
        for(int i = 0; i < length; i++){
            System.out.print((int) (cipher[i]) + " ");
        }
        System.out.println("");
    }

    /*
       PRECOND: Bluetooth set up
       Set key and IV based on gps/touchscreen data and do something with the touchscreen while we're at it.
    */
    public static String get_gps() throws Exception{
        return "1234567890qwerty";
    }

    public static String get_ts() throws Exception{
        return "uiopasdfghjklzxc";
    }

    public static void main(String[] args) {
        try {
            System.out.println("Encryption Test Start\\n");
            key = get_gps();
            iv = get_ts();

            System.out.println("text: " + text);

            byte[] cipher = encrypt(text, key, iv);

            System.out.print("cipher:  ");
            print_cipher(cipher, cipher.length);

            String decryption = decrypt(cipher, key, iv);
            System.out.println("decryption: " + decryption);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}