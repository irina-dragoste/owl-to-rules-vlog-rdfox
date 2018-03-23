package vlog.owl_to_rules.rules;

import java.util.Arrays;

import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class Atom {

	public static final String CSV_COMMA_DELIMITER = ",";
	public static final String CSV_NEW_LINE_SEPARATOR = "\n";

	private final String predicate;
	private final Term[] args;

	public Atom(String predicate, Term... args) {
		this.predicate = predicate;
		this.args = args;
	}

	public boolean isUnary() {
		return args.length == 1;
	}

	public boolean isBinary() {
		return args.length == 2;
	}

	public String toTurtleAtom() {
		if (isUnary())
			return args[0] + " <" + OWLRDFVocabulary.RDF_TYPE + "> <" + predicate + "> .";
		else
			return args[0] + " <" + predicate + "> " + args[1] + " .";
	}

	public String toCSVFormat() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			if (i < args.length - 1) {
				sb.append(CSV_COMMA_DELIMITER);
			}
		}
		sb.append(CSV_NEW_LINE_SEPARATOR);
		return sb.toString();
	}

	public String toRDFoxFormat() {
		if (isUnary())
			return "<" + predicate + ">(" + args[0].toRDFoxFormat() + ")";
		else
			return "<" + predicate + ">(" + args[0].toRDFoxFormat() + ", " + args[1].toRDFoxFormat() + ")";
	}

	public String toRDFoxExtensionFormat() {
		if (isUnary())
			return predicate + "(" + args[0].toRDFoxFormat() + ")";
		else
			return predicate + "(" + args[0].toRDFoxFormat() + ", " + args[1].toRDFoxFormat() + ")";
	}

	public String toVLogFormat() {
		if (isUnary())
			return  predicate + "(" + args[0] + ")";
		else
			return  predicate + "(" + args[0] + "," + args[1] + ")";
	}

	/**
	 * treatment { <br>
	 * id : INTEGER, <br>
	 * patient : STRING, <br>
	 * hospital : STRING, <br>
	 * npi : INTEGER, <br>
	 * conf : DOUBLE <br>
	 * }
	 * 
	 * @param predicate
	 * @param arrity
	 * @return
	 */
	public static String generatePredicateSchema(final String predicate, final int arrity) {
		StringBuilder sb = new StringBuilder(predicate);
		sb.append(" {\n");
		for (int i = 0; i < arrity; i++) {
			sb.append(" c").append(i).append(" : STRING");
			if (i < arrity - 1) {
				sb.append(",\n");
			}
		}
		sb.append("\n}\n");
		return sb.toString();
	}

	public String getPredicate() {
		return predicate;
	}

	public Term[] getArgs() {
		return args;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.args);
		result = prime * result + ((this.predicate == null) ? 0 : this.predicate.hashCode());
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
		Atom other = (Atom) obj;
		if (!Arrays.equals(this.args, other.args))
			return false;
		if (this.predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!this.predicate.equals(other.predicate))
			return false;
		return true;
	}
	
	
	

}
