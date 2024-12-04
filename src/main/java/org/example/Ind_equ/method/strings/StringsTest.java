package org.example.Ind_equ.method.strings;

import org.example.Ind_equ.method.strings.impl.TranslateMethodImpl;

public class StringsTest {
    public static void main(String[] args) {
        TranslateMethod translator = new TranslateMethodImpl();
        String english = "CCD-CMOS照相机";
        System.out.println(translator.processCategory(english));
//        System.out.println(english);
//        String chinese = translator.translateClassification(english);
//        String chinese2 = translator.translateClassification(english);
//        System.out.println("English: " + english + ", Chinese: " + chinese);
        translator.writeEnglishWord();
    }
}
