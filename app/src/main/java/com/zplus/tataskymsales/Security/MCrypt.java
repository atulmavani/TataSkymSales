package com.zplus.tataskymsales.Security;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class MCrypt {

   // private String iv = "fedcba9876543210"; //Dummy iv (CHANGE IT!)
    private IvParameterSpec ivspec;
    private SecretKeySpec keyspec;
    private Cipher cipher;

   // private String SecretKey = "0123456789abcdef"; //Dummy secretKey (CHANGE IT!)

    public MCrypt(String SecretKey, String iv)
    {
        ivspec = new IvParameterSpec(iv.getBytes());
        keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String encrypt(String text) throws Exception
    {
        if(text == null || text.length() == 0)
            throw new Exception("Empty string");

        byte[] encrypted = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

            encrypted = cipher.doFinal(padString(text).getBytes());
        }
        catch (Exception e)
        {
            throw new Exception("[encrypt] " + e.getMessage());
        }
        return new String(Base64.encode(encrypted,0));
    }

    public String decrypt(String code) throws Exception
    {
        if(code == null || code.length() == 0)
            throw new Exception("Empty string");

        byte[] decrypted = null;

        try {
            byte[] cipherData = Base64.decode(code,0);
            //output
            /*{41, 47, 4, 73, -110, 48, -33, 2, -51, -70, -29, 69, -124, 11, -99, 55, 61, -128, 62, 66, -14, 87, -90, -118, -2, -123,
                    68, -118, -40, -55, 15, -46, -121, 1, -56, 62, -107, 29, 34, 18, 109, -61, -2, 118, -13, 16, -98, 63}*/

            //byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            decrypted = cipher.doFinal(cipherData);
            //output
            /*{91, 34, 57, 51, 51, 55, 51, 51, 54, 51, 52, 57, 34, 44, 34, 122, 111, 108, 116, 97, 50, 48, 50, 48, 34, 44, 34, 77, 79,
                    66, 87, 69, 66, 34, 93}*/
        }
        catch (Exception e)
        {
                throw new Exception("[decrypt] " + e.getMessage());
        }
        return new String(decrypted, "UTF-8");
        //output return below string
        // ["9337336349","zolta2020","MOBWEB"]
    }

    public static String bytesToHex(byte[] data)
    {
        if (data==null) return null;

        int len = data.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if ((data[i]&0xFF)<16)
                str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
        }
        return str;
    }

    public static byte[] hexToBytes(String str)
    {
        if (str==null) {
            return null;
        }
        else if (str.length() < 2) {
            return null;
        }
        else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i=0; i<len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
            }
            return buffer;
        }
    }

    private static String padString(String source)
    {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++)
        {
            source += paddingChar;
        }

        return source;
    }
}
