/*
 * @author mchyzer
 * $Id: JdbcSubjectAttributeSet.java,v 1.2 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.lang.ObjectUtils;

/**
 * unmodifiable, holds exactly one value, lightweight set
 * @param <E> is type of set
 */
public class JdbcSubjectAttributeSet<E> implements Set<E> {

  /** element */
  private E element;
  
  /**
   * construct
   * @param theElement
   */
  public JdbcSubjectAttributeSet(E theElement) {
    this.element = theElement;
  }
  
  /**
   * @see java.util.Map#clear()
   */
  public void clear() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#add(java.lang.Object)
   */
  public boolean add(E o) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends E> c) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return ObjectUtils.equals(o, this.element);
  }

  /**
   * @see java.util.Set#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> c) {
    
    return c.size() == 1 && this.contains(c.iterator().next());
  }

  /**
   * @see java.util.Set#isEmpty()
   */
  public boolean isEmpty() {
    return false;
  }

  /**
   * @see java.util.Set#iterator()
   */
  public Iterator<E> iterator() {
    return new SingletonIterator(this.element, false);
  }

  /**
   * @see java.util.Set#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> c) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> c) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see java.util.Set#size()
   */
  public int size() {
    return 1;
  }

  /**
   * @see java.util.Set#toArray()
   */
  public Object[] toArray() {
    return new Object[]{this.element};
  }

  /**
   * @see java.util.Set#toArray(T[])
   */
  public <T> T[] toArray(T[] a) {
    if (a.length < 1) {
      a = (T[])Array.newInstance(a.getClass().getComponentType(), 1);
    }
    a[0] = (T)this.element;
    return a;
  }
}
