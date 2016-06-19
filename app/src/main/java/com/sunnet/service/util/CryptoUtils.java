package com.sunnet.service.util;

import android.util.Base64;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String CIPHER_ALGORITHM_KHS = "AES/CBC/PKCS7Padding";
    private static final String ALGORITHM_OLD = "AES/ECB/PKCS7Padding";

    private static final String SECRET_KEY_ALGORITHM = "AES";
    public static final String SHA_1 = "SHA-1";
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_384 = "SHA-384";
    public static final String SHA_512 = "SHA-512";

    private static final int ITERATIONS_OLD = 1000;
    private static final int ITERATIONS_KHS = 1000;
    private static final int SALT_LOOP = 2;

    private static final int KEY_128_LENGTH = 128;
    private static final int KEY_192_LENGTH = 192;
    private static final int KEY_256_LENGTH = 256;
    private static final int KEY_LENGTH = KEY_128_LENGTH;

    public static final boolean isEncryptMyList = true;
    public static final byte[] iv16BytesDefault = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /**
     * *********************** SHA @algorithm [SHA-1 :160 bits hash], [SHA-256], [SHA-384], [SHA-512] *****************************
     */
    public static String getSecurePassword(String algorithm, String passwordToHash, String salt) {
        algorithm = algorithm.trim();
        passwordToHash = passwordToHash.trim();
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            if (salt != null)
                md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static String getSecureSalt(String algorithm, String password, String saltInput) {
        String salt = null;
        for (int i = 0; i < SALT_LOOP; i++) {
            if (salt == null)
                salt = getSecurePassword(algorithm, password, saltInput);
            else
                salt = getSecurePassword(algorithm, salt, saltInput);
        }
        return salt;
    }


    /**
     * ********************** Generate KEY = PBKDF2(passWord + Salt) *****************************
     */
    public static String getKeyPassword(String algorithm, String saltInput, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        algorithm = algorithm.trim();
        password = password.trim();
        String passwordSecure = getSecurePassword(algorithm, password, saltInput);
        String saltSecure = getSecureSalt(algorithm, password, saltInput);
        if (passwordSecure == null || saltSecure == null)
            return null;

        char[] chars = passwordSecure.toCharArray();
        byte[] salt = saltSecure.getBytes();
        byte[] hash;

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(chars), salt, ITERATIONS_OLD);
        KeyParameter key = (KeyParameter) generator.generateDerivedMacParameters(KEY_LENGTH);
        hash = key.getKey();

        return toHex(hash);
    }

    public static String toHex(byte[] array) throws NoSuchAlgorithmException {
        return byteArrayToHexString(array);
    }

    /************************* KHS *******************************/
    /**
     * converts given byte array to a hex string
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10)
                buffer.append("0");
            buffer.append(Long.toString((int) bytes[i] & 0xff, 16));
        }
        return buffer.toString();
    }

    /**
     * converts given hex string to a byte array
     * (ex: "0D0A" => {0x0D, 0x0A,})
     *
     * @param str
     * @return
     */
    public static final byte[] hexStringToByteArray(String str) {
        int i = 0;
        byte[] results = new byte[str.length() / 2];
        for (int k = 0; k < str.length(); ) {
            results[i] = (byte) (Character.digit(str.charAt(k++), 16) << 4);
            results[i] += (byte) (Character.digit(str.charAt(k++), 16));
            i++;
        }
        return results;
    }

    public static SecretKey genKeyRandom(int outputKeyLengthByBits) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM);
        keyGenerator.init(outputKeyLengthByBits, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static String genKeyRandomString(int outputKeyLengthByBits) throws NoSuchAlgorithmException {
        SecretKey key = genKeyRandom(outputKeyLengthByBits);
        return byteArrayToHexString(key.getEncoded());
    }

    public static String genSalt(int outputKeyLengthByBytes) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte salt[] = new byte[outputKeyLengthByBytes];
        random.nextBytes(salt);
        return byteArrayToHexString(salt);
    }

    public static String encrypt(String dataEncrypt, String keyStr, String ivStr) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(keyStr), new IvParameterSpec(hexStringToByteArray(ivStr)));
            return Base64.encodeToString(cipher.doFinal(dataEncrypt.getBytes("UTF-8")), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptReturnValueWhenError(String dataEncrypt, String keyStr, String ivStr) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(keyStr), new IvParameterSpec(hexStringToByteArray(ivStr)));
            return Base64.encodeToString(cipher.doFinal(dataEncrypt.getBytes("UTF-8")), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEncrypt;
    }

    public static String encryptReturnValueWhenError(String dataEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(ConfigApi.KEY_CRYPTO), new IvParameterSpec(hexStringToByteArray(ConfigApi.IV_CRYPTO)));
            return Base64.encodeToString(cipher.doFinal(dataEncrypt.getBytes("UTF-8")), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEncrypt;
    }

    public static String decrypt(String dataEncrypted, String keyStr, String ivStr) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(keyStr), new IvParameterSpec(hexStringToByteArray(ivStr)));
            return new String(cipher.doFinal(Base64.decode(dataEncrypted, Base64.NO_WRAP)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptReturnValueWhenError(String dataEncrypted, String keyStr, String ivStr) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(keyStr), new IvParameterSpec(hexStringToByteArray(ivStr)));
            return new String(cipher.doFinal(Base64.decode(dataEncrypted, Base64.NO_WRAP)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEncrypted;
    }

    public static String decryptReturnValueWhenError(String dataEncrypted) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(ConfigApi.KEY_CRYPTO), new IvParameterSpec(hexStringToByteArray(ConfigApi.IV_CRYPTO)));
            return new String(cipher.doFinal(Base64.decode(dataEncrypted, Base64.NO_WRAP)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEncrypted;
    }

    /************************************************************/

    public static Cipher getCipher(String keyStr, String ivStr, int opMode) throws Exception {
        if (Utils.isEmptyString(keyStr))
            return null;

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_KHS, "BC");
        cipher.init(opMode, generateKey(keyStr), new IvParameterSpec(hexStringToByteArray(ivStr)));
        return cipher;
    }

    public static Cipher getCipher(String keyStr, int opMode) throws Exception {
        if (Utils.isEmptyString(keyStr))
            return null;

        Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM_OLD);
        Cipher c = Cipher.getInstance(ALGORITHM_OLD, "BC");
        c.init(opMode, key);
        return c;
    }

    public static String encrypt(String valueToEnc, Cipher c) throws Exception {
        //Log.i(CryptoUtils.class.getSimpleName() + " value encrypt: " + valueToEnc);
        if (Utils.isEmptyString(valueToEnc))
            return valueToEnc;
        return Base64.encodeToString(c.doFinal(valueToEnc.getBytes("UTF8")), Base64.NO_WRAP);
    }

    public static String decrypt(String valueToDec, Cipher c) throws Exception {
        //Log.i(CryptoUtils.class.getSimpleName() + " value decrypt: " + valueToDec);
        if (Utils.isEmptyString(valueToDec))
            return valueToDec;
        return new String(c.doFinal(Base64.decode(valueToDec, Base64.NO_WRAP)));
    }

    private static Key generateKey(String keyStr) throws Exception {
        Key key = new SecretKeySpec(hexStringToByteArray(keyStr), CIPHER_ALGORITHM_KHS);
        return key;
    }


}
