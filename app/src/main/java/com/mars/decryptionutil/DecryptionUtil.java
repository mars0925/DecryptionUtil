package com.mars.decryptionutil;

import android.util.Base64;


import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mars at 2019-07-05
 * 推播加解密
 */
public class DecryptionUtil {
    public static final String tag = DecryptionUtil.class.getSimpleName();


    /**
     * ASE265解密 - RFC2829金鑰向量編成
     * @param data 加密內容
     * @param salt salt
     * @param Password Password
     * @return 解密內容
     */
    public static String decrypt(String data, String salt, String Password) {
        Rfc2898DeriveBytes keyGenerator = null;
        String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";

        try {
            keyGenerator = new Rfc2898DeriveBytes(Password.getBytes(), salt.getBytes(), 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] bKey = keyGenerator.getBytes(32);
        byte[] bIv = keyGenerator.getBytes(16);
        byte[] datadata;

        try {
            SecretKey secretKey = new SecretKeySpec(bKey, "AES");
            AlgorithmParameterSpec param = new IvParameterSpec(bIv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, param);

            datadata = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(datadata, StandardCharsets.UTF_8);

        } catch (Exception e) {
            return null;
        }
    }

    //SHA1 加密实例

    /**
     * SHA1 加密方法
     * @param value 要加密的內容
     * @return 加密字串
     */
    public static String getSHA(String value) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] results = sha1.digest(value.getBytes(StandardCharsets.UTF_8));//加密
            StringBuilder stringBuilder = new StringBuilder();
            for (byte result : results) {
                stringBuilder.append(Integer.toString((result & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Created by Administrator on 2016/7/21.
     */
    public static class Rfc2898DeriveBytes {
        private Mac _hmacSha1;
        private byte[] _salt;
        private int _iterationCount;
        private byte[] _buffer = new byte[20];
        private int _bufferStartIndex = 0;
        private int _bufferEndIndex = 0;
        private int _block = 1;

        /**
         * Creates new instance.
         *
         * @param password   The password used to derive the key.
         * @param salt       The key salt used to derive the key.
         * @param iterations The number of iterations for the operation.
         * @throws NoSuchAlgorithmException HmacSHA1 algorithm cannot be found.
         * @throws InvalidKeyException      Salt must be 8 bytes or more. -or- Password cannot be null.
         */
        Rfc2898DeriveBytes(byte[] password, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeyException {
            if ((salt == null) || (salt.length < 8)) {
                throw new InvalidKeyException("Salt must be 8 bytes or more.");
            }
            if (password == null) {
                throw new InvalidKeyException("Password cannot be null.");
            }
            this._salt = salt;
            this._iterationCount = iterations;
            this._hmacSha1 = Mac.getInstance("HmacSHA1");
            this._hmacSha1.init(new SecretKeySpec(password, "HmacSHA1"));
        }

        /**
         * Creates new instance.
         *
         * @param password   The password used to derive the key.
         * @param salt       The key salt used to derive the key.
         * @param iterations The number of iterations for the operation.
         * @throws NoSuchAlgorithmException     HmacSHA1 algorithm cannot be found.
         * @throws InvalidKeyException          Salt must be 8 bytes or more. -or- Password cannot be null.
         */
        Rfc2898DeriveBytes(String password, byte[] salt, int iterations) throws InvalidKeyException, NoSuchAlgorithmException {
            this(password.getBytes(StandardCharsets.UTF_8), salt, iterations);
        }

        /**
         * Creates new instance.
         *
         * @param password The password used to derive the key.
         * @param salt     The key salt used to derive the key.
         * @throws NoSuchAlgorithmException     HmacSHA1 algorithm cannot be found.
         * @throws InvalidKeyException          Salt must be 8 bytes or more. -or- Password cannot be null.
         */
        public Rfc2898DeriveBytes(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException {
            this(password, salt, 0x3e8);
        }

        /**
         * Returns a pseudo-random key from a password, salt and iteration count.
         *
         * @param count Number of bytes to return.
         * @return Byte array.
         */
        byte[] getBytes(int count) {
            byte[] result = new byte[count];
            int resultOffset = 0;
            int bufferCount = this._bufferEndIndex - this._bufferStartIndex;

            if (bufferCount > 0) { //if there is some data in buffer
                if (count < bufferCount) { //if there is enough data in buffer
                    System.arraycopy(this._buffer, this._bufferStartIndex, result, 0, count);
                    this._bufferStartIndex += count;
                    return result;
                }
                System.arraycopy(this._buffer, this._bufferStartIndex, result, 0, bufferCount);
                this._bufferStartIndex = this._bufferEndIndex = 0;
                resultOffset += bufferCount;
            }

            while (resultOffset < count) {
                int needCount = count - resultOffset;
                this._buffer = this.func();
                if (needCount > 20) { //we one (or more) additional passes
                    System.arraycopy(this._buffer, 0, result, resultOffset, 20);
                    resultOffset += 20;
                } else {
                    System.arraycopy(this._buffer, 0, result, resultOffset, needCount);
                    this._bufferStartIndex = needCount;
                    this._bufferEndIndex = 20;
                    return result;
                }
            }
            return result;
        }

        private byte[] func() {
            this._hmacSha1.update(this._salt, 0, this._salt.length);
            byte[] tempHash = this._hmacSha1.doFinal(getBytesFromInt(this._block));

            this._hmacSha1.reset();
            byte[] finalHash = tempHash;
            for (int i = 2; i <= this._iterationCount; i++) {
                tempHash = this._hmacSha1.doFinal(tempHash);
                for (int j = 0; j < 20; j++) {
                    finalHash[j] = (byte) (finalHash[j] ^ tempHash[j]);
                }
            }
            if (this._block == 2147483647) {
                this._block = -2147483648;
            } else {
                this._block += 1;
            }

            return finalHash;
        }

        private byte[] getBytesFromInt(int i) {
            return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
        }
    }
}
