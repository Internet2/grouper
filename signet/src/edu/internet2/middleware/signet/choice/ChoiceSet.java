/*--
$Id: ChoiceSet.java,v 1.1 2005-01-12 23:49:24 mnguyen Exp $
$Date: 2005-01-12 23:49:24 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;


public interface ChoiceSet {

	/*
	 * Returns ID of this ChoiceSet.
	 */
	public String getId();
	
	/*
	 * Indicates if this ChoiceSet is restricted to the returned
	 * Subsystem ID. A null value indicates this ChoiceSet
	 * is usable by any Subsystem.
	 */
	public String getSubsystemId();

	/*
	 * Returns the ChoiceSet adapter.
	 */
	public ChoiceSetAdapter getChoiceSetAdapter();

	/*
	 * Returns an array of Choices for this ChoiceSet.
	 */
	public Choice[] getChoices();

}
