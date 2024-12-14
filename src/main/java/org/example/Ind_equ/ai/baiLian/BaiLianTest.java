package org.example.Ind_equ.ai.baiLian;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.Ind_equ.ai.baiLian.impl.BaiLianMethodImpl;
import org.python.antlr.ast.Str;

public class BaiLianTest {
    public static void main(String[] args) throws NoApiKeyException, InputRequiredException {
        BaiLianMethod baiLianMethod = new BaiLianMethodImpl();
        String text = """
                
                """;
        String result = BaiLianMethodImpl.ExtractAttribute(text);
        System.out.println(result);
    }
}
