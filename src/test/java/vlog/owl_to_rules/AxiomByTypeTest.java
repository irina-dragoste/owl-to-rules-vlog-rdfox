package vlog.owl_to_rules;

import java.io.File;
import java.rmi.UnexpectedException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import junit.framework.TestCase;
import vlog.owl_to_rules.rules.Commons;
import vlog.owl_to_rules.rules.Program;

public class AxiomByTypeTest extends TestCase {

	public void testLUBM() throws UnexpectedException, OWLOntologyCreationException {
		String location = "src//test//data//normalizedTBoxes";
		makeProgram(location + File.separator + "LUBM.owl");
	}

	public void testReactome() throws UnexpectedException, OWLOntologyCreationException {
		String location = "src//test//data//normalizedTBoxes";
		makeProgram(location + File.separator + "Reactome.owl");
	}

	public void testUniprot() throws UnexpectedException, OWLOntologyCreationException {
		String location = "src//test//data//normalizedTBoxes";
		makeProgram(location + File.separator + "Uniprot.owl");
	}

	public void testUOBM() throws UnexpectedException, OWLOntologyCreationException {
		String location = "src//test//data//normalizedTBoxes";
		makeProgram(location + File.separator + "UOBM.owl");
	}

	private void makeProgram(String ontoPath) throws OWLOntologyCreationException, UnexpectedException {
		final File ontoFile = new File(ontoPath);
		final String ontoName = ontoFile.getName();
		System.out.println("- Loading ontology " + ontoName + " " + LocalDate.now() + " " + LocalTime.now());

		final OWLOntology normalizedOntology = Commons.loadOntology(ontoFile);

		final Program program = new Program(normalizedOntology);
	}
	
	
//	- Loading ontology Uniprot.owl 2018-02-03 16:31:08.178
//	MIN CARDINALITY: 2in axiom superclass: ObjectMinCardinality(2 <http://purl.uniprot.org/core/database> owl:Thing)
//	MIN CARDINALITY: 2in axiom superclass: ObjectMinCardinality(2 <http://purl.uniprot.org/core/method> owl:Thing)
//		
//	- Loading ontology Reactome.owl 2018-02-03 16:31:09.377
//	MIN CARDINALITY: 2in axiom superclass: ObjectMinCardinality(2 <http://www.biopax.org/release/biopax-level3.owl#participant> owl:Thing)
//	
//	- Loading ontology UOBM.owl 2018-02-03 16:31:09.508
//	MIN CARDINALITY: 3in axiom superclass: ObjectMinCardinality(3 <http://semantics.crl.ibm.com/univ-bench-dl.owl#like> owl:Thing)
//	
//		- Loading ontology LUBM.owl 2018-02-03 16:31:09.575
	
//	ObjectMinCardinality(2 <http://purl.uniprot.org/core/database> owl:Thing)
//	ObjectMinCardinality(2 <http://purl.uniprot.org/core/method> owl:Thing)
//	ObjectMinCardinality(2 <http://www.biopax.org/release/biopax-level3.owl#participant> owl:Thing)		
//	ObjectMinCardinality(3 <http://semantics.crl.ibm.com/univ-bench-dl.owl#like> owl:Thing)		
		
}