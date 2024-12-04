package org.example.Ind_equ.method.strings;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Random;

public interface StringsMethod {
    static String generate_resource_url_hashcode(String str) {
        try {
            String input = System.currentTimeMillis() + "-" + new Random().nextInt(1000000) + str;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

     /**
     * 清理字符串，去除特俗字符，使其适合作为OWL本体的标识符，同时保留中文字符。
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    static String cleanForOWLIri(String input) {
         if (input == null) {
            return "";
        }
        // 去除前后空格
        String cleaned = input.trim();
        // 替换空格为下划线
        cleaned = cleaned.replaceAll("\\s+", "_");
        // 去除或替换特殊字符，保留中文字符
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]", "");
//         转换为小写（注意：中文字符不会被转换）
//        cleaned = cleaned.toLowerCase();
        return cleaned;
    }
     /**
     * 清理字符串，使其适合作为OWL本体的标识符，同时保留中文字符和一些常见特殊字符。
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    static String weekCleanForOWLIri(String input) {
        if (input == null) {
            return "";
        }
        // 去除前后空格
        String cleaned = input.trim();
        // 替换空格为下划线
        cleaned = cleaned.replaceAll("\\s+", "_");
        // 去除可能会造成隐患的特殊字符
        cleaned = cleaned.replaceAll("[!！@#$￥%^&…*()（）{}【】<>《》?？/·、；：‘“’”，。|~`]", "-");
//        cleaned = encodeUri(cleaned);
        // 转换为小写（注意：中文字符不会被转换）
        // cleaned = cleaned.toLowerCase();
        return cleaned;
    }

    static String encodeUri(String uri) {
        try {
            return URLEncoder.encode(uri, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding URI: " + uri, e);
        }
    }

    static String normalizeToNfc(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFC);
    }

}
