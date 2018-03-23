package vlog.owl_to_rules.rules;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class SourceToTargetProgram {

	public static final String CSV_COMMA_DELIMITER = ",";
	public static final String CSV_NEW_LINE_SEPARATOR = "\n";

	private static final String SRC_PRED_PREFIX = "SRC_";

	final Map<String, Set<Atom>> facts;
	private final Set<Rule> sourceToTargetTGDs = new HashSet<>();
	private final Set<Rule> targetToTargetTGDs = new HashSet<>();
	//
	private Map<String, Integer> rulePredicateArities = new HashMap<>();
	private Map<String, String> rulePredicateIds = new HashMap<>();
	private Map<String, String> individualIds;

	public SourceToTargetProgram(final Program program, String aboxFolder) throws OWLOntologyCreationException {
		// TODO read from ABox
		Set<Rule> rules = program.getRules();
		collectRulePredicates(rules);

		facts = program.getFacts();
		System.out.println("# Fact predicates: " + facts.size());
		// if (aboxFolder!=null) {
		// loadFactsFormABoxFolder(new File(aboxFolder));
		// }
		

		individualIds = program.getIndividualIds();

		// add s->t rules that rename all rule predicates src_pred_id to (target)
		// pred_id
		createSourceToTargetTGDs();
		// translate orig rules using target predicates ids

		createTargetToTargetTGDs(rules);

	}

//	private void loadFactsFormABoxFolder(final File aboxFolder) throws OWLOntologyCreationException {
//		for (File ttlFile : aboxFolder.listFiles()) {
//			OWLOntology aboxOntology = Commons.loadOntology(ttlFile);
//			//check the types of axioms, see if they are property assertions or not
//			aboxOntology.axioms().forEachOrdered(axiom->System.out.println(axiom));
//			
//		}
//	}

	/**
	 * collect predicates and their arrities from rules
	 *
	 * @param program
	 */

	private void collectRulePredicates(Set<Rule> rules) {
		for (Rule rule : rules) {
			Atom[] body = rule.getBody();
			for (Atom atom : body) {
				rulePredicateIds.putIfAbsent(atom.getPredicate(), "P" + rulePredicateIds.keySet().size());
				loadArrity(atom);
			}
			Atom[] head = rule.getHead();
			for (Atom atom : head) {
				rulePredicateIds.putIfAbsent(atom.getPredicate(), "P" + rulePredicateIds.keySet().size());
				loadArrity(atom);
			}
		}

		System.out.println("# Rule predicates: " + rulePredicateIds.size());
	}

	public void writeFactsToCSV(final String ontoFactsCSVFolder) throws IOException {
		Set<String> factOrigPredicates = facts.keySet();
		for (String origFactPredicate : factOrigPredicates) {
			if (isRulePred(origFactPredicate)) {
				// rename it
				final File factsFile = new File(
						ontoFactsCSVFolder + File.separator + getRuleSourcePredId(origFactPredicate) + ".csv");
				Commons.initializeFile(factsFile);

				final Set<Atom> factsPerPred = facts.get(origFactPredicate);
				for (Atom atom : factsPerPred) {
					if (rulePredicateArities.get(atom.getPredicate()) != atom.getArgs().length) {
						throw new RuntimeException("Variable arity for predicate: " + atom.getPredicate());
					}
					// TODO maybe check same arity
					Commons.writeToFile(factsFile, atom.toCSVFormat(), true);
				}

			} else {
				// ignore it, t will not be influenced by thge chase
			}
		}
	}

	public void writePredicaetDictionary(final String dictFolder, final String ontoName) throws IOException {
		final File dict = new File(dictFolder + File.separator + ontoName + "pred.dict.csv");
		Commons.initializeFile(dict);
		rulePredicateIds.forEach((origPred, predId) -> {
			String line = origPred + CSV_COMMA_DELIMITER + predId + CSV_NEW_LINE_SEPARATOR;
			try {
				Commons.writeToFile(dict, line, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void writePredicaetDictionary(final String dictFolder) throws IOException {
		final File dict = new File(dictFolder + File.separator + "pred.dict.csv");
		Commons.initializeFile(dict);
		rulePredicateIds.forEach((origPred, predId) -> {
			String line = origPred + CSV_COMMA_DELIMITER + predId + CSV_NEW_LINE_SEPARATOR;
			try {
				Commons.writeToFile(dict, line, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void writeIndividualsDictionary(final String dictFolder, final String ontoName) throws IOException {
		final File dict = new File(dictFolder + File.separator + ontoName + "individual.dict.csv");
		Commons.initializeFile(dict);
		individualIds.forEach((origIndiv, indivPred) -> {
			String line = "<" + origIndiv + ">" + CSV_COMMA_DELIMITER + indivPred + CSV_NEW_LINE_SEPARATOR;
			try {
				Commons.writeToFile(dict, line, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void writeIndividualsDictionary(final String dictFolder) throws IOException {
		final File dict = new File(dictFolder + File.separator +  "individual.dict.csv");
		Commons.initializeFile(dict);
		individualIds.forEach((origIndiv, indivPred) -> {
			String line = "<" + origIndiv + ">" + CSV_COMMA_DELIMITER + indivPred + CSV_NEW_LINE_SEPARATOR;
			try {
				Commons.writeToFile(dict, line, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private boolean isRulePred(String origFactPredicate) {
		return rulePredicateIds.containsKey(origFactPredicate);
	}

	private void loadArrity(Atom atom) {
		int arity = atom.getArgs().length;
		String predicate = atom.getPredicate();
		if (rulePredicateArities.containsKey(predicate)) {
			if (arity != rulePredicateArities.get(predicate)) {
				throw new RuntimeException(" Different arities for the same predicate name !" + predicate);
			}
		}

		rulePredicateArities.putIfAbsent(predicate, arity);
	}

	private void createSourceToTargetTGDs() {
		rulePredicateArities.forEach((origPred, arity) -> {
			final String targetPredId = getRuleTargetPredId(origPred);
			final String sourcePredId = getRuleSourcePredId(origPred);
			final Rule sourceToTargetRule;
			if (arity == 1) {
				sourceToTargetRule = new Rule(new Atom(targetPredId, Commons.VAR_X),
						new Atom(sourcePredId, Commons.VAR_X));
			} else if (arity == 2) {
				sourceToTargetRule = new Rule(new Atom(targetPredId, Commons.VAR_X, Commons.VAR_Y),
						new Atom(sourcePredId, Commons.VAR_X, Commons.VAR_Y));
			} else {
				throw new RuntimeException("unexpected case");
			}
			sourceToTargetTGDs.add(sourceToTargetRule);
		});

		System.out.println("#source to target TGDs: " + sourceToTargetTGDs.size());
	}

	private void createTargetToTargetTGDs(final Set<Rule> origRules) {
		origRules.forEach(origRule -> {

			final Atom[] targetBody = renamePredicateToTargetId(origRule.getBody());
			final Atom[] targetHead = renamePredicateToTargetId(origRule.getHead());

			Rule translatedRule = new Rule(targetHead, targetBody);
			targetToTargetTGDs.add(translatedRule);

		});

		System.out.println("#target to target TGDs: " + targetToTargetTGDs.size());
	}

	private Atom[] renamePredicateToTargetId(final Atom[] origArray) {
		Atom[] targetArray = new Atom[origArray.length];
		for (int i = 0; i < origArray.length; i++) {
			final Atom sourceAtom = origArray[i];
			final String origPredicate = sourceAtom.getPredicate();

			if (sourceAtom.getArgs().length != rulePredicateArities.get(origPredicate)) {
				throw new RuntimeException("Different arities for the same predicate name!" + origPredicate);
			}

			final Atom targetAtom = new Atom(getRuleTargetPredId(origPredicate), sourceAtom.getArgs());
			targetArray[i] = targetAtom;
		}
		return targetArray;
	}

	public String getRuleSourcePredId(final String origPred) {
		return SRC_PRED_PREFIX + getRuleTargetPredId(origPred);
	}

	public String getRuleTargetPredId(final String origPred) {
		return rulePredicateIds.get(origPred);
	}

	public Collection<Rule> getSourceToTargetTGDs() {
		return sourceToTargetTGDs;
	}

	public Collection<Rule> getTargetToTargetTGDs() {
		return targetToTargetTGDs;
	}

	public Map<String, Integer> getPredicateArities() {
		return this.rulePredicateArities;
	}

	public Map<String, String> getPredicateIds() {
		return this.rulePredicateIds;
	}

	public Map<String, Set<Atom>> getFacts() {
		return this.facts;
	}

}
