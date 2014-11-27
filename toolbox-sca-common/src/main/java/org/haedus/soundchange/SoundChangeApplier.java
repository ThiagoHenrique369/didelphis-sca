/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange;

import org.haedus.datatypes.SegmentationMode;
import org.haedus.datatypes.Segmenter;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.exceptions.ParseException;
import org.haedus.exceptions.VariableDefinitionFormatException;
import org.haedus.io.DiskFileHandler;
import org.haedus.io.NullFileHandler;
import org.haedus.soundchange.command.Command;
import org.haedus.soundchange.command.LexiconCloseCommand;
import org.haedus.soundchange.command.LexiconOpenCommand;
import org.haedus.soundchange.command.LexiconWriteCommand;
import org.haedus.soundchange.command.Rule;
import org.haedus.soundchange.command.ScriptExecuteCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.haedus.io.FileHandler;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/18/13
 * Time: 11:46 PM
 */
public class SoundChangeApplier {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplier.class);

	private static final String COMMENT_STRING = "%";
	private static final String NORMALIZATION  = "NORMALIZATION";
	private static final String SEGMENTATION   = "SEGMENTATION";
	private static final String RESERVE        = "RESERVE";
	private static final String EXECUTE        = "EXECUTE";
	private static final String IMPORT         = "IMPORT";
	private static final String OPEN           = "OPEN";
	private static final String WRITE          = "WRITE";
	private static final String CLOSE          = "CLOSE";
	private static final String FILEHANDLE     = "([A-Z0-9_]+)";
	private static final String FILEPATH       = "[\"\']([^\"\']+)[\"\']";

	private final FileHandler    fileHandler;
	private final FeatureModel   model;
	private final Queue<Command> commands;
	private final VariableStore  variables;

	private final Map<String, List<List<Sequence>>> lexicons;

	private SegmentationMode segmentationMode = SegmentationMode.DEFAULT;
	private NormalizerMode   normalizerMode   = NormalizerMode.NFC;

	public SoundChangeApplier() {
		model = new FeatureModel();
		variables = new VariableStore(model); // TODO: indicative of excess coupling?
		lexicons = new HashMap<String, List<List<Sequence>>>();
		commands = new ArrayDeque<Command>();
		fileHandler = new DiskFileHandler();
	}

	public SoundChangeApplier(Iterable<String> commandsParam) throws ParseException {
		this();
		parse(commandsParam);
	}

	// Package-private: for tests only
	SoundChangeApplier(String script) throws ParseException {
		this(script.split("\\s*(\\r?\\n|\\r)\\s*")); // Splits newlines and removes padding whitespace
	}

	// Package-private: for tests only
	SoundChangeApplier(String[] array) throws ParseException {
		this(array, new NullFileHandler());
	}

	// Package-private: for tests only
	SoundChangeApplier(String[] array, FileHandler fileHandlerParam) throws ParseException {
		model = new FeatureModel();
		variables = new VariableStore(model); // TODO: indicative of excess coupling?
		lexicons = new HashMap<String, List<List<Sequence>>>();
		commands = new ArrayDeque<Command>();
		fileHandler = fileHandlerParam;

		Collection<String> list = new ArrayList<String>();
		Collections.addAll(list, array);
		parse(list);
	}

	public void addLexicon(String handle, List<List<String>> lexicon) {
		List<List<Sequence>> sequences = getSequences(lexicon);
		lexicons.put(handle, sequences);
	}

	public FeatureModel getFeatureModel() {
		return model;
	}

	public VariableStore getVariables() {
		return variables;
	}

	public List<List<Sequence>> getLexicon(String handle) {
		return lexicons.get(handle);
	}

	public boolean hasLexicon(String handle) {
		return lexicons.containsKey(handle);
	}

	public Collection<List<List<Sequence>>> getLexicons() {
		return lexicons.values();
	}

	public NormalizerMode getNormalizerMode() {
		return normalizerMode;
	}

	public SegmentationMode getSegmentationMode() {
		return segmentationMode;
	}

	public List<List<Sequence>> removeLexicon(String handle) {
		return lexicons.remove(handle);
	}

	public void process() {
		for (Command command : commands) {
			command.execute();
		}
	}

	public List<List<Sequence>> getSequences(Iterable<List<String>> list) {
		List<List<Sequence>> lexicon = new ArrayList<List<Sequence>>();
		for (List<String> line : list) {
			List<Sequence> sequences = new ArrayList<Sequence>();
			for (String item : line) {
				String word = normalize(item);
				Sequence sequence = Segmenter.getSequence(word, model, variables, segmentationMode);
				sequences.add(sequence);
			}
			lexicon.add(sequences);
		}
		return lexicon;
	}

	// Testing only
	List<Sequence> processLexicon(List<String> list) {
		List<List<String>> lex = new ArrayList<List<String>>();
		lex.add(list);

		List<List<Sequence>> lexicon = getSequences(lex);
		lexicons.put("DEFAULT", lexicon);
		// Should test later if this is better than for-each
		for (Command command : commands) {
			command.execute();
		}
		return lexicon.get(0);
	}

	private void parse(Iterable<String> strings) throws ParseException {

		for (String string : strings) {
			if (!string.startsWith(COMMENT_STRING) && !string.isEmpty()) {
				String trimmedCommand = string.replaceAll(COMMENT_STRING + ".*", "");

				String command = normalize(trimmedCommand);

				if (command.startsWith(EXECUTE)) {
					executeScript(command);
				} else if (command.startsWith(IMPORT)) {
					importScript(command);
				} else if (command.startsWith(OPEN)) {
					openLexicon(command);
				} else if (command.startsWith(WRITE)) {
					writeLexicon(command);
				} else if (command.startsWith(CLOSE)) {
					closeLexicon(command);
				} else if (command.contains("=")) {
					assignVariable(command);
				} else if (command.contains(">")) {
					commands.add(new Rule(command, lexicons, model, variables, segmentationMode));
				} else if (command.startsWith(NORMALIZATION)) {
					setNormalization(command);
				} else if (command.startsWith(SEGMENTATION)) {
					setSegmentation(command);
				} else if (command.startsWith(RESERVE)) {
					String reserve = command.replaceAll(RESERVE + ":? *", "");
					for (String symbol : reserve.split(" +")) {
						model.addSegment(symbol);
					}
				} else if (command.startsWith("BREAK")) {
					// Stop parsing commands
					break;
				} else {
					LOGGER.warn("Unrecognized Command: {}", string);
				}
			}
		}
	}

	private void assignVariable(String command) {
		try {
			variables.add(command);
		} catch (VariableDefinitionFormatException e) {
			LOGGER.error("Error parsing variable assignment.", e);
		}
	}

	/**
	 * OPEN "some_lexicon.txt" (as) FILEHANDLE to load the contents of that file into a lexicon stored against the file-handle;
	 *
	 * @param command the whole command staring from OPEN, specifying the path and file-handle
	 */
	private void openLexicon(String command) throws ParseException {
		Pattern pattern = Pattern.compile(OPEN + "\\s+" + FILEPATH + "\\s+(as\\s)?" + FILEHANDLE);
		Matcher matcher = pattern.matcher(command);

		if (matcher.lookingAt()) {
			String path = matcher.group(1);
			String handle = matcher.group(3);
			commands.add(new LexiconOpenCommand(path, handle, fileHandler, this));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * CLOSE FILEHANDLE (as) "some_output2.txt" to close the file-handle and save the lexicon to the specified file.
	 *
	 * @param command the whole command starting from CLOSE, specifying the file-handle and path
	 * @throws ParseException
	 */
	private void closeLexicon(String command) throws ParseException {
		Pattern pattern = Pattern.compile(CLOSE + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
		Matcher matcher = pattern.matcher(command);

		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			commands.add(new LexiconCloseCommand(lexicons, path, handle, fileHandler));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * WRITE FILEHANDLE (as) "some_output1.txt" to save the current state of the lexicon to the specified file, but leave the handle open
	 *
	 * @param command the whole command starting from WRITE, specifying the file-handle and path
	 * @throws ParseException
	 */
	private void writeLexicon(String command) throws ParseException {
		Pattern pattern = Pattern.compile(WRITE + "\\s+" + FILEHANDLE + "\\s+(as\\s)?" + FILEPATH);
		Matcher matcher = pattern.matcher(command);

		if (matcher.lookingAt()) {
			String handle = matcher.group(1);
			String path = matcher.group(3);
			commands.add(new LexiconWriteCommand(lexicons, path, handle, fileHandler));
		} else {
			throw new ParseException("Command seems to be ill-formatted: " + command);
		}
	}

	/**
	 * IMPORT other rule files, which basically inserts those commands into your current rule file;
	 * Unlike other commands, this runs immediately and insert the
	 *
	 * @param command the whole command starting with 'IMPORT'
	 */
	private void importScript(String command) throws ParseException {
		String path = command.replaceAll(IMPORT + "\\s+", "").replaceAll("\"|\'", "");
		List<String> strings = fileHandler.readLines(path);
		parse(strings);
	}

	/**
	 * EXECUTE other rule files, which just does what that rule file does in a separate process;
	 *
	 * @param command the whole command starting with 'EXECUTE'
	 */
	private void executeScript(String command) throws ParseException {
		String path = command.replaceAll(EXECUTE + "\\s+", "");
		commands.add(new ScriptExecuteCommand(path));
	}

	/**
	 * Sets the normalization mode of the sound change applier
	 *
	 * @param command the whole command, beginning with NORMALIZATION
	 */
	private void setNormalization(String command) throws ParseException {
		String mode = command.replaceAll(NORMALIZATION + ": *", "");
		try {
			normalizerMode = NormalizerMode.valueOf(mode);
		} catch (IllegalArgumentException e) {
			throw new ParseException(e.getMessage());
		}
	}

	/**
	 * Sets the segmentation mode of the sound change applier
	 *
	 * @param command the whole command, beginning with SEGMENTATION
	 */
	private void setSegmentation(String command) throws ParseException {
		String mode = command.replaceAll(SEGMENTATION + ": *", "");

		if (mode.startsWith("FALSE")) {
			segmentationMode = SegmentationMode.NAIVE;
		} else if (mode.startsWith("TRUE")) {
			segmentationMode = SegmentationMode.DEFAULT;
		} else {
			throw new ParseException("Unrecognized segmentation mode \"" + mode + "\"");
		}
	}

	private String normalize(String string) {
		if (normalizerMode == NormalizerMode.NONE) {
			return string;
		} else {
			return Normalizer.normalize(string, Normalizer.Form.valueOf(normalizerMode.toString()));
		}
	}
}
