package org.example.Ind_equ.ai.baiLian;

import java.util.*;
import java.lang.System;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.python.antlr.ast.Str;

import static org.example.Ind_equ.ai.baiLian.prompt.*;

/**
 *
 *                         您是命名实体属性识别方面的专家。我现在的问题是：
 *                         input内容是某一工业设备实体的产品描述，请从input中抽取出符合该工业产品属性的关键词，该产品描述中可能包含多种属性，请全部输出,未提及的属性请不要输出。
 *                         请一步步进行推理并得出结论。
 *                         然后，再从结论里抽出属性标签。
 *                         请按照JSON字符串的格式回答。
 *
 * **/
public interface BaiLianMethod {
    public static String get_extract_property(String input) throws NoApiKeyException, InputRequiredException {
//        String tempOutput = testText;
        String tempOutput = extract_property(input);
        // 提取JSON部分
        String jsonPart = extractJson(tempOutput);
        System.out.println("JSON Part: " + jsonPart);
        if (jsonPart != null) {
            try {
                // 将提取的字符串转换为JSONObject
                Set<String> keys = getKeysFromJsonString(jsonPart);
                // 将keys转成json格式
                JSONArray jsonArray = new JSONArray(keys);
//                System.out.println("Extracted Keys: " + keys);
//                System.out.println("JSON Array: " + jsonArray);
                return jsonArray.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No JSON found in the input.");
            return null;
        }
        return null;
    }


    public static Set<String> getKeysFromJsonString(String jsonString) {
        Set<String> keysSet = new HashSet<>();

        // 定义正则表达式模式，匹配形如 "AA": "BBBBB" 的键
//        Pattern pattern = Pattern.compile("\"([^\"]+)\":\\s*\"");
        Pattern pattern = Pattern.compile("\"([^\"]+)\":");
        Matcher matcher = pattern.matcher(jsonString);

        while (matcher.find()) {
            // 将匹配到的键添加到 Set 集合中
            keysSet.add(matcher.group(1));
        }

        return keysSet;
    }



    public static String extract_property(String input) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(prompt_for_property)
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
//        System.out.println(result);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    private static String extractJson(String input) {
        int start = input.indexOf("```json");
        int end = input.lastIndexOf("```");

        if (start != -1 && end != -1) {
            // 提取JSON部分并去除引号
            return input.substring(start + 8, end).trim();
        }

        return null;
    }

    public static String get_extract_relation(String input) throws NoApiKeyException, InputRequiredException {
        String tempOutput = extract_relation(input);
        // 提取JSON部分
        String jsonPart = extractJson(tempOutput);
        System.out.println("JSON Part: " + jsonPart);
        return jsonPart;
    }

    public static String extract_relation(String input) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(prompt_for_relation)
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
//        System.out.println(result);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    public static Set<String> getValuesFromJsonString(String jsonString) {
        Set<String> valuesList = new HashSet<>();
        JSONObject jsonObject = new JSONObject(jsonString);

        for (String key : jsonObject.keySet()) {
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            for (int i = 0; i < jsonArray.length(); i++) {
                valuesList.add(jsonArray.getString(i));
            }
        }

        return valuesList;
    }

    public static String get_generate_relation(String input) throws NoApiKeyException, InputRequiredException {
        String tempOutput = generate_relation(input);
        // 提取JSON部分
        String jsonPart = extractJson(tempOutput);
//        System.out.println("JSON Part: " + jsonPart);
        if (jsonPart != null) {
            try {
                // 将提取的字符串转换为JSONObject
                Set<String> keys = getValuesFromJsonString(jsonPart);
                // 将keys转成json格式
                JSONArray jsonArray = new JSONArray(keys);
//                System.out.println("Extracted Keys: " + keys);
//                System.out.println("JSON Array: " + jsonArray);
                return jsonArray.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No JSON found in the input.");
            return null;
        }
        return null;
    }

    public static String generate_relation(String input) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(prompt_for_create_relation)
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
//        System.out.println(result);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    public static Map<String,Map<String,List<String>>> get_generate_property_and_relation(String input) throws NoApiKeyException, InputRequiredException {
        String tempOutput = generate_property_and_relation(input);
        // 提取JSON部分
        String jsonPart = extractJson(tempOutput);
//        System.out.println("JSON Part: " + jsonPart);
        JSONObject jsonObject = null;
        if (jsonPart != null) {
            jsonObject = new JSONObject(jsonPart);
        }
//        System.out.println("JSON Part: " + jsonObject);
        Map<String,Map<String,List<String>>> resultMap = new HashMap<>();
        if (jsonObject != null){
            JSONObject properties = jsonObject.getJSONObject("属性");
            if (properties != null){
                Map<String,List<String>> propertiesMap = new HashMap<>();
                for (String key : properties.keySet()) {
                    JSONArray jsonArray = properties.getJSONArray(key);
                    // 将JSONArray转换为List
                    List<String> list = new ArrayList<>();
                    if (jsonArray != null){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            list.add(jsonArray.getString(i));
                        }
                    }
                    propertiesMap.put(key,list);
                }
                resultMap.put("属性",propertiesMap);
            }
            JSONObject relations = jsonObject.getJSONObject("关系");
            if (relations != null){
                Map<String,List<String>> relationsMap = new HashMap<>();
                for (String key : relations.keySet()) {
                    JSONArray jsonArray = relations.getJSONArray(key);
                    // 将JSONArray转换为List
                    List<String> list = new ArrayList<>();
                    if (jsonArray != null){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            list.add(jsonArray.getString(i));
                        }
                    }
                    relationsMap.put(key,list);
                }
                resultMap.put("关系",relationsMap);
            }
        }
        return resultMap;
    }

    public static String generate_property_and_relation(String input) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(prompt_for_property_and_relation)
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
//        System.out.println(result);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
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
                "石油石化：注水泵、输油泵、排风机、管道泵、压缩机等。";
//        String text = testText2;
        try {
//            String result = get_extract_property(text);
//            String result = get_extract_relation(text);
//            String result = get_generate_relation(text);
            String result = String.valueOf(get_generate_property_and_relation(text));
            System.out.println(result);
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("错误信息："+e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
        System.exit(0);
    }

    static String testText2 = "{\"分类标签列表\":[\"\",\"数据采集与监视控制系统SCADA\",\"其它\",\"远程终端单元RTU\",\"人机接口HMI/数据采集与监视控制系统SCADA软件\",\"组态软件\",\"通讯产品\",\"MODEM\",\"分布式I/O\",\"模拟量输入\",\"数字量输出\",\"数字量输入\",\"模拟量输出\",\"采集仪表\",\"数据采集与监视控制系统SCADA主站系统\",\"网关\",\"可编程逻辑控制器PLC\",\"小型可编程逻辑控制器PLC\",\"通讯模块\",\"执行机构\",\"调节阀\",\"阀门定位器\",\"液压产品\",\"电动执行器\",\"电磁阀\",\"水泵\",\"气动产品\",\"运动控制\",\"运动控制器\",\"可编程逻辑控制器PLC运动控制模块\",\"PC运动控制卡\",\"一体化驱动器\",\"其它\",\"运动机械部件\",\"数控系统\",\"特种加工数控系统\",\"整机\",\"通用数控系统\",\"运动控制总线\",\"步进系统\",\"步进一体机\",\"步进电机\",\"步进控制器\",\"伺服系统\",\"伺服驱动器\",\"电机驱动一体机\",\"伺服电机\",\"永磁交流伺服电动机\",\"交流伺服电动机\",\"直流伺服电机\",\"仪器仪表\",\"分析测试仪表\",\"红外热像仪\",\"故障诊断\",\"网络分析测试\",\"过程校验仪表\",\"气体检测/分析\",\"电工仪表\",\"水质分析\",\"显示控制仪表\",\"数显仪表\",\"其它\",\"巡检/调节仪表\",\"温控器\",\"记录仪\",\"转速表\",\"电量仪表\",\"现场仪表\",\"物位/液位仪表\",\"液位控制器\",\"液位变送器\",\"液位计\",\"液位开关\",\"浮球液位计\",\"音叉\",\"磁浮子液位计\",\"雷达\",\"超声波\",\"电容式\",\"物位计\",\"重锤式\",\"音叉\",\"超声波\",\"雷达\",\"电容式\",\"物位开关\",\"射频导纳\",\"水位计\",\"流量仪表\",\"流量传感器\",\"计量仪\",\"流量计\",\"气体流量计\",\"超声波流量计\",\"孔板（差压式）流量计\",\"涡街流量计\",\"电磁流量计\",\"质量流量计\",\"流量开关\",\"水表\",\"流量变送器\",\"温度仪表\",\"热电偶\",\"非接触式温度计\",\"双金属温度计\",\"温度传感器\",\"温度开关\",\"温度变送器\",\"热电阻\",\"压力仪表\",\"压力变送器\",\"压力传感器\",\"压力表\",\"差压变送器\",\"压力计\",\"减压器\",\"压力开关\",\"称重/配料仪表\",\"安全栅\",\"仪表结构装置\",\"智能物流设备\",\"智能分拣机\",\"穿梭式货架\",\"轻型堆垛机\",\"托盘输送机\",\"超高超重型堆垛机\",\"自动导引车\",\"智能多层穿梭车\",\"高端数控机床\",\"车铣磨复合加工机床\",\"其它\",\"数控加工中心\",\"螺旋内齿圈拉床\",\"蒙皮镜像铣数控装备\",\"数控蜗杆砂轮磨齿机\",\"五轴加工中心\",\"离散传感器\",\"位移传感器\",\"速度传感器\",\"行程开关\",\"接近开关\",\"光电开关\",\"RFID\",\"编码器\",\"无线通信\",\"无线数传电台/无线数据终端\",\"WirelessHART\",\"无线网关/网卡\",\"RFID系统\",\"无线路由器/中继器\",\"GSM/CDMA/4G\",\"ZigBee\",\"物联网\",\"应用层\",\"软件平台\",\"其它\",\"嵌入式应用\",\"云服务\",\"感知层\",\"振动传感器\",\"RFID\",\"速度传感器\",\"压力传感器\",\"电量传感器\",\"视觉传感器\",\"位置检测\",\"其它\",\"网络层\",\"接入传输交换产品\",\"无线通信产品\",\"其它\",\"机箱机柜\",\"网络机柜\",\"仪表盘/箱\",\"配电箱/柜\",\"高低压开关柜\",\"开关柜\",\"控制柜\",\"配电组件\",\"变频柜\",\"机械传动\",\"无级变速机\",\"连轴器\",\"制动器\",\"滑轨\",\"减速机\",\"丝杠\",\"离合器\",\"工业大数据\",\"商业智能\",\"其它\",\"数据集成\",\"云数据服务\",\"大数据分析\",\"大数据服务器/大数据机\",\"工业电源\",\"UPS\",\"直流变换电源\",\"线性直流电源\",\"逆变电源\",\"开关电源\",\"交流稳压电源\",\"USPS\",\"低压变频器\",\"行业专用变频器\",\"卷绕拉丝行业专用变频器\",\"电梯专用变频器\",\"机床专用变频器\",\"纺织行业专用变频器\",\"风机/水泵专用变频器\",\"空压机专用变频器\",\"起重机行业专用变频器\",\"工程型变频器\",\"通用型变频器\",\"电气连接\",\"接线端子头\",\"PCB连接器\",\"接线端子\",\"重载连接器\",\"接线盒\",\"连接系统\",\"工业连接器\",\"互联网安全\",\"安全管理型交换设备\",\"网闸\",\"杀毒软件\",\"局域网防火墙\",\"安全审计\",\"网络安全整机\",\"控制层专用防火墙\",\"工业云计算\",\"IaaS（基础设施级服务）\",\"SaaS（软件级服务）\",\"其它\",\"PaaS（平台级服务）\",\"人机界面\",\"平板电脑\",\"其它\",\"编程/通讯电缆\",\"其它\",\"文本终端\",\"触摸屏\",\"人机接口HMI软件\",\"图控一体化产品\",\"可编程逻辑控制器PLC\",\"嵌入式系统\",\"嵌入式控制器\",\"嵌入式主板\",\"PC/104\",\"3.5寸\",\"COM-Express\",\"Slot-pc\",\"pos/aimb\",\"EBX/5.25寸\",\"SOM\",\"嵌入式软件\",\"嵌入式组态软件\",\"嵌入式操作系统\",\"嵌入式处理器\",\"嵌入式微控制器(MCU)\",\"嵌入式片上系统(SOC)\",\"其它\",\"存储产品\",\"嵌入式机箱\",\"其他\",\"单板电脑\",\"移动数据终端\",\"PLM\",\"CAD/CAE\",\"电机\",\"交流电机\",\"同步电机\",\"高压电机\",\"异步电机\",\"低压电机\",\"高压电机\",\"变频电机\",\"步进电机\",\"直流电机\",\"软启动器\",\"低压软启动器\",\"高压软启动器\",\"力矩电机\",\"伺服电机\",\"中高压变频器\",\"单元串联多电平\",\"电压源型\",\"交-交变频器\",\"高压变频器配件\",\"其它\",\"工控软件\",\"批量管理\",\"趋势、分析和报表\",\"工程设计软件\",\"实时/历史数据库\",\"人机接口HMI/数据采集与监视控制系统SCADA\",\"用于过程控制的对象连接和控制OPC软件\",\"编程软件\",\"IEC61131标准编程软件\",\"分布式控制系统DCS\",\"操作员站\",\"键盘\",\"显示器\",\"计算机\",\"工程师站\",\"计算机\",\"显示器\",\"总线转换器\",\"键盘\",\"I/O单元\",\"I/O卡件槽\",\"I/O卡\",\"开关量输出卡\",\"温度输入卡\",\"模拟量输出卡\",\"开关量输入卡\",\"脉冲量卡\",\"多点混合输入/输出卡\",\"模拟量输入卡\",\"I/O单元电源卡\",\"I/O控制站\",\"远程I/O站\",\"现场I/O站\",\"中央处理器CPU\",\"服务器\",\"用于过程控制的对象连接和控制OPC服务器\",\"软件\",\"增材制造设备\",\"其它\",\"金属增材制造装备\",\"生物及医疗个性化增材制造装备\",\"非金属增材制造装备\",\"3D打印软件\",\"3D打印机附件\",\"3D打印材料\",\"功能安全\",\"人员安全\",\"网络安全\",\"设备安全\",\"本安防爆\",\"安全地毯\",\"安全网络\",\"安全检测与防护\",\"安全光束\",\"安全开关\",\"安全继电器\",\"安全传感器\",\"拉绳开关\",\"安全光栅\",\"安全光幕\",\"安全控制产品\",\"安全可编程逻辑控制器PLC\",\"安全模块\",\"安全锁\",\"防爆电脑\",\"其它\",\"过程安全\",\"其它\",\"ESD/SIS(紧急停车/安全仪表系统)\",\"远程终端单元RTU\",\"工业机器人\",\"精密及重载装配机器人\",\"关节型喷涂机器人\",\"平面关节（SCARA）型机器人\",\"六轴关节型机器人\",\"机加工机器人\",\"Delta机器人\",\"点焊机器人\",\"工业以太网\",\"CC-Link-IE\",\"Modbus-TCP\",\"网关\",\"Ethernet-I/O\",\"EtherNet/IP\",\"网关\",\"中继器\",\"以太网控制自动化技术EtherCAT\",\"光纤转换器\",\"以太网交换机\",\"PC网卡\",\"集线器\",\"网关\",\"无线以太网设备\",\"可编程逻辑控制器PLC网络模块\",\"ProfiNet\",\"网关\",\"以太网交换机\",\"其它\",\"可编程逻辑控制器PLC网络模块\",\"以太网交换机\",\"整机\",\"附件\",\"低压电器\",\"控制电器\",\"主令电器\",\"凸轮开关\",\"脚踏开关\",\"转换开关\",\"按钮开关\",\"选择开关\",\"轻触开关\",\"按钮开关\",\"主令控制器\",\"继电器\",\"温度继电器\",\"时间继电器\",\"速度继电器\",\"固态继电器\",\"电流继电器\",\"液位继电器\",\"中间继电器\",\"直流接触器\",\"其它\",\"驱动电器\",\"电磁阀\",\"启动器\",\"手动启动器\",\"交流接触器\",\"附件\",\"空气式接触器\",\"其它\",\"智能电动机控制器/装置\",\"配电电器\",\"负荷开关\",\"组合开关\",\"断路器\",\"万能式（框架式）断路器\",\"微型断路器\",\"真空断路器\",\"装置式（塑壳）断路器\",\"普通装置式塑壳断路器\",\"其它附件\",\"电抗器\",\"变压器\",\"其它\",\"控制元件\",\"计数器\",\"电位器\",\"按钮盒\",\"可控硅\",\"信号转换\",\"电流、电压传感器\",\"模拟信号转换器\",\"智能低压电器\",\"显示单元\",\"指示灯\",\"信号灯\",\"报警装置\",\"蜂鸣器\",\"声光报警器\",\"数码显示单元\",\"保护电器\",\"电涌保护器\",\"其它\",\"电机保护器\",\"避雷器\",\"热继电器\",\"熔断器\",\"制造执行系统MES\",\"专用制造执行系统MES\",\"其它\",\"集成制造执行系统MES\",\"现场总线\",\"ControlNet\",\"中继器\",\"Interbus\",\"接口模块\",\"CC-link\",\"中继器\",\"分布式I/O\",\"总线电缆\",\"Profibus\",\"网关\",\"中继器\",\"PC接口卡\",\"接口模块\",\"功能站\",\"Modbus\",\"分布式I/O\",\"网关\",\"中继器\",\"DeviceNet\",\"网关\",\"总线电缆\",\"中继器\",\"FF\",\"接口模块\",\"可编程逻辑控制器PLC\",\"可编程逻辑控制器PLC\",\"小型可编程逻辑控制器PLC\",\"小型可编程逻辑控制器PLC\",\"功能模块\",\"称重模块\",\"脉冲输出/定位模块\",\"I/O扩展单元\",\"数字(离散)量输入模块\",\"模拟量输入模块\",\"模拟量输出模块\",\"模拟量输入/输出模块\",\"数字(离散)量输入/输出模块\",\"数字(离散)量输出模块\",\"附件\",\"电池\",\"编程电缆\",\"端子连接块\",\"端子排\",\"编程软件\",\"存储器卡\",\"其它\",\"通讯模块\",\"电源模块\",\"中央处理器CPU模块\",\"温度测量扩展模块\",\"热电偶扩展模块\",\"热电阻扩展模块\",\"大型可编程逻辑控制器PLC\",\"功能模块\",\"运动控制模块\",\"高速计数模块\",\"通讯模块\",\"电源模块\",\"定位模块\",\"I/O模块\",\"模拟量输入输出模块\",\"数字(离散)量输入模块\",\"模拟量输出模块\",\"热电偶输入\",\"模拟量输入模块\",\"接口模块\",\"中央处理器CPU模块\",\"网关\",\"应用网关\",\"小型可编程逻辑控制器PLC\",\"其它\",\"中型可编程逻辑控制器PLC\",\"功能模块\",\"高速计数模块\",\"称重模块\",\"定位模块\",\"电源模块\",\"通讯模块\",\"运动控制模块\",\"附件\",\"连接电缆\",\"I/O模块\",\"模拟量输出模块\",\"热电偶输入\",\"模拟量输入模块\",\"数字(离散)量输出模块\",\"模拟量输入输出模块\",\"数字(离散)量输入模块\",\"数字(离散)量输入输出模块\",\"中央处理器CPU模块\",\"接口模块\",\"工业交换机\",\"机架式交换机\",\"其它\",\"网管型交换机\",\"行业专用交换机\",\"非网管型交换机\",\"IP67交换机\",\"机器视觉\",\"图像处理单元\",\"电荷耦合器件CCD/互补金属氧化物半导体CMOS照相机\",\"监视器\",\"条码识别\",\"视觉传感器\",\"图像处理软件\",\"视觉系统\",\"工控机\",\"PC-Based控制器\",\"工控机配件\",\"工业母板\",\"计算机电源\",\"LED显示器\",\"其它\",\"机箱\",\"显示产品\",\"中央处理器CPU卡\",\"存储产品\",\"专用机\",\"专用工作站\",\"其他\",\"服务器\",\"虚拟仪器\",\"外围产品\",\"其它\",\"通讯产品\",\"信号调理\",\"I/O模件\",\"分布式I/O\",\"工业I/O\",\"数据采集模块\",\"分布式I/O系统\",\"工控机整机\",\"便携式工业PC\",\"嵌入式工控机\",\"上架式工控机\",\"其它\",\"工作站\",\"壁挂式工控机\",\"平板电脑（面板式个人计算机PPC）\",\"称紧凑型外围组件互连扩展CompactPCI/面向仪器系统的外围组件互连扩展PXI\",\"中央处理器CPU模块\",\"整机\",\"电源模块\",\"便携机\",\"I/O模块\",\"运动控制卡\",\"背板\",\"专用板卡\",\"运动控制卡\",\"数据采集卡\",\"其它\"],\"关系标签组\":[[\"继电器\",\"CAN\",\"RS485\",\"RS232\",\"晶体管\"],[\"数据采集模块\",\"PLC\"],[\"光耦\",\"绝缘型DC/DC转换器\"],[\"IEC61131-3梯形图编程\",\"多种通讯接口和协议\"],[\"继电器\",\"水泵设备\",\"木工设备\",\"印刷设备\",\"石材加工设备\",\"CTP印前设备\",\"晶体管\",\"测试设备\"],[\"WiFi模组\",\"二点模拟量输出\",\"八点数字量输入\",\"八点数字量输出\",\"三点模拟量输入\"],[\"2G/3G/4G/5G/ADSL上云\",\"大屏网站\",\"PLC控制\",\"2路RS485\",\"边缘计算\",\"极简工业工业互联网平台\",\"1路10/100M以太网\",\"超级网关\",\"手机APP\"],[\"系统级、模块级、I/O级多层次冗余\",\"更大的分布式系统\",\"J30J航空插头\",\"航天定制PLC专用SOC处理器\",\"丰富的I/O模块\",\"多种通讯接口和协议\"],[\"扩展器\",\"KTY81 测温探头\",\"智能继电器\",\"3 线接近传感器 PNP\",\"NTC 1000k 测温探头\",\"显示面板\",\"Pt 500 测温探头\",\"NTC 10k 测温探头\"],[\"扩展模块\",\"数字量I/O\",\"矩形高速现场总线\",\"串口\",\"模拟量\",\"分布式安装和控制\",\"网口\",\"高速ARM处理器\"]]}";

    static String testText = "GenerationResult(requestId=b7833296-98f2-972a-894d-85454fb7396e, usage=GenerationUsage(inputTokens=1486, outputTokens=904, totalTokens=2390), output=GenerationOutput(text=null, finishReason=null, choices=[GenerationOutput.Choice(finishReason=stop, index=null, message=Message(role=assistant, content=根据提供的产品描述，我们可以逐步推理并提取出相关的属性关键词。以下是详细的推理过程：\n" +
            "\n" +
            "### 1. 型号\n" +
            "- **提到的型号**：EDH2200系列高压变频器\n" +
            "\n" +
            "### 2. 功率\n" +
            "- **提到的功率**：未明确提及具体的功率值，但提到了“功率单元”。\n" +
            "\n" +
            "### 3. 控制方式\n" +
            "- **提到的控制方式**：全数字化高性能控制器，基于CPU板、数据采集卡和FPGA光纤子板。\n" +
            "\n" +
            "### 4. 接口类型\n" +
            "- **提到的接口类型**：未明确提及具体的接口类型，但提到了“人机接口界面”。\n" +
            "\n" +
            "### 5. 通信协议\n" +
            "- **提到的通信协议**：未明确提及具体的通信协议。\n" +
            "\n" +
            "### 6. 安全特性\n" +
            "- **提到的安全特性**：变压器、功率单元具有齐全的保护功能，结合人机接口界面实现了故障诊断与故障实时记录。\n" +
            "\n" +
            "### 7. 精度\n" +
            "- **提到的精度**：未明确提及具体的精度。\n" +
            "\n" +
            "### 8. 速度\n" +
            "- **提到的速度**：未明确提及具体的运行速度。\n" +
            "\n" +
            "### 9. 效率\n" +
            "- **提到的效率**：新型高效节能型变频器，采用了先进的控制算法，实现了优质的可变频变压（VVVF）的正弦电压和正弦电流的完美输出。\n" +
            "\n" +
            "### 10. 工作温度范围\n" +
            "- **提到的工作温度范围**：未明确提及具体的工作温度范围。\n" +
            "\n" +
            "### 11. 防护等级\n" +
            "- **提到的防护等级**：未明确提及具体的防护等级。\n" +
            "\n" +
            "### 12. 负载能力\n" +
            "- **提到的负载能力**：参数可以任意设置，调节方便，可以满足各种特性的负载要求。\n" +
            "\n" +
            "### 13. 环境适应性\n" +
            "- **提到的环境适应性**：未明确提及具体的环境适应性，但提到了广泛的应用领域，包括电力、钢铁、采矿、市政供水、石油、化工、水泥、有色、造纸等行业。\n" +
            "\n" +
            "### 14. 使用环境\n" +
            "- **提到的使用环境**：未明确提及具体的使用环境，但提到了广泛的应用领域，包括电力、钢铁、采矿、市政供水、石油、化工、水泥、有色、造纸等行业。\n" +
            "\n" +
            "### 15. 其他\n" +
            "- **提到的其他属性**：\n" +
            "  - **数字控制系统**：全数字化高性能控制器，由一块CPU板、一块数据采集卡、若干块基于FPGA的光纤子板构成。\n" +
            "  - **层叠母线技术**：有效抑制了由器件开通关断时产生的尖峰电压，延长了功率器件的使用寿命，提高了系统的可靠性。\n" +
            "  - **最优控制策略**：采用先进的优化控制算法，参数可以任意设置，调节方便，可以满足各种特性的负载要求，使其启动电流小，启动平滑，不会出现低频振荡现象。\n" +
            "\n" +
            "### 最终提取的属性关键词\n" +
            "```json\n" +
            "{\n" +
            "  \"型号\": \"EDH2200系列高压变频器\",\n" +
            "  \"型号\": \"EDH2200系列高压变频器\",\n" +
            "  \"控制方式\": \"全数字化高性能控制器，基于CPU板、数据采集卡和FPGA光纤子板\",\n" +
            "  \"安全特性\": \"变压器、功率单元具有齐全的保护功能，结合人机接口界面实现了故障诊断与故障实时记录\",\n" +
            "  \"效率\": \"新型高效节能型变频器，采用了先进的控制算法，实现了优质的可变频变压（VVVF）的正弦电压和正弦电流的完美输出\",\n" +
            "  \"负载能力\": \"参数可以任意设置，调节方便，可以满足各种特性的负载要求\",\n" +
            "  \"其他\": {\n" +
            "    \"数字控制系统\": \"全数字化高性能控制器，由一块CPU板、一块数据采集卡、若干块基于FPGA的光纤子板构成\",\n" +
            "    \"层叠母线技术\": \"有效抑制了由器件开通关断时产生的尖峰电压，延长了功率器件的使用寿命，提高了系统的可靠性\",\n" +
            "    \"最优控制策略\": \"采用先进的优化控制算法，参数可以任意设置，调节方便，可以满足各种特性的负载要求，使其启动电流小，启动平滑，不会出现低频振荡现象\"\n" +
            "  }\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "以上是根据产品描述提取出的属性关键词。, toolCalls=null, toolCallId=null, name=null, contents=null))]))";
}
/**
 *
 * ,https://www.gongkong.com/product/201405/69910.html\n" +
 *                 "明阳龙源 3MW海上型风机主控及逆变装置,中高压变频器 高压变频器配件 功率模块,明阳龙源,广东明阳龙源电力电子有限公司,为用户带来显著成果的领先之作！,http://nfs.gongkong.com/Upload/product/201609/20160922135947834_w.jpg,\"高度集成：集风机主控，风电变流及变桨控制于一休的主控及逆变装置\n" +
 *                 "快速预装：厂内统一安装调试，现场整体呆装\n" +
 *                 "高可靠性：满足高寒/高湿/高热/海洋等气候环境的运行要求，外壳采用特殊加强设计的集装箱，能抵御70M/S超强台风，通过低电压穿越试验\n" +
 *                 "强适应性：外壳防护等级IP65，防腐等级C5-M，适用于极端环境条件\n" +
 *                 "运输便捷：外壳采用集装箱形式，同时满足陆运及海运要求，可三层叠加运输，节省运输空间\n" +
 *                 "系统参数\n" +
 *                 "平面布置图\n" +
 *                 "系统图\n" +
 *                 "室外安装平台"
 例如：input内容为[
     一、产品简介
     EDH2200系列高压变频器是一款新型高效节能型变频器，采用最新一代IGBT功率器件和大规模集成电路芯片全数字控制，多电平串联倍压的技术方案，先进的最优控制算法，实现了优质的可变频变压（VVVF）的正弦电压和正弦电流的完美输出。EDH2200系列高压变频器主要有三大部分组成，干式移相变压器柜、功率柜、控制柜。
      二 、产品特点
     1）数字控制系统
     全数字化高性能控制器，由以一块CPU板、一块数据采集卡、若干块基于FPGA的光纤子板构成，缩小了控制器的体积，提高了系统的反应速度、可靠性，也为系统维护带来了便捷。
     2）层叠母线技术
     在功率单元设计时我公司采用特有的层叠母线技术，如图所示，有效的抑制了由器件开通关断时产生的尖峰电压，延长了功率器件的使用寿命，提高了系统的可靠性。
     层叠母线效果图
     3）最优控制策略采用先进的优化控制算法，参数可以任意设置，调节方便，可以满足各种特性的负载要求，使其启动电流小，启动平滑，不会出现低频振荡现象。4）完善的保护功能变压器、功率单元具有齐全的保护功能，结合人机接口界面实现了故障诊断与故障实时记录。三 、产品应用领域功率单元串联式多电平高压变频调速技术是在高压变频领域具有绝对优势的一项技术，可广泛应用于电力、钢铁、采矿、市政供水、石油、化工、水泥、有色、造纸等行业。冶金：除尘风机、送水泵、主排风机、退火炉风机、鼓风机、除磷泵等。火力发电：引风机、送风机、给水泵、循环水泵、凝结水泵、灰浆泵、磨煤机等。\n" +
     石油石化：注水泵、输油泵、排风机、管道泵、压缩机等。
 ]

 * 下列列表是总结的可能出现的属性，您可以参考该列表总结工业设备可能出现的属性，然后回答我的问题：[
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
//public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
//    Generation gen = new Generation();
//    Message systemMsg = Message.builder()
//            .role(Role.SYSTEM.getValue())
//            .content("""
//                        请注意！！！！！！！未明确提及的属性请不要输出！！！
//                        请注意！！！！！！！未提及的属性请不要输出！！！
//                        请注意！！！！！！！请您只列出提及的属性！！！
//                 {"instruction": "您是命名实体关系识别方面的专家。
//                 input内容是某一工业设备实体的产品描述，请从input中抽取出符合schema定义的关系三元组，不存在的关系三元组不输出，同个关系可能存在多个不同客体的三元组。
//                 请按照JSON字符串的格式回答。",
//                 "schema": [{"搭载": "该关系描述了该工业设备A实体上面"搭载"/"板载"了另一个工业设备B，那么A的关系"搭载"的客体为"B""}，
//                            {"配备": "该关系描述了该工业设备A实体上面"配备"了另一个工业设备B，那么A的关系"配备"的客体为"B"，注意："配备"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}，
//                            {"采用": "该关系描述了该工业设备A实体上面"采用"/"使用"了另一个工业设备B，那么A的关系"采用"的客体为"B"，注意："采用"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}
//                            {"应用领域": "该关系描述了该工业设备A能够使用的场景Z或工业设备B，那么A的关系"应用领域"的客体为"Z"或"B""}], "input": "%s"}
//                        """)
//            .build();
//
//    Message userMsg = Message.builder()
//            .role(Role.USER.getValue())
//            .content("""
//                        """)
//            .build();
//    GenerationParam param = GenerationParam.builder()
//            // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
//            // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
//            .model("qwen-plus")
//            .messages(Arrays.asList(systemMsg, userMsg))
//            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
//            .build();
//    return gen.call(param);
//}