package vlog.owl_to_rules.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.manchester.cs.owl.owlapi.InternalizedEntities;

public class Rule {

	private Atom[] body;
	private Atom[] head;

	public Rule(Atom[] head, Atom[] body) {
		this.head = eliminateTop(head);
		this.body = eliminateTop(body);
		checkBodyIsNotTop();
	}

	public Rule(Atom head, Atom[] body) {
		this.head = new Atom[] { head };
		this.body = eliminateTop(body);
		checkBodyIsNotTop();
	}

	public Rule(Atom head, Atom body) {
		this.head = new Atom[] { head };
		this.body = new Atom[] { body };
		checkBodyIsNotTop();
	}

	private void checkBodyIsNotTop() {
		if (body.length == 1 && isTop(body[0])) {
			throw new RuntimeException("Unexpected rule with only Top in the body! " + toVLogFormat());
		}
	}

	public String toRDFOxFormat() {
		StringBuilder rdfoxFormattedRule = new StringBuilder();
		for (Atom headCounjunct : head) {
			rdfoxFormattedRule.append(headCounjunct.toRDFoxFormat()).append(" :- ");
			for (Atom bodyAtom : body) {
				rdfoxFormattedRule.append(bodyAtom.toRDFoxFormat()).append(", ");
			}
			rdfoxFormattedRule = new StringBuilder(rdfoxFormattedRule.substring(0, rdfoxFormattedRule.length() - 2))
					.append(" .\n");
		}
		return rdfoxFormattedRule.toString();
	}

	public String toVLogFormat() {
		StringBuilder rdfoxFormattedRule = new StringBuilder();
		for (int i = 0; i < head.length; i++) {
			rdfoxFormattedRule.append(head[i].toVLogFormat());
			if (i < head.length - 1) {
				rdfoxFormattedRule.append(",");
			}
		}
		rdfoxFormattedRule.append(" :- ");
		for (int i = 0; i < body.length; i++) {
			rdfoxFormattedRule.append(body[i].toVLogFormat());
			if (i < body.length - 1) {
				rdfoxFormattedRule.append(",");
			}
		}
		rdfoxFormattedRule.append("\n");
		return rdfoxFormattedRule.toString();
	}

	// TODO how do you repesent constants in this format?
	/**
	 * Dean(?X) -> headOf(?X,?Y), College(?Y) . <br>
	 * Person(?X), headOf(?X,?X1), Department(?X1) -> Chair(?X) .
	 * 
	 * @return
	 */
	public String toRDFOxExtensionFormat() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < body.length; i++) {
			sb.append(body[i].toRDFoxExtensionFormat());
			if (i < body.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(" -> ");
		for (int i = 0; i < head.length; i++) {
			sb.append(head[i].toRDFoxExtensionFormat());
			if (i < head.length - 1) {
				sb.append(",");
			}
		}
		sb.append(" .\n");
		return sb.toString();
	}

	public Atom[] getHead() {
		return head;
	}

	public Atom[] getBody() {
		return body;
	}

	public static Atom[] eliminateTop(Atom[] atomSet) {
		boolean changed = false;
		List<Atom> atomSetCopy = new ArrayList<>(Arrays.asList(atomSet));
		Iterator<Atom> iterator = atomSetCopy.iterator();
		while (iterator.hasNext()) {
			Atom atom = iterator.next();
			if (isTop(atom)) {
				if (atomSetCopy.size() > 1) {
					iterator.remove();
					changed = true;
				}
			}
		}

		if (changed) {
			return atomSetCopy.toArray(new Atom[atomSetCopy.size()]);
		} else {
			return atomSet;
		}
	}

	private static boolean isTop(Atom atom) {
		return atom.getPredicate().equals(InternalizedEntities.OWL_THING.toStringID()) && atom.getArgs().length == 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.body);
		result = prime * result + Arrays.hashCode(this.head);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (!Arrays.equals(this.body, other.body))
			return false;
		if (!Arrays.equals(this.head, other.head))
			return false;
		return true;
	}

}
