package org.example.person.method;

import org.apache.jena.rdf.model.Resource;
import org.example.ont.OntMethod;

import java.util.List;

public interface Person_ont extends OntMethod {
    @Override
    boolean read_ont(String ont_filename, String base_url);

    @Override
    boolean add_ont(String ont_filename, String base_url);

    @Override
    void write_ont(String ont_filename);

    public List<String> write_person_ont_string(String filename);

    public void write_stmt_string( Resource object, List<String> strings);

    public void read_person_ont_string_map(String filename);


}
