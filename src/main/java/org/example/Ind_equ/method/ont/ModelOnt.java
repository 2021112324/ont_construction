package org.example.Ind_equ.method.ont;

import org.apache.jena.rdf.model.Resource;
import org.example.ont.OntMethod;

public interface ModelOnt extends OntMethod {
    boolean init_method();

    boolean add_subclass_relation(Resource resourceA, Resource resourceB);

    boolean generate_init_ont_from_datafile(String datafile_name, int init_ont_case);

    boolean add_init_ont_case1(String filename);
}
