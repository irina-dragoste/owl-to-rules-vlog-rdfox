package vlog.owl_to_rules.rules;

import static vlog.owl_to_rules.rules.Commons.VAR_X;
import static vlog.owl_to_rules.rules.Commons.VAR_Y;
import static vlog.owl_to_rules.rules.OWLConverter.OWLConceptToAtom;
import static vlog.owl_to_rules.rules.OWLConverter.OWLObjectPropertytoAtom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.InternalizedEntities;

public class AxiomsToRules {

	public static void axiomatizeDifferentFrom(final Set<Rule> rules) {
		// different_from (x,x) -> nothing(x)
		rules.add(new Rule(OWLConceptToAtom(InternalizedEntities.OWL_NOTHING, VAR_X),
				new Atom(OWLRDFVocabulary.OWL_DIFFERENT_FROM.toString(), VAR_X, VAR_X)));
		// different_from (x,y) -> different_from (y,x)
		rules.add(new Rule(new Atom(OWLRDFVocabulary.OWL_DIFFERENT_FROM.toString(), VAR_Y, VAR_X),
				new Atom(OWLRDFVocabulary.OWL_DIFFERENT_FROM.toString(), VAR_X, VAR_Y)));
	}

	public static void collectRules(final OWLAxiom axiom, final Set<Rule> rules) {
		switch (axiom.getAxiomType().getName()) {
		case "SubObjectPropertyOf": // [R sqs S] => R(x,y) -> S(x,y)
			subPropertyOf(axiom, rules);
			break;
		case "SubPropertyChainOf": // [R0 o ... o Rn-1 sqs R] => R0(X0,X1), R1(X1,X2),...,Rn-1(Xn-1,Xn) -> R(X1,Xn)
			subPropertyChainOf(axiom, rules);
			break;
		case "FunctionalObjectProperty": // [T sqs <=1 R.T] => EGD, ignore this axiom
			// EGD, must ignore this axiom
			break;
		case "DisjointObjectProperties":// [ (R0 and R1) sqs R_bottom ; (R1 and R2) sqs R_bottom ; (R0 and R2) sqs
										// R_bottom ]
										// => [ R0(x,y),R1(x,y) -> nothing(x) ; R1(x,y),R2(x,y) -> nothing(x) ;
										// R0(x,y),R2(x,y) -> nothing(x)]
			OWLDisjointObjectPropertiesAxiom disjointProperty = (OWLDisjointObjectPropertiesAxiom) axiom;
			Collection<OWLDisjointObjectPropertiesAxiom> asPairwiseAxioms = disjointProperty.asPairwiseAxioms();
			for (OWLDisjointObjectPropertiesAxiom disjointRolePairAxiom : asPairwiseAxioms) {
				disjointObjectPropertiesPair(disjointRolePairAxiom, rules);
			}
			break;
		case "AsymmetricObjectProperty": // (R and R-) derives inconsistency
			asymmetricObjectProperty(axiom, rules);
			break;

		case "Rule": // SWRLRule
			throw new RuntimeException("Do not support SWRL RULE: " + axiom);

		case "SubClassOf":
			
			OWLClassExpression subClass = ((OWLSubClassOfAxiom) axiom).getSubClass();
			
			
			OWLClassExpression superClass = ((OWLSubClassOfAxiom) axiom).getSuperClass();
			if (subClass.isOWLClass() && subClass.isOWLThing() && !superClass.getClassExpressionType().equals(ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
				System.out.println("OWL:TOP in subclass!! "+axiom.getAxiomType()+ " "+axiom); //it is ok if it is OBJECT_ALL_VALUES_FROM
			}
			switch (superClass.getClassExpressionType()) {
			case OBJECT_ONE_OF: // Nominal: [ A sqs {a,b,c} ] => [ A(x) -> x=a OR x=b OR x=c ]
			case OBJECT_MAX_CARDINALITY: // #atMostAxioms
				// EGD, must ignore this axiom
				break;

			case OBJECT_ALL_VALUES_FROM: // [A sqs forall R.B] => [A(x), R(x,y) -> B(y)]
				allValuesFrom(rules, subClass, superClass);
				break;

			case OBJECT_HAS_SELF:// [ A sqs exists R.Self ] => [ A(x) -> R(x,x) ]
				OWLObjectHasSelf hasSelfSuperClass = (OWLObjectHasSelf) superClass;
				rules.add(new Rule(OWLObjectPropertytoAtom(hasSelfSuperClass.getProperty(), VAR_X, VAR_X),
						OWLConceptToAtom(subClass, VAR_X)));
				break;

			case OBJECT_MIN_CARDINALITY:
				// [ A sqs >= n R.B ] => [ A(X) -> E Y0, ..., Yn-1 R(X,Y0),...,R(X,Yn-1),
				// B(Y0),..,B(Yn-1), Neq(Y0,Y1),Neq(Y0,Y2),...,Neq(Yn-2,Yn-1),
				// T(Y0),...,T(Yn)
				minCardinality(rules, subClass, superClass);
				break;

			case OWL_CLASS:
			case OBJECT_UNION_OF: // [ (A1 cap ... cap An) sqs (B1 cup ... cup Bm) ]
				List<OWLClassExpression> asDisjunctSetSuperclasses = new ArrayList<>(superClass.asDisjunctSet());
				if (asDisjunctSetSuperclasses.size() == 1) {
					OWLClassExpression superClassConcept = asDisjunctSetSuperclasses.get(0);
					switch (subClass.getClassExpressionType()) {
					case OBJECT_INTERSECTION_OF:
					case OWL_CLASS: // A1 cap ... cap An sqs B
						List<OWLClassExpression> subClassConjuncts = new ArrayList<>(subClass.asConjunctSet());
						Atom[] intersectionAxiomBody = new Atom[subClassConjuncts.size()];
						for (int i = 0; i < subClassConjuncts.size(); i++) {
							intersectionAxiomBody[i] = OWLConceptToAtom(subClassConjuncts.get(i), VAR_X);
						}
						rules.add(new Rule(OWLConceptToAtom(superClassConcept, VAR_X), intersectionAxiomBody));
						break;
					case OBJECT_HAS_SELF: // [exists R.Self sqs A] => R(x,x) -> A(x)
						rules.add(new Rule(OWLConceptToAtom(superClassConcept, VAR_X),
								OWLObjectPropertytoAtom(((OWLObjectHasSelf) subClass).getProperty(), VAR_X, VAR_X)));
						break;
					default:
						break;
					}
				}
				// else non-Horn
				break;

			default:
				throw new RuntimeException("Unexpected axiom from normalized ontology: " + axiom);
			}
			break;
		default:
			throw new RuntimeException("Unexpected axiom from normalized ontology: " + axiom);

		}
	}

	// (R and R-) derives inconsistency
	private static void asymmetricObjectProperty(final OWLAxiom axiom, final Set<Rule> rules) {
		OWLObjectPropertyExpression asymmetricProperty = ((OWLAsymmetricObjectPropertyAxiom) axiom).getProperty()
				.getSimplified();
		rules.add(new Rule(OWLConceptToAtom(InternalizedEntities.OWL_NOTHING, VAR_X),
				new Atom[] { OWLObjectPropertytoAtom(asymmetricProperty, VAR_X, VAR_Y),
						OWLObjectPropertytoAtom(asymmetricProperty.getInverseProperty(), VAR_X, VAR_Y) }));
	}

	// [ (R0 and R1) sqs R_bottom ; (R1 and R2) sqs R_bottom ; (R0 and R2) sqs
	// R_bottom ]
	// => [ R0(x,y),R1(x,y) -> nothing(x) ; R1(x,y),R2(x,y) -> nothing(x) ;
	// R0(x,y),R2(x,y) -> nothing(x)]
	private static void disjointObjectPropertiesPair(OWLDisjointObjectPropertiesAxiom disjointRolePairAxiom,
			final Set<Rule> rules) {
		List<OWLObjectPropertyExpression> disjointProperties = disjointRolePairAxiom.properties()
				.collect(Collectors.toList());
		if (disjointProperties.size() != 2) {
			throw new RuntimeException("Expected a pair of disjoint properties: " + disjointRolePairAxiom);
		}
		OWLObjectPropertyExpression firstProperty = disjointProperties.get(0);
		OWLObjectPropertyExpression secondProperty = disjointProperties.get(1);

		Rule disjointPropertiesPairRule = new Rule(OWLConceptToAtom(InternalizedEntities.OWL_NOTHING, VAR_X),
				new Atom[] { OWLObjectPropertytoAtom(firstProperty, VAR_X, VAR_Y),
						OWLObjectPropertytoAtom(secondProperty, VAR_X, VAR_Y) });
		rules.add(disjointPropertiesPairRule);
	}

	// [ A sqs >= n R.B ] => [ A(X) -> E Y0, ..., Yn-1 R(X,Y0),...,R(X,Yn-1),
	// B(Y0),..,B(Yn-1), T(Y0),...,T(Yn), Neq(Y0,Y1),Neq(Y0,Y2),...,Neq(Yn-2,Yn-1)
	private static void minCardinality(final Set<Rule> rules, final OWLClassExpression subClass,
			final OWLClassExpression superClass) {
		OWLObjectMinCardinality minCard = (OWLObjectMinCardinality) superClass;
		int minCardinality = minCard.getCardinality();
		if (minCardinality >= 1) {
			if (minCardinality > 1) {
				System.out.println("MIN CARDINALITY: "+ minCardinality + "in axiom superclass: "+superClass);
			}
			ArrayList<Variable> existentialVars = new ArrayList<>(minCardinality);
			for (int i = 0; i < minCardinality; i++) {
				existentialVars.add(new Variable("X" + i));
			}

			ArrayList<Atom> head = new ArrayList<>();
			for (int i = 0; i < minCardinality; i++) {
				head.add(OWLObjectPropertytoAtom(minCard.getProperty(), VAR_X, existentialVars.get(i)));
				head.add(OWLConceptToAtom(minCard.getFiller(), existentialVars.get(i)));
				// head.add(OWLConceptToAtom(InternalizedEntities.OWL_THING,
				// existentialVars.get(i)));
				for (int j = i + 1; j < minCardinality; j++) {
					head.add(new Atom(OWLRDFVocabulary.OWL_DIFFERENT_FROM.toString(), existentialVars.get(i),
							existentialVars.get(j)));
				}
			}

			Atom[] body = new Atom[] { OWLConceptToAtom(subClass, VAR_X) };
			Rule existentialRule = new Rule(head.toArray(new Atom[head.size()]), body);
			rules.add(existentialRule);
		}
	}

	// [A sqs forall R.B] => [A(x), R(x,y) -> B(y)]
	private static void allValuesFrom(final Set<Rule> rules, final OWLClassExpression subClass,
			final OWLClassExpression superClass) {
		OWLObjectAllValuesFrom allValuesSuperClass = (OWLObjectAllValuesFrom) superClass;
		vlog.owl_to_rules.rules.Atom[] body = new Atom[] { OWLConceptToAtom(subClass, VAR_X),
				OWLObjectPropertytoAtom(allValuesSuperClass.getProperty(), VAR_X, VAR_Y) };
		Rule allValuesFromRule = new Rule(OWLConceptToAtom(allValuesSuperClass.getFiller(), VAR_Y), body);
		rules.add(allValuesFromRule);
	}

	// [R0 o ... o Rn-1 sqs R] => R0(X0,X1), R1(X1,X2),...,Rn-1(Xn-1,Xn) -> R(X1,Xn)
	private static void subPropertyChainOf(final OWLAxiom axiom, final Set<Rule> rules) {
		OWLSubPropertyChainOfAxiom subPropertyChainOfAxiom = (OWLSubPropertyChainOfAxiom) axiom;
		OWLObjectPropertyExpression superProperty = subPropertyChainOfAxiom.getSuperProperty();
		List<OWLObjectPropertyExpression> subPropertyChain = subPropertyChainOfAxiom.getPropertyChain();
		Atom[] chainOfAxiomBody = new Atom[subPropertyChain.size()];
		for (int i = 0; i < chainOfAxiomBody.length; i++) {
			chainOfAxiomBody[i] = OWLObjectPropertytoAtom(subPropertyChain.get(i), new Variable("X" + i),
					new Variable("X" + (i + 1)));
		}
		// R0(X0,X1), R1(X1,X2),...,Rn-1(Xn-1,Xn) -> R(X1,Xn)
		Rule propertyChainRule = new Rule(OWLObjectPropertytoAtom(superProperty, new Variable("X" + 0),
				new Variable("X" + subPropertyChain.size())), chainOfAxiomBody);
		rules.add(propertyChainRule);
	}

	// R sqs S => R(x,y) -> S(x,y)
	private static void subPropertyOf(final OWLAxiom axiom, final Set<Rule> rules) {
		OWLSubObjectPropertyOfAxiom subPropertyOfAxiom = (OWLSubObjectPropertyOfAxiom) axiom;
		OWLObjectPropertyExpression superProperty = subPropertyOfAxiom.getSuperProperty().getSimplified();
		OWLObjectPropertyExpression subProperty = subPropertyOfAxiom.getSubProperty().getSimplified();

		// R(x,y) -> S(x,y)
		Rule subRoleOf = new Rule(OWLObjectPropertytoAtom(superProperty, VAR_X, VAR_Y),
				OWLObjectPropertytoAtom(subProperty, VAR_X, VAR_Y));
		rules.add(subRoleOf);
	}

	public static void collectFacts(final OWLAxiom axiom, final Map<String, Set<Atom>> facts,
			Map<String, String> individualIds) {

		AxiomType<?> axiomType = axiom.getAxiomType();
		if (AxiomType.ABoxAxiomTypes.contains(axiomType)) {
			if (axiomType.equals(AxiomType.CLASS_ASSERTION)) {
				OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;

				Atom atom = OWLConceptToAtom(classAssertionAxiom.getClassExpression(),
						classAssertionAxiom.getIndividual(), individualIds);
				addFactAtomToFacts(facts, atom);
				// facts.add(assertIndividualInTop(classAssertionAxiom.getIndividual()));

			} else if (axiomType.equals(AxiomType.DIFFERENT_INDIVIDUALS)) {
				((OWLDifferentIndividualsAxiom) axiom).asPairwiseAxioms().forEach(diffIndividualsAxiom -> {
					OWLIndividual firstIndividual = diffIndividualsAxiom.getIndividualsAsList().get(0);
					OWLIndividual secondIndividual = diffIndividualsAxiom.getIndividualsAsList().get(1);

					Atom atom = new Atom(OWLRDFVocabulary.OWL_DIFFERENT_FROM.toString(), OWLConverter.renameIndividual(firstIndividual, individualIds),
							OWLConverter.renameIndividual(secondIndividual, individualIds));

					addFactAtomToFacts(facts, atom);

					// facts.add(assertIndividualInTop(firstIndividual));
					// facts.add(assertIndividualInTop(secondIndividual));
				});
			} else if (axiomType.equals(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
				OWLObjectPropertyAssertionAxiom propertyAssertionAxiom = (OWLObjectPropertyAssertionAxiom) axiom;
				OWLObjectPropertyExpression property = propertyAssertionAxiom.getProperty().getSimplified();

				Atom atom = OWLObjectPropertytoAtom(property, propertyAssertionAxiom.getSubject(),
						propertyAssertionAxiom.getObject(), individualIds);

				addFactAtomToFacts(facts, atom);

				// facts.add(assertIndividualInTop(propertyAssertionAxiom.getSubject()));
				// facts.add(assertIndividualInTop(propertyAssertionAxiom.getObject()));
			} else if (axiomType.equals(AxiomType.SAME_INDIVIDUAL)) {
				System.out.println("Ignoring same individual assertion. We only suppoprt HORN: " + axiomType);
				// EGD, do nothing
			} else {
				throw new RuntimeException("Untreated A-box axiom of type:" + axiomType + " :" + axiom);
			}
		}
	}

	private static void addFactAtomToFacts(final Map<String, Set<Atom>> facts, Atom atom) {
		String predicate = atom.getPredicate();
		facts.putIfAbsent(predicate, new HashSet<>());
		facts.get(predicate).add(atom);
	}

	// private static Atom assertIndividualInTop(final OWLIndividual owlIndividual)
	// {
	// return OWLConceptToAtom(InternalizedEntities.OWL_THING, owlIndividual);
	// }

}
