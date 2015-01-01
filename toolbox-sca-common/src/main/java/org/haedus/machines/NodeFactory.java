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

package org.haedus.machines;

import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Samantha Fiona Morrigan McCabe
 * Date: 9/10/13
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeFactory {

	private static int parallelCounter = 0;
	private static int machineCounter  = 0;
	private static int nodeCounter     = 0;

	private static final Node<Sequence> BLANK_NODE = new PlainNode("EMPTY_NODE", null, true);

	public static Node<Sequence> getParallelStateMachine(List<Expression> expression, SequenceFactory factoryParam, ParseDirection direction, boolean accepting) {
		parallelCounter++;
		return new ParallelStateMachine("P-" + parallelCounter, expression, factoryParam, direction, accepting);
	}

	public static Node<Sequence> getParallelStateMachine(String expression, SequenceFactory factoryParam, ParseDirection direction, boolean accepting) {
		parallelCounter++;
		return new ParallelStateMachine("P-" + parallelCounter, expression, factoryParam, direction, accepting);
	}

	public static Node<Sequence> getStateMachine(List<Expression> expression, SequenceFactory factoryParam, ParseDirection direction, boolean accepting) {
		machineCounter++;
		return new StateMachine("S-" + machineCounter, expression, factoryParam, direction, accepting);
	}

	public static Node<Sequence> getStateMachine(String expression, SequenceFactory factoryParam, ParseDirection direction, boolean accepting) {
		machineCounter++;
		return new StateMachine("S-" + machineCounter, expression, factoryParam, direction, accepting);
	}

	public static Node<Sequence> getNode(SequenceFactory factory) {
		return getNode(factory, false);
	}

	public static Node<Sequence> getNode(SequenceFactory factory, boolean accepting) {
		nodeCounter++;
		return new PlainNode("N-" + nodeCounter, factory, accepting);
	}

	public static Node<Sequence> getBlankNode() {
		return BLANK_NODE;
	}
}
