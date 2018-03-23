package vlog.owl_to_rules.rules;

public class Constant extends Term {

	public Constant(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return "<" + super.getName() + ">";
	}

	// FIXME: not sure
	@Override
	public String toRDFoxFormat() {
		return "<" + super.getName() + ">";
	}

	// FIXME: not sure
	@Override
	public String toVLogFormat() {
		return "<" + super.getName() + ">";
	}
	
	

}
