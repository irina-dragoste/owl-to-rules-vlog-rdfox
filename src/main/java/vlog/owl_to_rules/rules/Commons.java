package vlog.owl_to_rules.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Commons {

	public static final Variable VAR_X = new Variable("X");
	public static final Variable VAR_Y = new Variable("Y");
	public static final Variable VAR_Z = new Variable("Z");

	public static OWLOntology loadOntology(File inputOntologyPath) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration()
				.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		OWLOntologyDocumentSource documentSource = new FileDocumentSource(inputOntologyPath);
		return manager.loadOntologyFromOntologyDocument(documentSource, config);
	}

	public static void appendRulesToVLogFile(Collection<Rule> rules, File file) throws IOException {
		for (Rule rule : rules) {
			appendRuleToLVogFile(rule, file);
		}
	}

	public static void appendRuleToLVogFile(Rule rule, File file) throws IOException {
		writeToFile(file, rule.toVLogFormat(), true);
	}

	public static void appendRulesToRDFoxExtensionFile(Collection<Rule> rules, File file) throws IOException {
		for (Rule rule : rules) {
			appendRuleToRDFOxExtensionFile(rule, file);
		}
	}

	public static void appendRuleToRDFOxExtensionFile(Rule rule, File file) throws IOException {
		writeToFile(file, rule.toRDFOxExtensionFormat(), true);
	}

	public static void appendFactsToFile(Collection<Atom> facts, File file) throws IOException {
		for (Atom fact : facts) {
			appendFactToFile(fact, file);
		}
	}

	public static void appendFactToFile(Atom fact, File file) throws IOException {
		writeToFile(file, fact.toTurtleAtom() + "\n", true);
	}

	public static void initializeFile(File file) throws IOException {
		writeToFile(file, "", false);
	}

	public static void writeToFile(File file, String content, boolean append) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, append))) {
			bw.write(content);
		}
	}

	public static File createDir(final String dirPath) {
		final File file = new File(dirPath);
		// File file = new File("C:\\Directory1");
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Created directory " + dirPath);
			} else {
				throw new RuntimeException("Error creating directory "+ dirPath);
			}
		}
		return file;
	}

}
