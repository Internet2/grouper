/*--
$Id: Status.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
* This is a typesafe enumeration that identifies the various
* statuses that a Signet entity may have.
* 
*/
public class Status
implements Serializable
{
/**
 * Stores the external name of this instance, by which
 * it can be retrieved.
 */
private final String name;

/**
 * Stores the human-readable description of this instance, by which it is
 * identified in the user interface.
 */
private final transient String description;

/**
 * Return the external name associated with this instance. <p>
 * 
 * @return the name by which this instance is identified
 * 		in code.
 */
public String getName()
{
  return name;
}

/**
 * Return the description associated with this instance.
 * 
 * @return the human-readable description by which this
 * 		instance is identified in the user interface.
 */
public String getHelpText()
{
  return description;
}

/**
 * Keeps track of all instances by name, for efficient
 * lookup.
 */
private static final Map instancesByName = new HashMap();

/**
 * Constructor is private to prevent instantiation except
 * during class loading.
 * 
 * @param name the external name of the status value.
 * @param description the human readable description of
 * 		the status value, by which it is presented in the
 * 		user interface.
 */
private Status(String name, String description)
{
  this.name = name;
  this.description = description;

  // Record this instance in the collection that track
  // the enumeration.
  instancesByName.put(name, this);
}

/**
 * The instance that represents an active entity.
 */
public static final Status ACTIVE =
  new Status("active", "currently active");

/**
 * The instance that represents an inactive entity.
 */
public static final Status INACTIVE =
  new Status
	  ("inactive",
	   "inactive, exists only for the historical record");

/**
 * The instance that represents a pending entity.
 */
public static final Status PENDING =
  new Status
	  ("pending",
	   "pending, will become active when prerequisites are fulfilled");


/**
 * Obtain the collection of all legal enumeration values.
 *
 * @return all instances of this typesafe enumeration.
 */
public static Collection getAllValues()
{
  return Collections.unmodifiableCollection
	  (instancesByName.values());
}

/**
 * Look up an instance by name.
 *
 * @param name the external name of an instance.
 * @return the corresponding instance.
 * @throws NoSuchElementException if there is no such
 * 		instance.
 */
public static Status getInstanceByName(String name)
{
  Status result = (Status)instancesByName.get(name);
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
 * This guarantees that enums are never cloned, which is necessary
 * to preserve their "singleton" status.
 *
 * @return (never returns)
 * @throws CloneNotSupportedException
 */
protected final Object clone()
  throws CloneNotSupportedException
{
	throw new CloneNotSupportedException
		("Status instances are singletons, and so cannot be cloned.");
}

}
