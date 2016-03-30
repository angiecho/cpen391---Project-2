package team22.messagingapp;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

class AESEncryption {
    static String text = "TESTINGWITH16BTS"; // 16 byte blocks
    static String key; // 16 bytes
    static String iv; // 16 bytes

    public static byte[] encrypt(String text) throws Exception {
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("US-ASCII"), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("US-ASCII"));

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        return cipher.doFinal(text.getBytes("US-ASCII"));
    }

    public static String decrypt(byte[] text) throws Exception{
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("US-ASCII"), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("US-ASCII"));

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        return new String(cipher.doFinal(text), "US-ASCII");
    }

    /*
        Print the encrypted cipher.
     */
    public static void print_cipher(byte[] cipher ,int length){
        for(int i = 0; i < length; i++){
            System.out.print(new Integer(cipher[i]));
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

            byte[] cipher = encrypt(text);

            System.out.print("cipher:  ");
            print_cipher(cipher, cipher.length);

            String decryption = decrypt(cipher);
            System.out.println("decryption: " + decryption);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}