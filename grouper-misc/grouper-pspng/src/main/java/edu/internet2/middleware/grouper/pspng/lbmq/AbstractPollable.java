package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * This class provides skeletal implementations of some {@link Pollable} operations. The implementations in this class
 * are appropriate when the base implementation does <em>not</em> allow <tt>null</tt> elements. Methods {@link #remove
 * remove}, and {@link #element element} are based on {@link #poll poll}, and {@link #peek peek}, respectively, but
 * throw exceptions instead of indicating failure via <tt>false</tt> or <tt>null</tt> returns.
 * 
 * <p>
 * A <tt>Pollable</tt> implementation that extends this class must minimally define methods {@link Queue#peek} and
 * {@link Queue#poll}. Typically, additional methods will be overridden as well.
 *
 * @param <E>
 *            the type of elements held in this collection
 */
public abstract class AbstractPollable<E> implements Pollable<E> {

    /**
     * Retrieves and removes the head of this queue. This method differs from {@link #poll poll} only in that it throws
     * an exception if this queue is empty.
     *
     * <p>
     * This implementation returns the result of <tt>poll</tt> unless the queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException
     *             if this queue is empty
     */
    public E remove() {
        E x = poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * Retrieves, but does not remove, the head of this queue. This method differs from {@link #peek peek} only in that
     * it throws an exception if this queue is empty.
     *
     * <p>
     * This implementation returns the result of <tt>peek</tt> unless the queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException
     *             if this queue is empty
     */
    public E element() {
        E x = peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

}
