/*--
$Id: Limit.java,v 1.2 2005-01-21 20:30:47 acohen Exp $
$Date: 2005-01-21 20:30:47 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet;

public interface Limit
{
	/*
	 * Returns the limit ID.
	 */
	public String getLimitId();
	
	/*
	 * Returns the limit type which can be a ChoiceSet or a Tree.
	 */
	public String getLimitType();
	
	/*
	 * Returns the ID of a ChoiceSet or Tree whose values/nodes are
	 * the domain of limit values.
	 */
	public String getLimitTypeId();
	
	/*
	 * Returns the limit name.
	 */
	public String getName();
	
	/*
	 * Returns the help text for this limit.
	 */
	public String getHelpText();
	
	//May not need datatype if each choice has a rank field
	//for comparison?
	//public String getDataType();

	/*
	 * Returns the value type of this limit. This is used
	 * for generating a Privileges XML document.
	 */
	public ValueType getValueType();

}
