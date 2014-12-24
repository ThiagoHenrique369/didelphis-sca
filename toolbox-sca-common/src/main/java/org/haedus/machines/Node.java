package org.haedus.machines;

import java.util.Collection;

/**
 * Created by Samantha F M McCabe on 12/21/14.
 */
public interface Node<T> {

    /**
     * Checks if the object has any outgoing connections to other nodes
     * @return true iff
     */
    boolean isEmpty();

    /**
     *
     * @param target
     * @return
     */
    boolean matches(T target);

    /**
     * Adds a single node on a blank arc
     * @param node the node to add; must not be null
     */
    void add(Node<T> node);

    /**
     * Adds an outgoing connection to another node
     * @param arcValue the label of the outgoing arc
     * @param node the node to connect to
     */
    void add(T arcValue, Node<T> node);

    /**
     * Checks for the presence of any outgoing arcs with the provided labels
     * @param arcValue the label value to test for
     * @return true iff an arc exists with the label provided
     */
    boolean hasArc(T arcValue);

    /**
     * Returns all nodes reachable from the curent node by an arc with the provided label
     * @param arcValue
     * @return
     */
    Collection<Node<T>> getNodes(T arcValue);

    /**
     * Returns a set containing labels used in outgoing arcs; may contain null
     * @return a set of objects representing the labels of outgoing arcs; the set may contain null, but cannot be null itself.
     */
    Collection<T> getKeys();

    boolean isAccepting();

    /**
     * Each node should have an id associated with it; this method returns it
     * @return the node's id code
     */
    String getId();

    void setAccepting(boolean acceptingParam);
}
