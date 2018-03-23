package vlog.owl_to_rules.rules;

public class Variable extends Term {

	public Variable(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return super.getName();
	}

	@Override
	public String toRDFoxFormat() {
		return "?" + super.getName();
	}

	@Override
	public String toVLogFormat() {
		return super.getName();
	}


}
