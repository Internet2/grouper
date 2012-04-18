/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.subject;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * linked hash map with case insensitive matching and tolower attribute names (e.g. for keyset)
 * @author mchyzer
 * @param <E> type of set
 *
 */
@SuppressWarnings("serial")
public class SubjectCaseInsensitiveSetImpl<E> extends LinkedHashSet<E> implements
    SubjectCaseInsensitiveSet {

  /**
   * 
   */
  public SubjectCaseInsensitiveSetImpl() {
    super();
  }

  /**
   * 
   * @param c
   */
  public SubjectCaseInsensitiveSetImpl(Collection<? extends E> c) {
    super(Math.max(2*c.size(), 11), .75f);
    addAll(c);
  }
  
  /**
   * @see Set#add(Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean add(E e) {
    if (e instanceof String) {
      e = (E)((String)e).toLowerCase();
    }
    return super.add(e);
  }

  /**
   * @see Set#addAll(Collection)
   */
  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    Iterator<? extends E> e = c.iterator();
    while (e.hasNext()) {
      if (add(e.next())) {
        modified = true;
      }
    }
    return modified;
  }

  /**
   * @see Set#containsAll(Collection)
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    Iterator<?> e = c.iterator();
    while (e.hasNext()) {
      if (!contains(e.next())) {
        return false;
      }
    }
    return true;
  }

  /**
   * @see Set#retainAll(Collection)
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    boolean modified = false;
    Iterator<E> e = iterator();
    while (e.hasNext()) {
      if (!c.contains(e.next())) {
        e.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * @see Set#contains(Object)
   */
  @Override
  public boolean contains(Object o) {
    if (o instanceof String) {
      o = ((String)o).toLowerCase();
    }
    return super.contains(o);
  }

  /**
   * @see Set#remove(Object)
   */
  @Override
  public boolean remove(Object o) {
    if (o instanceof String) {
      o = ((String)o).toLowerCase();
    }
    return super.remove(o);
  }

  /**
   * @see Set#removeAll(Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    boolean modified = false;

    if (size() > c.size()) {
        for (Iterator<?> i = c.iterator(); i.hasNext(); )
            modified |= remove(i.next());
    } else {
        for (Iterator<?> i = iterator(); i.hasNext(); ) {
            if (c.contains(i.next())) {
                i.remove();
                modified = true;
            }
        }
    }
    return modified;
  }

  
  
}
