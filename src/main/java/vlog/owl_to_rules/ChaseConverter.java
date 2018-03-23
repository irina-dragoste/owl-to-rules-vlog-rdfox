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

public class ChaseConverter {

	// common files to RDFox and VLOG: csv ABox, rules dictionary

	// RDFox specific files: rules folder, schema folder

	// VLog specific files: conf.mem, rules, facts.nt

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		String ontoPath = args[0];
		String outputFolder = args[1];
		String dbConfLocation = args[2];
		String aboxFolder = null;

		if (args.length > 2) {
			// folder with Turtle files representing the ABox
			aboxFolder = args[3];
		}

		generateChaseFiles(ontoPath, aboxFolder, outputFolder, dbConfLocation);
	}

	public static void generateChaseFiles(String ontoPath, String aboxFolder, String outputFolder,
			String dbConfLocation) throws OWLOntologyCreationException, IOException {

		Commons.createDir(outputFolder);

		final File ontoFile = new File(ontoPath);
		final String ontoName = ontoFile.getName();
		System.out.println("- Loading ontology " + ontoName + " " + LocalDate.now() + " " + LocalTime.now());

		final OWLOntology normalizedOntology = Commons.loadOntology(ontoFile);

		final Program program = new Program(normalizedOntology);

		System.out.println("- Processing rules and facts " + LocalDate.now() + " " + LocalTime.now());
		final SourceToTargetProgram sourceToTarget = new SourceToTargetProgram(program, aboxFolder);

		// Common
		System.out.println("- writing common test files " + LocalDate.now() + " " + LocalTime.now());
		File commonOutputFolder = Commons.createDir(outputFolder + File.separator + "common");
		File dataOutputFolder = Commons.createDir(commonOutputFolder.getAbsolutePath() + File.separator + "data");
		sourceToTarget.writeFactsToCSV(dataOutputFolder.getAbsolutePath());
//		sourceToTarget.writePredicaetDictionary(commonOutputFolder.getAbsolutePath(), ontoName);
		sourceToTarget.writeIndividualsDictionary(commonOutputFolder.getAbsolutePath(), ontoName);

		// VLog
		System.out.println("- writing VLog test files  " + LocalDate.now() + " " + LocalTime.now());
		File vlogOutputFolder = Commons.createDir(outputFolder + File.separator + "vlog");
		final VlogChaseConverter converter = new VlogChaseConverter(sourceToTarget);
		converter.generateInMemDbConfigFile(ontoName, vlogOutputFolder.getAbsolutePath(), dbConfLocation);
		converter.writeRules(vlogOutputFolder.getAbsolutePath());
		// converter.writeRules(vlogOutputFolder.getAbsolutePath(), ontoName);
		// converter.writeFactsToTurtleFile(vlogOutputFolder.getAbsolutePath(),
		// ontoName);

		// chaseRDFox
		System.out.println("- writing chaseRDFox test files  " + LocalDate.now() + " " + LocalTime.now());
		File rdfoxOutputFolder = Commons.createDir(outputFolder + File.separator + "rdfox");
		File rdfoxSchemasFolder = Commons.createDir(rdfoxOutputFolder.getAbsolutePath() + File.separator + "schemas");
		File rdfoxRulesFolder = Commons.createDir(rdfoxOutputFolder.getAbsolutePath() + File.separator + "rules");

		RDFoxChaseConverter rdFoxChaseConverter = new RDFoxChaseConverter(sourceToTarget);
		// rdFoxChaseConverter.generateSchemaFiles(rdfoxSchemasFolder.getAbsolutePath(),
		// ontoName);
		rdFoxChaseConverter.generateSchemaFiles(rdfoxSchemasFolder.getAbsolutePath());
		// rdFoxChaseConverter.writeRules(rdfoxRulesFolder.getAbsolutePath(), ontoName);
		rdFoxChaseConverter.writeRules(rdfoxRulesFolder.getAbsolutePath());

		System.out.println("- Done. " + ontoName + " " + LocalDate.now() + " " + LocalTime.now());

	}

}
