package vlog.owl_to_rules;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import vlog.owl_to_rules.rules.Atom;
import vlog.owl_to_rules.rules.Commons;
import vlog.owl_to_rules.rules.SourceToTargetProgram;

public class VlogChaseConverter {
	private SourceToTargetProgram sourceToTargetProgram;

	

	public VlogChaseConverter(SourceToTargetProgram sourceToTargetProgram) {
		super();
		this.sourceToTargetProgram = sourceToTargetProgram;
	}

	
	public void writeRules(final String rulesFolder, final String ontoName) throws IOException {
		// .st-tgds.txt
		// .t-tgds.txt
		final File rules = new File(rulesFolder + File.separator + ontoName + ".rules");
		Commons.initializeFile(rules);
		Commons.appendRulesToVLogFile(sourceToTargetProgram.getSourceToTargetTGDs(), rules);
		Commons.appendRulesToVLogFile(sourceToTargetProgram.getTargetToTargetTGDs(), rules);
	}
	
	public void writeRules(final String rulesFolder) throws IOException {
		// .st-tgds.txt
		// .t-tgds.txt
		final File rules = new File(rulesFolder + File.separator +"rules");
		Commons.initializeFile(rules);
		Commons.appendRulesToVLogFile(sourceToTargetProgram.getSourceToTargetTGDs(), rules);
		Commons.appendRulesToVLogFile(sourceToTargetProgram.getTargetToTargetTGDs(), rules);
	}
	

//	public void writeFactsToTurtleFile(final String ttlFactsFolder, final String ontoName) throws IOException {
//		final File factsFile = new File(ttlFactsFolder + File.separator + ontoName + ".facts.nt");
//		Commons.initializeFile(factsFile);
//		Commons.appendFactsToFile(sourceToTargetProgram.getSourceIdFacts(), factsFile);
//	}

	/**
	 * EDB0_predname=hospital <br>
	 * EDB0_type=INMEMORY<br>
	 * EDB0_param0=/Users/ceriel/Projects/vlog-runs/data/doctors.new/db-10K<br>
	 * EDB0_param1=hospital
	 * 
	 * @throws IOException
	 */
	public void generateInMemDbConfigFile(final String ontoName, final String confFileFolder, final String dbLocation)
			throws IOException {
		final File inMemConfFile = new File(confFileFolder + File.separator + ontoName + ".conf.mem");
		Commons.initializeFile(inMemConfFile);

		Map<String, Set<Atom>> factsPerOrigPredicate = sourceToTargetProgram.getFacts();

		Collection<String> origPredicates = sourceToTargetProgram.getPredicateIds().keySet();
		int i = 0;
		for (String origPred : origPredicates) {
			if (factsPerOrigPredicate.containsKey(origPred) && !factsPerOrigPredicate.get(origPred).isEmpty()) {
				String sourcePredId = sourceToTargetProgram.getRuleSourcePredId(origPred);
				String predConfig = generatePredConfig(sourcePredId, i, dbLocation);
				Commons.writeToFile(inMemConfFile, predConfig, true);
				i++;
			}

		}
	}

	private String generatePredConfig(final String sourcePred, final int index, final String dbLocation) {
		String edbPrefix = "EDB" + index + "_";
		StringBuilder sb = new StringBuilder();
		sb.append(edbPrefix).append("predname=").append(sourcePred).append("\n").append(edbPrefix)
				.append("type=INMEMORY").append("\n").append(edbPrefix).append("param0=").append(dbLocation)
				.append("\n").append(edbPrefix).append("param1=").append(sourcePred).append("\n");
		return sb.toString();
	}

	public void generateInTridentDbConfigFile() {
		// TODO
	}

}
