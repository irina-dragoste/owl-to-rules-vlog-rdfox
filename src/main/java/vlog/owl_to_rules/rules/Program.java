package vlog.owl_to_rules.rules;

import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class Program {

	private Map<String, Set<Atom>> facts = new HashMap<>();
	private Set<Rule> rules = new HashSet<>();
	private Map<String, String> individualIds = new HashMap<>();

	public Program(OWLOntology normalizedOntology) throws UnexpectedException {
		for (OWLAxiom axiom : normalizedOntology.logicalAxioms().collect(Collectors.toList())) {
			if (!AxiomType.ABoxAxiomTypes.contains(axiom.getAxiomType())) {
				AxiomsToRules.collectRules(axiom, rules);
			} else {
				AxiomsToRules.collectFacts(axiom, facts, individualIds);
			}
		}
		AxiomsToRules.axiomatizeDifferentFrom(rules);
	}

	public Set<Rule> getRules() {
		return rules;
	}

	public Map<String, Set<Atom>> getFacts() {
		return facts;
	}

	public void setFacts(Map<String, Set<Atom>> facts) {
		this.facts = facts;
	}

	public Map<String, String> getIndividualIds() {
		return this.individualIds;
	}

}
