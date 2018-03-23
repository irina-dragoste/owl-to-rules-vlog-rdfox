package vlog.owl_to_rules;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import vlog.owl_to_rules.rules.Atom;
import vlog.owl_to_rules.rules.Commons;
import vlog.owl_to_rules.rules.SourceToTargetProgram;

public class RDFoxChaseConverter {

	private SourceToTargetProgram sourceToTargetProgram;

	public RDFoxChaseConverter(SourceToTargetProgram sourceToTargetProgram) {
		super();
		this.sourceToTargetProgram = sourceToTargetProgram;
	};

	public void writeRules(final String dependenciesFolder, final String ontoName) throws IOException {
		// .st-tgds.txt
		// .t-tgds.txt
		final File sourceToTargetTGDsFile = new File(dependenciesFolder + File.separator + ontoName + ".st-tgds.txt");
		final File targetToTargetTGDsFile = new File(dependenciesFolder + File.separator + ontoName + ".t-tgds.txt");
		Commons.initializeFile(sourceToTargetTGDsFile);
		Commons.initializeFile(targetToTargetTGDsFile);
		Commons.appendRulesToRDFoxExtensionFile(sourceToTargetProgram.getSourceToTargetTGDs(), sourceToTargetTGDsFile);
		Commons.appendRulesToRDFoxExtensionFile(sourceToTargetProgram.getTargetToTargetTGDs(), targetToTargetTGDsFile);
	}
	
	public void writeRules(final String dependenciesFolder) throws IOException {
		// .st-tgds.txt
		// .t-tgds.txt
		final File sourceToTargetTGDsFile = new File(dependenciesFolder + File.separator  + "st-tgds.txt");
		final File targetToTargetTGDsFile = new File(dependenciesFolder + File.separator  + "t-tgds.txt");
		Commons.initializeFile(sourceToTargetTGDsFile);
		Commons.initializeFile(targetToTargetTGDsFile);
		Commons.appendRulesToRDFoxExtensionFile(sourceToTargetProgram.getSourceToTargetTGDs(), sourceToTargetTGDsFile);
		Commons.appendRulesToRDFoxExtensionFile(sourceToTargetProgram.getTargetToTargetTGDs(), targetToTargetTGDsFile);
	}


	public void generateSchemaFiles(final String schemaFolder, final String ontoName) throws IOException {
		final Map<String, Integer> origPredicateArrities = sourceToTargetProgram.getPredicateArities();

		final File sourceSchemaFile = new File(schemaFolder + File.separator + ontoName + ".s-schema.txt");
		final File targetSchemaFile = new File(schemaFolder + File.separator + ontoName + ".t-schema.txt");
		Commons.initializeFile(sourceSchemaFile);
		Commons.initializeFile(targetSchemaFile);

		for (Entry<String, Integer> entry : origPredicateArrities.entrySet()) {
			final String origPredName = entry.getKey();
			final Integer arity = entry.getValue();

			final String sourcePredId = sourceToTargetProgram.getRuleSourcePredId(origPredName);
			final String targetPredId = sourceToTargetProgram.getRuleTargetPredId(origPredName);

			Commons.writeToFile(sourceSchemaFile, Atom.generatePredicateSchema(sourcePredId, arity), true);
			Commons.writeToFile(targetSchemaFile, Atom.generatePredicateSchema(targetPredId, arity), true);
		}

	}
	
	public void generateSchemaFiles(final String schemaFolder) throws IOException {
		final Map<String, Integer> origPredicateArrities = sourceToTargetProgram.getPredicateArities();

		final File sourceSchemaFile = new File(schemaFolder + File.separator +"s-schema.txt");
		final File targetSchemaFile = new File(schemaFolder + File.separator + "t-schema.txt");
		Commons.initializeFile(sourceSchemaFile);
		Commons.initializeFile(targetSchemaFile);

		for (Entry<String, Integer> entry : origPredicateArrities.entrySet()) {
			final String origPredName = entry.getKey();
			final Integer arity = entry.getValue();

			final String sourcePredId = sourceToTargetProgram.getRuleSourcePredId(origPredName);
			final String targetPredId = sourceToTargetProgram.getRuleTargetPredId(origPredName);

			Commons.writeToFile(sourceSchemaFile, Atom.generatePredicateSchema(sourcePredId, arity), true);
			Commons.writeToFile(targetSchemaFile, Atom.generatePredicateSchema(targetPredId, arity), true);
		}

	}

}
