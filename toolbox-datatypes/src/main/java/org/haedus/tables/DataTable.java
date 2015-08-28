/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 8/23/2015
 */
public class DataTable<E> implements ColumnTable<E> {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(DataTable.class);

	private final Map<String, List<E>> columns;
	
	private final List<List<E>> rows;
	private final List<String>  keys;

	private int numberOfRows;

	public DataTable() {
		columns = new LinkedHashMap<String, List<E>>();
		keys = new ArrayList<String>();
		rows = new ArrayList<List<E>>();
	}
	
	public DataTable(Map<String, List<E>> map) {
		this();
		
		// Ensure we can 
		if (!map.isEmpty()) {
			Iterator<List<E>> iterator = map.values().iterator();
			numberOfRows = iterator.next().size();
			while (iterator.hasNext()) {
				if (numberOfRows != iterator.next().size()) {
					throw new IllegalArgumentException("DataTable cannot be instantiated using a Map whose values" +
						" are Lists of inconsistent length");
				}
			}
			columns.putAll(map);
			keys.addAll(map.keySet());
			
			for ( int i =0; i < numberOfRows; i++) {
				List<E> row = new ArrayList<E>();
				for (String key : keys) {
					row.add(columns.get(key).get(i));
				}
				rows.add(row);
			}
		}
	}

	@Override
	public boolean hasKey(String key) {
		return keys.contains(key);
	}

	@Override
	public List<String> getKeys() {
		return Collections.unmodifiableList(keys);
	}

	@Override
	public List<E> getColumn(String key) {
		if (hasKey(key)) {
			return Collections.unmodifiableList(columns.get(key));
		} else {
			return null;
		}
	}

	@Override
	public Map<String, E> getRowAsMap(int index) {
		Map<String, E> map = new LinkedHashMap<String, E>();
		for (String key : keys) {
			map.put(key, columns.get(key).get(index));
		}
		return map;
	}

	@Override
	public List<E> getRow(int index) {
		checkRowIndex(index);
		return Collections.unmodifiableList(rows.get(index));
	}

	@Override
	public E get(int i, int j) {
		checkRowIndex(j);
		return columns.get(keys.get(i)).get(j);
	}

	@Override
	public void set(E element, int i, int j) {
		checkRowIndex(j);
		columns.get(keys.get(i)).set(j, element);
	}

	@Override
	public int getNumberRows() {
		return numberOfRows;
	}

	@Override
	public int getNumberColumns() {
		return keys.size();
	}

	@Override
	public String getPrettyTable() {
		return null; // TODO:
	}

	@Override
	public Iterator<List<E>> iterator() {
		return Collections.unmodifiableCollection(rows).iterator();
	}

	private void checkRowIndex(int index) {
		if (numberOfRows <= index) {
			throw new IndexOutOfBoundsException("Attempting to access row " + index + " of a table with only " + numberOfRows + " rows");
		}
	}
}
