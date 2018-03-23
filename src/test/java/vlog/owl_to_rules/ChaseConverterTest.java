package vlog.owl_to_rules;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import junit.framework.TestCase;

public class ChaseConverterTest extends TestCase {
	
	public void testChaseConverterNonBlocked() throws OWLOntologyCreationException, IOException{
		ChaseConverter.generateChaseFiles("src/test/data/non-blocked/non-blocked.owl", null, "src/test/data/non-blocked", "/data/db1");
	}

	
	public void testChaseConverterBlocked() throws OWLOntologyCreationException, IOException{
		ChaseConverter.generateChaseFiles("src/test/data/blocked/blocked.owl", null, "src/test/data/blocked", "/data/db1");
	}
	
}
