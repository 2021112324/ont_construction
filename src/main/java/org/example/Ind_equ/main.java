package org.example.Ind_equ;

import org.apache.jena.rdf.model.Resource;
import org.example.Ind_equ.method.ont.IndEquOnt;
import org.example.Ind_equ.method.ont.impl.IndEquOntImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        IndEquOnt ind_equOnt = new IndEquOntImpl();

        //测试本体构建
//        ind_equOnt.add_ont_from_data_dir("src/main/java/org/example/Ind_equ/data/csv");
//////        ind_equOnt.print_ont();
////
//        ind_equOnt.write_ont("src/main/java/org/example/Ind_equ/data/rdf/Ind_equ_ont.owl");

        //测试create_equip_relation
//        ind_equOnt.add_ont("rdf/Ind_equ_ont.owl", "");
//        Resource testResource = ind_equOnt.find_product("矩形科技 CMPAC加固大型PLC");
////        System.out.println(testResource);
//        ind_equOnt.create_equip_relation(testResource);

        //获取产品名
//        ind_equOnt.add_ont("rdf/Ind_equ_ont.owl", "");
//        List<String> product_name_list =ind_equOnt.find_all_product_name();
//        writeProductNamesToCSV(product_name_list, "src/main/java/org/example/Ind_equ/data/product_names.csv");

        //手动添加测试数据
        ind_equOnt.add_ont("rdf/Ind_equ_ont_test.owl", "");
        List<String> extractedData = extractLinesFromCSV(
                "src/main/java/org/example/Ind_equ/data/product_names.csv", 41, 60);
        for (String line : extractedData){
            System.out.println(line);
            Resource testResource = ind_equOnt.find_product(line);
            System.out.println(testResource);
            ind_equOnt.create_equip_relation(testResource);
        }
        ind_equOnt.write_ont("src/main/java/org/example/Ind_equ/data/rdf/Ind_equ_ont_test.owl");

//        ind_equOnt.add_ont_from_data_dir("src/main/java/org/example/Ind_equ/data/testcsv");
////
//        ind_equOnt.write_ont("src/main/java/org/example/Ind_equ/data/rdf/Test_ind_equ_ont.owl");
//        ind_equOnt.add_ont("rdf/Test_ind_equ_ont.owl", "");
//        ind_equOnt.print_ont();
//        ind_equOnt.create_all_equip_relation();
    }

    private static void writeProductNamesToCSV(List<String> product_name_list, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String productName : product_name_list) {
                writer.write(productName);
                writer.newLine(); // 换行
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> extractLinesFromCSV(String filePath, int startLine, int endLine) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLine = 0;
            while ((line = reader.readLine()) != null) {
                currentLine++;
                if (currentLine >= startLine && currentLine <= endLine) {
                    lines.add(line);
                }
                if (currentLine > endLine) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
