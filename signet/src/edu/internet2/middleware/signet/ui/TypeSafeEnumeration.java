/*--
$Id: TypeSafeEnumeration.java,v 1.3 2006-02-09 10:34:06 lmcrae Exp $
$Date: 2006-02-09 10:34:06 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.ui;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
abstract class TypeSafeEnumeration
implements Serializable
{

  /**
   * Stores the external name of this instance, by which it can be
   * retrieved.
   */
  protected final String           name;

  /**
   * Stores the human-readable description of this instance, by which
   * it is identified in the user interface.
   */
  protected final transient String description;

  /**
   * Return the external name associated with this instance.
   * <p>
   * 
   * @return the name by which this instance is identified in code.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Return the description associated with this instance.
   * 
   * @return the human-readable description by which this instance is
   * 	identified in the user interface.
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Keeps track of all instances by name, for efficient lookup.
   */
  private static final Map instancesByName = new HashMap();

  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   * 		the external name of the DataType value.
   * @param description
   *    the human readable description of the DataType value,
   * 	  by which it is presented in the user interface.
   */
  protected TypeSafeEnumeration(String name, String description)
  {
    this.name = name;
    this.description = description;

    // Record this instance in the collection that track
    // the enumeration.
    instancesByName.put(name, this);
  }

  /**
   * Obtain the collection of all legal enumeration values.
   * 
   * @return all instances of this typesafe enumeration.
   */
  public static Collection getAllValues()
  {
    return Collections.unmodifiableCollection(instancesByName.values());
  }

  /**
   * Look up an instance by name.
   * 
   * @param name
   *          the external name of an instance.
   * @return the corresponding instance.
   * @throws NoSuchElementException
   *           if there is no such instance.
   */
  public static Object getInstanceByName(String name)
  {
    Object result = instancesByName.get(name);
    if (result == null)
    {
      throw new NoSuchElementException(name);
    }

    return result;
  }

  /**
   * Return a string representation of this object.
   */
  public String toString()
  {
    return name;
  }

  /**
   * Insure that deserialization preserves the signleton property.
   */
  private Object readResolve()
  {
    return getInstanceByName(name);
  }

  /**
   * This guarantees that enums are never cloned, which is necessary to preserve
   * their "singleton" status.
   * 
   * @return (never returns)
   * @throws CloneNotSupportedException
   */
  protected final Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException
    	("Instances of type-safe enumerations are singletons,"
    	 + " and so cannot be cloned.");
  }
  
  public boolean equals(Object obj)
  {
    TypeSafeEnumeration other = (TypeSafeEnumeration)obj;
    return this.name.equals(other.name);
  }
  
  public int hashCode()
  {
    return this.name.hashCode();
  }
}
