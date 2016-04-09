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

package org.haedus.tables;

/**
 * @param <E>
 * @author Samantha Fiona Morrigan McCabe
 */
public class SymmetricTable<E> extends RectangularTable<E> {

	protected SymmetricTable(int n) {
		super(n, n);
	}

	public SymmetricTable(E defaultValue, int n) {
		this(n);
		int number = getIndex(n, n)-1;
		for (int i = 0; i < number; i++) {
			array.add(defaultValue);
		}
	}

	public SymmetricTable(SymmetricTable<E> otherTable) {
		this(otherTable.getNumberRows());
		array.addAll(otherTable.array);
	}

	@Override
	public String getPrettyTable() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getNumberRows(); i++) {
			for (int j = 0; j <= i;j ++) {
				sb.append(get(i,j));
				if (j < i) {
					sb.append('\t');
				}
			}
			if (i < getNumberRows() -1) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("RefusedBequest")
	@Override
	public void set(E element, int i, int j) {
		int index = getIndex(i, j);
		array.set(index, element);
	}

	@SuppressWarnings("RefusedBequest")
	@Override
	public E get(int i, int j) {
		return array.get(getIndex(i, j));
	}


	private static int getIndex(int i, int j) {
		if (j > i) {
			return getRowStart(j) + i;
		} else {
			return getRowStart(i) + j;
		}
	}

	private static int getRowStart(int row) {
		int sum = 0;
		for (int i = 0; i <= row; i++) {
			sum += i;
		}
		return sum;
	}
}
