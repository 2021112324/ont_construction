package org.example.Ind_equ.ai.baiLian;

import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
public interface BaiLianMethod {

    public static String extract_property(String input) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("""
                        您是命名实体属性识别方面的专家。
                        我现在的问题是：
                        input内容是某一工业设备实体的产品描述，请从input中抽取出符合该工业产品属性的关键词，该产品描述中可能包含多种属性，请全部输出,未提及的属性请不要输出。
                        请注意！！！！！！！未明确提及的属性请不要输出！！！
                        请注意！！！！！！！未提及的属性请不要输出！！！
                        请注意！！！！！！！请您只列出提及的属性！！！
                        请按照JSON字符串的格式回答
                               """)
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(input)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationResult result = gen.call(param);
        return String.valueOf(result);
    }
    public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
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
        return gen.call(param);
    }
    public static void main(String[] args) {
        String text = "一、产品简介\n" +
                "EDH2200系列高压变频器是一款新型高效节能型变频器，采用最新一代IGBT功率器件和大规模集成电路芯片全数字控制，多电平串联倍压的技术方案，先进的最优控制算法，实现了优质的可变频变压（VVVF）的正弦电压和正弦电流的完美输出。\n" +
                "EDH2200系列高压变频器主要有三大部分组成，干式移相变压器柜、功率柜、控制柜。\n" +
                "二 、产品特点\n" +
                "1）数字控制系统\n" +
                "全数字化高性能控制器，由以一块CPU板、一块数据采集卡、若干块基于FPGA的光纤子板构成，缩小了控制器的体积，提高了系统的反应速度、可靠性，也为系统维护带来了便捷。\n" +
                "2）层叠母线技术\n" +
                "在功率单元设计时我公司采用特有的层叠母线技术，如图所示，有效的抑制了由器件开通关断时产生的尖峰电压，延长了功率器件的使用寿命，提高了系统的可靠性。\n" +
                "层叠母线效果图\n" +
                "3）最优控制策略采用先进的优化控制算法，参数可以任意设置，调节方便，可以满足各种特性的负载要求，使其启动电流小，启动平滑，不会出现低频振荡现象。4）完善的保护功能变压器、功率单元具有齐全的保护功能，结合人机接口界面实现了故障诊断与故障实时记录。三 、产品应用领域功率单元串联式多电平高压变频调速技术是在高压变频领域具有绝对优势的一项技术，可广泛应用于电力、钢铁、采矿、市政供水、石油、化工、水泥、有色、造纸等行业。冶金：除尘风机、送水泵、主排风机、退火炉风机、鼓风机、除磷泵等。火力发电：引风机、送风机、给水泵、循环水泵、凝结水泵、灰浆泵、磨煤机等。\n" +
                "石油石化：注水泵、输油泵、排风机、管道泵、压缩机等。\",https://www.gongkong.com/product/201405/69910.html\n" +
                "明阳龙源 3MW海上型风机主控及逆变装置,中高压变频器 高压变频器配件 功率模块,明阳龙源,广东明阳龙源电力电子有限公司,为用户带来显著成果的领先之作！,http://nfs.gongkong.com/Upload/product/201609/20160922135947834_w.jpg,\"高度集成：集风机主控，风电变流及变桨控制于一休的主控及逆变装置\n" +
                "快速预装：厂内统一安装调试，现场整体呆装\n" +
                "高可靠性：满足高寒/高湿/高热/海洋等气候环境的运行要求，外壳采用特殊加强设计的集装箱，能抵御70M/S超强台风，通过低电压穿越试验\n" +
                "强适应性：外壳防护等级IP65，防腐等级C5-M，适用于极端环境条件\n" +
                "运输便捷：外壳采用集装箱形式，同时满足陆运及海运要求，可三层叠加运输，节省运输空间\n" +
                "系统参数\n" +
                "平面布置图\n" +
                "系统图\n" +
                "室外安装平台";
        try {
            String result = extract_property(text);
            System.out.println(result);
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("错误信息："+e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
        System.exit(0);
    }
}
/** 下列列表是总结的可能出现的属性，您可以参考该列表总结工业设备可能出现的属性，然后回答我的问题：[
 型号：设备的具体型号，用于区分不同版本或配置。
 品牌：设备制造商的品牌名称。
 生产日期：设备的制造日期。
 序列号：唯一标识每台设备的编号。
 功率：设备运行时消耗的电能或其他能源的量。
 电压：设备正常工作所需的电压范围。
 电流：设备运行时的电流值。
 频率：设备运行时的电源频率。
 重量：设备的总重量。
 尺寸：设备的长、宽、高尺寸。
 材料：设备的主要制造材料。
 工作温度范围：设备能够正常工作的温度范围。
 防护等级：设备的防尘、防水等级，通常用IP等级表示。
 负载能力：设备能够承受的最大负载。
 精度：设备操作或测量的精确度。
 速度：设备运行的速度。
 效率：设备的能量转换或工作完成的效率。
 控制方式：手动、自动或半自动控制。
 接口类型：设备与其他系统或设备连接的接口类型，如USB、以太网等。
 通信协议：设备支持的通信协议，如Modbus、Profibus等。
 安全特性：设备的安全保护措施，如过载保护、短路保护等。
 维护周期：建议的维护和保养周期。
 使用寿命：设备预期的使用年限。
 认证标准：设备符合的国际或国家标准，如CE、UL等。
 环境适应性：设备对特殊环境条件的适应能力，如高温、低温、潮湿等。
 噪音水平：设备运行时产生的噪音水平。
 耗材：设备运行过程中需要定期更换的耗材，如滤芯、润滑油等。
 软件版本：设备内置软件或固件的版本号。
 数据存储：设备的数据存储容量和类型。
 扩展性：设备是否支持扩展模块或功能。
 兼容性：设备与其他设备或系统的兼容性。
 额定功率：设备在正常工作状态下的功率。
 最大功率：设备能够承受的最大功率。
 最小功率：设备能够稳定工作的最小功率。
 能效等级：设备的能效等级，如一级、二级等。
 运行模式：设备支持的运行模式，如连续运行、间歇运行等。
 启动时间：设备从启动到进入正常工作状态所需的时间。
 停机时间：设备从正常工作状态到完全停止所需的时间。
 冷却方式：设备的冷却方式，如风冷、水冷等。
 润滑方式：设备的润滑方式，如油润滑、脂润滑等。
 安装方式：设备的安装方式，如壁挂式、立式、卧式等。
 操作界面：设备的操作界面类型，如触摸屏、按钮等。
 显示方式：设备的显示方式，如LED、LCD等。
 数据传输方式：设备的数据传输方式，如有线、无线等。
 数据接口：设备支持的数据接口类型，如RS232、RS485、USB等。
 网络协议：设备支持的网络协议，如TCP/IP、Modbus TCP等。
 远程监控：设备是否支持远程监控功能。
 故障报警：设备的故障报警机制和方式。
 维护记录：设备的维护记录和历史维护情况。
 保修期限：设备的保修期限。
 认证证书：设备获得的相关认证证书，如CE、FCC等。
 包装方式：设备的包装方式，如木箱、纸箱等。
 运输方式：设备的运输方式，如陆运、海运、空运等。
 安装要求：设备的安装要求，如地面平整度、通风条件等。
 使用环境：设备适用的使用环境，如室内、室外、高温、低温等。
 操作人员培训：设备操作人员的培训要求。
 售后服务：设备的售后服务内容和方式。
 配件清单：设备的标准配件清单。
 可选配件：设备的可选配件清单。
 备件供应：设备备件的供应情况和渠道。
 技术支持：设备的技术支持方式和联系方式。
 用户手册：设备的用户手册和操作指南。
 安全说明：设备的安全使用说明和注意事项。
 环保性能：设备的环保性能指标，如低噪音、低排放等。
 能耗指标：设备的能耗指标，如每小时耗电量等。
 可靠性指标：设备的可靠性指标，如平均无故障时间（MTBF）等。
 设计寿命：设备的设计使用寿命。
 回收处理：设备的回收处理方式和要求。
 ]
 请注意！！！！！！！该列表仅供您参考，可能有其他该列表中不存在的属性，也可能该列表的某些属性在input中未提及，请您只考虑涉及到的属性！！！**/