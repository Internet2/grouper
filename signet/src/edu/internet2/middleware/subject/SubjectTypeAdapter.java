/*--
  $Id: SubjectTypeAdapter.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject;

import javax.naming.OperationNotSupportedException;

/**
 * Adapter to find and get Subjects.
 * 
 * This interface should be implemented by anyone who wants to use some
 * {@link Subject} implementation other than the default, 
 * database-persistent one provided with Signet.
 * 
 */

public interface SubjectTypeAdapter
{
	public Subject getSubject(SubjectType type, String id)
		throws SubjectNotFoundException;
	
	public Subject getSubjectByDisplayId(SubjectType type, String displayId)
	throws SubjectNotFoundException;

	public Subject quickSearch(String searchValue);

	public Subject[] searchByIdentifier(SubjectType type, String id);
	
	public Subject[] getSubjects(SubjectType type);

	public void init()
		throws AdapterUnavailableException;
	
  boolean isModifiable();
  
  public Subject newSubject
  	(SubjectType type,
  	 String id,
  	 String name,
  	 String description,
  	 String displayID)
  	throws OperationNotSupportedException;

	public void destroy();
}
