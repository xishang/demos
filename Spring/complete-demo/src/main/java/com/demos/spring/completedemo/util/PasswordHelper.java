package com.demos.spring.completedemo.util;

import org.apache.commons.lang3.StringUtils;

public class PasswordHelper {

    public static final String HASH_ALGORITHM = DigestUtils.MD5;
    public static final int HASH_INTERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    /**
     * 生成安全的密码，生成随机的16位salt并经过1024次 MD5(or SHA1) hash
     */
    public static String entryptPassword(String plainPassword) {
        if (StringUtils.isBlank(plainPassword)) {
            return plainPassword;
        }
        String plain = EncodeUtils.unescapeHtml(plainPassword);
        byte[] salt = DigestUtils.generateSalt(SALT_SIZE);
        byte[] hashPassword = null;
        if (HASH_ALGORITHM.equals(DigestUtils.MD5)) {
            hashPassword = DigestUtils.md5(plain.getBytes(), salt, HASH_INTERATIONS);
        } else if (HASH_ALGORITHM.equals(DigestUtils.SHA1)) {
            hashPassword = DigestUtils.sha1(plain.getBytes(), salt, HASH_INTERATIONS);
        }
        return EncodeUtils.encodeHex(salt) + EncodeUtils.encodeHex(hashPassword);
    }

    /**
     * 验证密码
     *
     * @param plainPassword 明文密码
     * @param password      密文密码
     * @return 验证成功返回true
     */
    public static boolean validatePassword(String plainPassword, String password) {
        String plain = EncodeUtils.unescapeHtml(plainPassword);
        byte[] salt = EncodeUtils.decodeHex(password.substring(0, 16));
        byte[] hashPassword = null;
        if (HASH_ALGORITHM.equals(DigestUtils.MD5)) {
            hashPassword = DigestUtils.md5(plain.getBytes(), salt, HASH_INTERATIONS);
        } else if (HASH_ALGORITHM.equals(DigestUtils.SHA1)) {
            hashPassword = DigestUtils.sha1(plain.getBytes(), salt, HASH_INTERATIONS);
        }
        return password.equals(EncodeUtils.encodeHex(salt) + EncodeUtils.encodeHex(hashPassword));
    }

}
