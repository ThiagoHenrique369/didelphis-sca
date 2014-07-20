package org.haedusfc.soundchange;

import org.apache.commons.io.FileUtils;
import org.haedusfc.datatypes.phonetic.Sequence;
import org.haedusfc.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/28/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException, RuleFormatException {

		double startTime = System.nanoTime();
		if (args.length == 3) {

			String lexiconPath = args[0];
			String rulesPath   = args[1];
			String outputPath  = args[2];

			List<String> lexicon = FileUtils.readLines(new File(lexiconPath), "UTF-8");
			List<String> rules   = FileUtils.readLines(new File(rulesPath), "UTF-8");

			SoundChangeApplier sca = new SoundChangeApplier(rules);

			Collection<String> output = new ArrayList<String>();
			for (Sequence sequence : sca.processLexicon(lexicon)) {
				output.add(sequence.toStringClean());
			}

			FileUtils.writeLines(new File(outputPath), "UTF-8", output,"\r\n");

		} else {
			LOGGER.error("You need to provide three parameters: lexicon, rules, and output.");
		}
		double elapsedTime = System.nanoTime() - startTime;

		double time = (elapsedTime / Math.pow(10,9));

		LOGGER.info("Finished in {} seconds", time);
	}
}
