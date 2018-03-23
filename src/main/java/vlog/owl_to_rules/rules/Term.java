package vlog.owl_to_rules.rules;

public abstract class Term {

	private final String name;

	public Term(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract String toRDFoxFormat();

	public abstract String toVLogFormat();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
		Term other = (Term) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		return true;
	}

	
}
