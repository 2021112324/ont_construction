package org.example.Ind_equ.ai.MacBERT.impl;

import org.example.Ind_equ.ai.MacBERT.MacBERTMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacBERTMethodImpl implements MacBERTMethod {
    @Override
    public List<String> runMacBERT(String equipment, String classificationString) {
        List<String> result = new ArrayList<>();
        try {
            // 构建命令行参数
            String pythonScriptPath = "pythonCode/python/indEqu/AI/MacBERT/macbert_silmilarity.py";
            String[] command = {"venv/Scripts/python.exe", pythonScriptPath, equipment, classificationString};

            // 创建进程
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            // 读取标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("'\\s*(.*?)\\s*'\\s*相似度:\\s*\\s*(\\d+\\.\\d+)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()){
//                    System.out.println(matcher.group(1) + matcher.group(2));
                    //TODO: 处理结果
                    if (matcher.group(2).matches("-?\\d+(\\.\\d+)?")) {
                        double number = Double.parseDouble(matcher.group(2));
                        if (number >= 0.9){
                            result.add(matcher.group(1));
                        }
                        // 继续处理
                    }

                }
//                output.append(line).append("\n");
            }

            // 读取错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // 等待进程结束
            int exitCode = process.waitFor();
            if (exitCode == 0) {
//                return output.toString().trim();
                return result;
            } else {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode + "\nError: " + errorOutput.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
//            return "Error: " + e.getMessage();
            return null;
        }
    }

    public static void main(String[] args) {
        MacBERTMethodImpl macBERTMethodImpl = new MacBERTMethodImpl();
        String equipment = "J30J航空插头";
        String classificationString = "机箱机柜, 控制柜, 仪表盘-箱, 配电箱-柜, 网络机柜, 配电组件, 机柜配件, 加热设备," +
                " 工业电源, 开关电源, 直流变换电源, 低压变频器, 通用型变频器, 行业专用变频器, 起重机行业专用变频器, 低压变频器配件, " +
                "整流模块, 离散传感器, 测量光幕, 光电开关, 接近开关, 超声波传感器, 行程开关, 光纤传感器, 色标传感器, 微动开关, " +
                "位移传感器, 工业以太网, 物联网, 应用层, 人机接口HMI-数据采集与监视控制系统SCADA软件, 分布式控制系统DCS, 工程师站," +
                " I-O单元, I-O控制站, 远程I-O站, 操作员站, 显示器, 中央处理器CPU, 软件, 工控软件, 伺服系统, 伺服电机, 伺服驱动器," +
                " 测量测试软件, 电气设计软件, 制造执行系统MES, 实时-历史数据库, 用于过程控制的对象连接和控制OPC软件, 感知层, 现场仪表," +
                " 安全栅, 流量仪表, 流量计, 流量开关, 流量传感器, 质量流量计, 压力变送器, 温湿度传感器, 压力传感器, 温度变送器, 编程软件," +
                " 人机接口HMI-数据采集与监视控制系统SCADA, 软件平台, 人机接口HMI软件, 编码器, 服务器, 中高压变频器, 高压变频器配件," +
                " 功率模块, 交-交变频器, 电流源型, 电压源型, 执行机构, 阀门定位器, 调节阀, 以太网控制自动化技术EtherCAT, 电动执行器," +
                " 电磁阀, 气动产品, 仪器仪表, 分析测试仪表, 温度仪表, 电量仪表, 压力仪表, 压力开关, 巡检-调节仪表, 显示控制仪表, 记录仪," +
                " 温控器, 温控系统, 冷却系统, 数显仪表, 运动控制, 步进系统, 步进控制器, 步进电机, 运动机械部件, 电机, 减速电机, 直线电机," +
                " 软启动器, 变频电机, 交流电机, 同步电机, 运动控制器, 功能安全, 设备安全, 安全锁, 安全控制产品, 安全模块, 安全检测与防护," +
                " 隔离器, 安全开关, 安全继电器, 防爆产品, 安全光栅, 安全光幕, 可编程逻辑控制器PLC, 人机界面, 平板电脑, 触摸屏, " +
                "图控一体化产品, 中型可编程逻辑控制器PLC, 其它, 制动电阻, 电抗器, 小型可编程逻辑控制器PLC, 通讯模块, 中央处理器CPU模块, " +
                "嵌入式系统, 嵌入式控制器, 图像处理单元, 工控机, 专用机, 机器视觉, 光源, 显示单元, 指示灯, 低压电器, 控制电器, 主令电器, " +
                "按钮开关, 轻触开关, 启动器, 继电器, 电流继电器, 热继电器, 固态继电器, 交流接触器, 控制元件, 智能低压电器, 配电电器, " +
                "断路器, 保护电器, 熔断器, 条码识别, 视觉系统, 工业镜头, 视觉传感器, 电荷耦合器件CCD-互补金属氧化物半导体CMOS照相机," +
                " 图像采集卡, 称紧凑型外围组件互连扩展CompactPCI-面向仪器系统的外围组件互连扩展PXI, 专用板卡, 数据采集卡, 外围产品, " +
                "分布式I-O系统, 工控机整机, 上架式工控机, , 嵌入式软件, 嵌入式操作系统, 嵌入式工控机, 工控机配件, 存储产品, 工业母板," +
                " 嵌入式主板, 数据采集与监视控制系统SCADA, 无线通信, 采集仪表, 远程终端单元RTU, 网络层, 无线通信产品, 通讯产品, " +
                "机械传动, 减速机, 连轴器, 丝杠, 轴承, 工业交换机, 电气连接, 工业连接器";
        macBERTMethodImpl.runMacBERT(equipment, classificationString);
//        System.out.println(result);
    }
}
