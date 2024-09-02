package com.grace.granos.util;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class EncryptUtil {
	private static final String key = "20232023202320232023202320232023"; // 密钥（16 字节长度）
	private static final String ALGORITHM = "AES";

	public String md5(String src, int salt) {
		// 這邊使用了springframework的加密方式
		// md5DigestAsHex參數是Bytes，所以透過java String類將字串轉為Bytes
		String result = src + Integer.toString(salt * 2023);
		return DigestUtils.md5DigestAsHex(result.getBytes());
	}

	// 使用 AES 加密算法加密字符串
	public String encrypt(String value) {
		try {
			Key ekey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, ekey);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String decrypt(String encryptedValue) {
        try {
            Key ekey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, ekey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
