package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Queue;

/**
 * This class provides skeletal implementations of some {@link Offerable} operations. The implementations in this class
 * are appropriate when the base implementation does <em>not</em> allow <tt>null</tt> elements. Method {@link #add add}
 * is based on {@link #offer offer}, but throws exceptions instead of indicating failure via <tt>false</tt> or
 * <tt>null</tt> returns.
 *
 * <p>
 * An <tt>Offerable</tt> implementation that extends this class must minimally define a method {@link Queue#offer} which
 * does not permit insertion of <tt>null</tt> elements, along with methods {@link Collection#size}, and
 * {@link Collection#iterator}. Typically, additional methods will be overridden as well. If these requirements cannot
 * be met, consider instead subclassing {@link AbstractCollection}.
 * 
 * @param <E>
 *            the type of elements held in this collection
 */
public abstract class AbstractOfferable<E> extends AbstractCollection<E> implements Offerable<E> {

    public boolean add(E e) {
        if (offer(e))
            return true;
        else
            throw new IllegalStateException("Queue full");
    }

    /**
     * Adds all of the elements in the specified collection to this queue. Attempts to addAll of a queue to itself
     * result in <tt>IllegalArgumentException</tt>. Further, the behavior of this operation is undefined if the
     * specified collection is modified while the operation is in progress.
     *
     * <p>
     * This implementation iterates over the specified collection, and adds each element returned by the iterator to
     * this queue, in turn. A runtime exception encountered while trying to add an element (including, in particular, a
     * <tt>null</tt> element) may result in only some of the elements having been successfully added when the associated
     * exception is thrown.
     *
     * @param c
     *            collection containing elements to be added to this queue
     * @return <tt>true</tt> if this queue changed as a result of the call
     * @throws ClassCastException
     *             if the class of an element of the specified collection prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified collection contains a null element and this queue does not permit null elements, or
     *             if the specified collection is null
     * @throws IllegalArgumentException
     *             if some property of an element of the specified collection prevents it from being added to this
     *             queue, or if the specified collection is this queue
     * @throws IllegalStateException
     *             if not all the elements can be added at this time due to insertion restrictions
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

}
