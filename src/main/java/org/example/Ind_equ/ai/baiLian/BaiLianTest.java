package org.example.Ind_equ.ai.baiLian;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.Ind_equ.ai.baiLian.impl.BaiLianMethodImpl;
import org.python.antlr.ast.Str;

import static org.example.Ind_equ.ai.baiLian.BaiLianMethod.extract_property;

public class BaiLianTest {
    public static void main(String[] args) throws NoApiKeyException, InputRequiredException {
        String text = """
                EDH2200系列高压变频器是一款新型高效节能型变频器，采用最新一代IGBT功率器件和大规模集成电路芯片全数字控制，多电平串联倍压的技术方案，先进的最优控制算法，实现了优质的可变频变压（VVVF）的正弦电压和正弦电流的完美输出。
                """;
        String result = extract_property(text);
        System.out.println(result);
    }
}
