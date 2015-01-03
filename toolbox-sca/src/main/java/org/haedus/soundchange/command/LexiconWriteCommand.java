/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.soundchange.command;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.io.FileHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconWriteCommand extends LexiconIOCommand {

	private final Map<String, List<List<Sequence>>> lexicons;

	public LexiconWriteCommand(Map<String, List<List<Sequence>>> lexiconParam, String pathParam, String handleParam, FileHandler handlerParam) {
		super(pathParam, handleParam, handlerParam);
		lexicons = lexiconParam;
	}

	@Override
	public void execute() {

		List<List<Sequence>> lexicon = lexicons.get(fileHandle);
		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence sequence = i2.next();
				sb.append(sequence);
				if (i2.hasNext()) sb.append("\t");

			}
			if (i1.hasNext()) sb.append("\n");
		}
		fileHandler.writeString(filePath, sb.toString().trim());
	}
}
