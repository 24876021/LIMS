package com.thematrix.labmanagement.common.utils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSAUtils工具类
 */
public class RSAUtils {

    private static final int MAX_ENCRYPT_BLOCK = 2048;
    private static final int MAX_DECRYPT_BLOCK = 2048;
    public static final String KEY_RSA = "RSA";
    public static final String KEY_RSA_PUBLICKEY = "RSAPublicKeyTian";
    public static final String KEY_RSA_PRIVATEKEY = "RSAPrivateKeyTian";
    private final static String KEY_RSA_SIGNATURE = "MD5withRSA";

    public static Map<String, Object> init() {
        Map<String, Object> map = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_RSA);
            generator.initialize(512);
            KeyPair keyPair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            map = new HashMap<String, Object>(2);

            map.put(KEY_RSA_PUBLICKEY, publicKey);
            map.put(KEY_RSA_PRIVATEKEY, privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static String getPublicKey(Map<String, Object> map) {
        String str = "";
        Key key = (Key) map.get(KEY_RSA_PUBLICKEY);
        str = encryptBase64(key.getEncoded());
        return str;
    }

    public static String getPrivateKey(Map<String, Object> map) {
        String str = "";
        Key key = (Key) map.get(KEY_RSA_PRIVATEKEY);
        str = encryptBase64(key.getEncoded());
        return str;
    }

    public static byte[] decryptBase64(String key) {
        return Base64.getDecoder().decode(key);
    }

    public static String encryptBase64(byte[] key) {
        return new String(Base64.getEncoder().encode(key));
    }

    public static String encryptByPublic(String encryptingStr, String publicKeyStr){
        try {
            byte[] publicKeyBytes = decryptBase64(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            byte[] data = encryptingStr.getBytes(StandardCharsets.UTF_8);
            KeyFactory factory;
            factory = KeyFactory.getInstance(KEY_RSA);
            PublicKey publicKey = factory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            return encryptBase64(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static String decryptByPrivateKey(String privateKeyStr, String encryptedStr){
        return decryptByPrivate(encryptedStr, privateKeyStr);
    }

    public static String decryptByPrivate(String encryptedStr, String privateKeyStr){
        try {
            byte[] privateKeyBytes = decryptBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            byte[] data = decryptBase64(encryptedStr);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String encryptByPrivate(String encryptingStr, String privateKeyStr){
        try {
            byte[] privateKeyBytes = decryptBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            byte[] data = encryptingStr.getBytes(StandardCharsets.UTF_8);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            return encryptBase64(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decryptByPublic(String encryptedStr, String publicKeyStr){
        try {
            byte[] publicKeyBytes = decryptBase64(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            byte[] data = decryptBase64(encryptedStr);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PublicKey publicKey = factory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String sign(String encryptedStr, String privateKey) {
        String str = "";
        try {
            byte[] data = encryptedStr.getBytes();
            byte[] bytes = decryptBase64(privateKey);
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PrivateKey key = factory.generatePrivate(pkcs);
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initSign(key);
            signature.update(data);
            str = encryptBase64(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean verify(String encryptedStr, String publicKey, String sign) {
        boolean flag = false;
        try {
            byte[] data = encryptedStr.getBytes();
            byte[] bytes = decryptBase64(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PublicKey key = factory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initVerify(key);
            signature.update(data);
            flag = signature.verify(decryptBase64(sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static String formatStr(String str,boolean flag){
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0,len=sb.length(); i < len; i++) {
            if(i%64==0){
                sb.insert(i, "\n");
            }
        }
        if(flag){
            sb = new StringBuilder("-----BEGIN PUBLIC KEY-----").append(sb).append("\n-----END PUBLIC KEY-----\n");
        }else {
            sb = new StringBuilder("-----BEGIN RSA PRIVATE KEY-----").append(sb).append("\n-----END RSA PRIVATE KEY-----\n");
        }
        return sb.toString();
    }
}
