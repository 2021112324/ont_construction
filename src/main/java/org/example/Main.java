package org.example;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class Main {
    public static void main(String[] args) {
        // 创建一个空的RDF模型
        Model model = ModelFactory.createDefaultModel();

        // 定义OWL类的URL
        String classUrl = "http://example.org/owl#Person";
        Resource personClass = model.createResource(classUrl, OWL.Class);

        // 定义OWL数据属性的URL
        String propertyUrl = "http://example.org/owl#hasName";
        Property hasNameProperty = model.createProperty(propertyUrl);
        hasNameProperty.addProperty(RDF.type, OWL.DatatypeProperty);

        // 定义OWL个体的URL
        String individualUrl = "http://example.org/owl#Alice";
        Resource aliceIndividual = model.createResource(individualUrl, personClass);

        // 为个体设置属性值
        String nameValue = "Alice";
        aliceIndividual.addProperty(hasNameProperty, model.createLiteral(nameValue));

        // 打印模型的内容
        model.write(System.out);
    }
}