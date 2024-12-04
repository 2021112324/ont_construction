package org.example.Ind_equ.method.strings;

public interface TranslateMethod {
    static boolean isEnglishWords(String str)
    {
        // 正则表达式匹配英文、数字、空格和特定符号
        String regex = "^[a-zA-Z0-9\\s\\-]+$";
        return str.matches(regex);
    }

    String translateClassification(String english);

    void writeEnglishWord();


    String processCategory(String category);
}
