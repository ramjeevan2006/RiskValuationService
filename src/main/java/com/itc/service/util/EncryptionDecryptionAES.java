package com.itc.service.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionDecryptionAES {

	public static String encrypt(String plainText) {

		// String encryptedText = null;
		// try {
		// KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		// keyGenerator.init(128);
		// SecretKey secretKey = keyGenerator.generateKey();
		// Cipher cipher = Cipher.getInstance("AES");
		//
		// byte[] plainTextByte = plainText.getBytes();
		// cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		// byte[] encryptedByte = cipher.doFinal(plainTextByte);
		// Base64.Encoder encoder = Base64.getEncoder();
		// encryptedText = encoder.encodeToString(encryptedByte);
		// } catch (Exception e) {
		// encryptedText = null;
		// }
		// return encryptedText;

		String generatedHash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte[] bytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			// Get complete hashed password in hex format
			generatedHash = sb.toString().trim();
			return generatedHash;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}