/*--
  $Id: TreeType.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

/**
A SubjectType may be a person, organization, or group.

*/

public interface TreeType
{
	public String getId();

	public String getName();

	public TreeTypeAdapter getAdapter();
}
