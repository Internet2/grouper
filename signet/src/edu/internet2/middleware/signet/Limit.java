/*--
$Id: Limit.java,v 1.3 2005-02-01 19:48:20 acohen Exp $
$Date: 2005-02-01 19:48:20 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet;

import edu.internet2.middleware.signet.choice.ChoiceSet;

public interface Limit
{
	/**
	 * @return the limit ID.
	 */
	public String getId();
	
	/**
	 * 
	 * @return the ChoiceSet associated with this Limit.
	 * @throws ObjectNotFoundException
	 */
	public ChoiceSet getChoiceSet() throws ObjectNotFoundException;
	
	/**
	 * @return the limit name.
	 */
	public String getName();
	
	/**
	 * @return the help text for this limit.
	 */
	public String getHelpText();
}
