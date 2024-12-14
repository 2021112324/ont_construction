package org.example.Ind_equ.ai.baiLian.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.Ind_equ.ai.baiLian.BaiLianMethod;
import org.python.antlr.ast.Str;

import java.util.Arrays;

public class BaiLianMethodImpl implements BaiLianMethod {
    public static String ExtractAttribute(String text) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("""
                 {"instruction": "您是命名实体关系识别方面的专家。
                 input内容是某一工业设备实体的产品描述，请从input中抽取出符合schema定义的关系三元组，不存在的关系三元组不输出，同个关系可能存在多个不同客体的三元组。
                 请按照JSON字符串的格式回答。",
                 "schema": [{"搭载": "该关系描述了该工业设备A实体上面"搭载"/"板载"了另一个工业设备B，那么A的关系"搭载"的客体为"B""}，
                            {"配备": "该关系描述了该工业设备A实体上面"配备"了另一个工业设备B，那么A的关系"配备"的客体为"B"，注意："配备"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}，
                            {"采用": "该关系描述了该工业设备A实体上面"采用"/"使用"了另一个工业设备B，那么A的关系"采用"的客体为"B"，注意："采用"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}
                            {"应用领域": "该关系描述了该工业设备A能够使用的场景Z或工业设备B，那么A的关系"应用领域"的客体为"Z"或"B""}], "input": "%s"}
                        """)
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("""
                        """)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        String result = String.valueOf(gen.call(param));
        return result;
    }
}
