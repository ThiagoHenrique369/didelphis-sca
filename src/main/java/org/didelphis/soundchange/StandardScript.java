/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this filePath except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.didelphis.soundchange;

import org.didelphis.common.io.FileHandler;
import org.didelphis.common.language.phonetic.LexiconMap;
import org.didelphis.soundchange.parser.ScriptParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * User: Samantha Fiona Morrigan McCabe
 * Date: 4/18/13
 * Time: 11:46 PM
 */
public class StandardScript implements SoundChangeScript {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StandardScript.class);

	private final FileHandler handler;
	private final String filePath;
	private final Queue<Runnable> commands;
	private final LexiconMap lexicons;

	public StandardScript(String filePath, CharSequence script, FileHandler handler,  ErrorLogger logger) {
		this.filePath = filePath;
		this.handler  = handler;

		ScriptParser scriptParser = new ScriptParser(filePath, script, handler, logger);
		scriptParser.parse();

		lexicons = scriptParser.getMemory().getLexicons();
		commands = scriptParser.getCommands();
	}

	@Override
	public FileHandler getHandler() {
		return handler;
	}
	
	@Override
	public Queue<Runnable> getCommands() {
		return commands;
	}

	@Override
	public LexiconMap getLexicons() {
		return lexicons;
	}

	@Override
	public void process() {
		for (Runnable command : commands) {
			command.run();
		}
	}

	@Override
	public String toString() {
		return "StandardScript{"+ filePath +'}';
	}
}
