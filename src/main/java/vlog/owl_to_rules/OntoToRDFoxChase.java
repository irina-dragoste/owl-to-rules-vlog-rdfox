package vlog.owl_to_rules;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import vlog.owl_to_rules.rules.Commons;
import vlog.owl_to_rules.rules.Program;
import vlog.owl_to_rules.rules.SourceToTargetProgram;

public class OntoToRDFoxChase {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		final String ontoPath = args[1];
		final String ontoRulesFolder = args[2];
		final String ontoFactsCSVFolder = args[3];
		final String ontoSchemaFolder = args[4];
		
		final File ontoFile = new File(ontoPath);
		String ontoName = ontoFile.getName();
		System.out.println("Loading ontology " + ontoName + " ." + LocalDate.now() + " " + LocalTime.now());
		final OWLOntology normalizedOntology = Commons.loadOntology(ontoFile);
		System.out.println("- Transforming to program ontology . " + LocalDate.now() + " " + LocalTime.now());
		final Program program = new Program(normalizedOntology);
		System.out.println("- Generating source and target rules . " + LocalDate.now() + " " + LocalTime.now());
		final SourceToTargetProgram sourceToTarget = new SourceToTargetProgram(program, null);

		transformOntoToRDFoxChaseExtension(sourceToTarget, ontoName, ontoRulesFolder, ontoFactsCSVFolder, ontoSchemaFolder);
	}

	public static void transformOntoToRDFoxChaseExtension(final SourceToTargetProgram sourceToTarget, final String ontoName, final String ontoRulesFolderPath, final String ontoFactsCSVFolderPath,
			final String schemaFolder) throws OWLOntologyCreationException, IOException {

//		File ontoRulesFolder = new File(ontoRulesFolderPath);
//		if (!ontoRulesFolder.exists()) {
//			ontoRulesFolder.mkdir();
//		}
//		
//		File ontoFactsFolder = new File(ontoFactsCSVFolderPath);
//		if (!ontoFactsFolder.exists()) {
//			ontoFactsFolder.mkdir();
//		}


		RDFoxChaseConverter rdFoxChaseConverter = new RDFoxChaseConverter(sourceToTarget);
		rdFoxChaseConverter.generateSchemaFiles(schemaFolder, ontoName);
		rdFoxChaseConverter.writeRules(ontoRulesFolderPath, ontoName);
//		sourceToTarget.writeFactsToCSV(ontoFactsCSVFolder);
//		sourceToTarget.writePredicatDictionary(schemaFolder, ontoName);
		System.out.println("- Done. " + LocalDate.now() + " " + LocalTime.now());
	}

}
