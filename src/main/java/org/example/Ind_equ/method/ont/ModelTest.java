package org.example.Ind_equ.method.ont;

import org.example.Ind_equ.method.ont.impl.ModelOntImpl;

public class ModelTest {
    public static void main(String[] args) {
        ModelOnt modelOnt = new ModelOntImpl();
        modelOnt.init_method();
        modelOnt.generate_init_ont_from_datafile("初始模型.csv",1);
        modelOnt.write_ont("初始模型v1.owl");
        modelOnt.write_ont("初始模型v1.jsonld");
    }
}
