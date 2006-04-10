/*--
$Id: LimitShape.java,v 1.5 2006-04-10 07:22:02 ddonn Exp $
$Date: 2006-04-10 07:22:02 $

Copyright (c) 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * This is a typesafe enumeration that identifies the various shapes
 * a Signet Limit may have.
 */
public class LimitShape implements ITypeSafeEnum
{
	/////////////////////////////////
	// static
	/////////////////////////////////

	/** implements Serializable */
	private static final long serialVersionUID = 1L;

	/** Keeps track of all instances by name, for efficient lookup. Use Hashtable
	 * to disallow null values and also support synchronized access.
	*/
	private static final Hashtable instancesByName;

	/** The instance that represents a ChoiceSet. */
	public static final LimitShape CHOICE_SET;

	/** The instance that represents a Tree. */
	public static final LimitShape TREE;

	static
	{
		instancesByName = new Hashtable(2, 1.0f);

		CHOICE_SET = new LimitShape("choice_set", "A set of discrete choices");
		instancesByName.put(CHOICE_SET.name, CHOICE_SET);

		TREE = new LimitShape("tree",
				"A hierarchical tree, with the possibility of multiple roots and multiple parents for each node.");
		instancesByName.put(TREE.name, TREE);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.ITypeSafeEnum#getInstanceByName(java.lang.String)
	 */
	public static Object getInstanceByName(String name) throws NoSuchElementException
	{
		LimitShape result = (LimitShape)instancesByName.get(name);
		if (null == result)
			throw new NoSuchElementException(name);
		return (result);
	}


	////////////////////////////////
	// instance
	////////////////////////////////

	/**
	 * Stores the external name of this instance, by which it can be retrieved.
	 */
	private final String	name;

	/**
	 * Stores the human-readable description of this instance, by which
	 * it is identified in the user interface.
	 */
	private final transient String	description;

	/**
	 * Constructor is private to prevent instantiation except during class loading.
	 * @param name The external name of the DataType value.
	 * @param description The human-readable description of the DataType value;
	 * 	presented in the user interface.
	 */
	private LimitShape(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	////////////////////////////
	// implements ITypeSafeEnum
	////////////////////////////

	/**
	 * Return the external name associated with this instance.
	 * @return the name by which this instance is identified in code.
	 */
	public String getName() { return name; }

	/**
	 * Return the description associated with this instance.
	 * @return the human-readable description by which this instance is identified in the user interface.
	 */
	public String getHelpText() { return description; }


	//////////////////////////
	// Serializable support
	//////////////////////////

	/** Insure that deserialization preserves the signleton property. */
	private Object readResolve() { return (getInstanceByName(name)); }


	////////////////////////////
	// overrides Object
	////////////////////////////

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException
			("Instances of type-safe enumerations are singletons and cannot be cloned.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return (name); }

}
