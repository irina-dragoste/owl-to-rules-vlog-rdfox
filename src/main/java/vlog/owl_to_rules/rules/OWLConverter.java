package vlog.owl_to_rules.rules;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class OWLConverter {

	public static Atom OWLConceptToAtom(OWLClassExpression classExpression, OWLIndividual individual, Map<String, String> individualIds) {
		
		Constant constant = renameIndividual(individual, individualIds);
		return OWLConceptToAtom(classExpression, constant);
	}

	public static Atom OWLConceptToAtom(OWLClassExpression classExpression, Term term) {
		return new Atom(getConceptName(classExpression), term);
	}

	public static Atom OWLObjectPropertytoAtom(OWLObjectPropertyExpression propertyExpression, OWLIndividual subject, OWLIndividual object,  Map<String, String> individualIds) {
		return OWLObjectPropertytoAtom(propertyExpression, renameIndividual(subject, individualIds), renameIndividual(object, individualIds));
	}
	
	public static Constant renameIndividual(OWLIndividual individual, Map<String, String> individualIds) {
		String origIndividual = individual.toStringID();
		individualIds.putIfAbsent(origIndividual, getIndividualId(individualIds));
		
		String renamedIndividual = individualIds.get(origIndividual);
		Constant constant = new Constant(renamedIndividual);
		return constant;
	}


	private static String getIndividualId(Map<String, String> individualIds) {
		return "I" + individualIds.keySet().size();
	}

	public static Atom OWLObjectPropertytoAtom(OWLObjectPropertyExpression propertyExpression, Term subject, Term object) {
		OWLObjectPropertyExpression simplified = propertyExpression.getSimplified();
		if (simplified.isOWLObjectProperty()) {
			return new Atom(getPropertyName(simplified), subject, object);
		} else {
			OWLObjectPropertyExpression inverseProperty = simplified.getInverseProperty().getSimplified();
			// inv(R) (s,0) => R (o,s)
			return new Atom(getPropertyName(inverseProperty), object, subject);
		}
	}
	
	public static Constant OWLIndividualToConstant(OWLIndividual individual) {
//		TODO is this ok?
		//return "<" + individual.toStringID() + ">";
		return new Constant(individual.toStringID());
	}

	private static String getConceptName(OWLClassExpression classExpression) {
		return classExpression.asOWLClass().toStringID();
	}
	
	private static String getPropertyName(OWLObjectPropertyExpression objectProperty) {
		return objectProperty.asOWLObjectProperty().toStringID();
	}


}
