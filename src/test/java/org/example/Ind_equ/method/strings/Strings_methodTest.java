package org.example.Ind_equ.method.strings;

import org.junit.jupiter.api.Test;

class Strings_methodTest {

    @Test
    void generate_resource_url_hashcode() {
    }

    @Test
    void cleanForOWLIri() {
        String test_txt = "341432    7-DEF & test/;';'[ st(rin[]g  END 你好 世？‘。\t界！";
        System.out.println(StringsMethod.cleanForOWLIri(test_txt));
    }
}