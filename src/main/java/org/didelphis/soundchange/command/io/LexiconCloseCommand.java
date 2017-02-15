/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.io;

import org.didelphis.common.io.FileHandler;
import org.didelphis.common.language.enums.FormatterMode;
import org.didelphis.common.language.phonetic.Lexicon;
import org.didelphis.common.language.phonetic.LexiconMap;
import org.didelphis.common.language.phonetic.sequences.Sequence;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/13/2014
 */
public class LexiconCloseCommand extends AbstractLexiconIoCommand {

	private final LexiconMap lexicons;
	private final FormatterMode mode;

	public LexiconCloseCommand(LexiconMap lexParam, String path, String handle, FileHandler name, FormatterMode modeParam) {
		super(path, handle, name);
		lexicons = lexParam;
		mode = modeParam;
	}

	@Override
	public void run() {
		// REMOVE data from lexicons
		Lexicon lexicon = lexicons.remove(getHandle());

		StringBuilder sb = new StringBuilder();
		Iterator<List<Sequence>> i1 = lexicon.iterator();
		while (i1.hasNext()) {
			Iterator<Sequence> i2 = i1.next().iterator();
			while (i2.hasNext()) {
				Sequence sequence = i2.next();
				sb.append(sequence);
				if (i2.hasNext()) { sb.append('\t'); }

			}
			if (i1.hasNext()) { sb.append('\n'); }
		}
		String data = sb.toString().trim();
		String normalized = mode.normalize(data);
		getHandler().writeString(getPath(), normalized);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LexiconCloseCommand)) return false;
		if (!super.equals(o)) return false;
		LexiconCloseCommand that = (LexiconCloseCommand) o;
		return Objects.equals(lexicons, that.lexicons) && mode == that.mode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lexicons, mode);
	}

	@Override
	public String toString() {
		return "LexiconCloseCommand{" +
				"lexicons=" + lexicons +
				", mode=" + mode +
				'}';
	}
}
