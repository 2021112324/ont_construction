package org.example;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        //创建model
        Model model = ModelFactory.createDefaultModel();
        InputStream in = RDFDataMgr.open("src/main/java/org/example/Ind_equ/data/owl/模型2.0版本.owl");
        model.read(in, "");

        Path file_path = Paths.get("src/main/java/org/example/Ind_equ/data/rdf/元模型Test.jsonld");

        // 检查并创建目录
        Path directory = file_path.getParent();
        if (directory != null && !Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        model.write(Files.newOutputStream(file_path), "JSON-LD");

    }
}