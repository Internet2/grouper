/*--
$Id: SubjectTypeAdapterImpl.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.subject.AbstractSubjectTypeAdapter;
import edu.internet2.middleware.subject.AdapterUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectTypeAdapter;

/**
* This implementation of SubjectTypeAdapter provides Signet's own native,
* database-persisted subjects.
*/
class SubjectTypeAdapterImpl
extends AbstractSubjectTypeAdapter
implements SubjectTypeAdapter
{
private Signet signet;

SubjectTypeAdapterImpl(Signet signet)
{
  super();
  this.signet = signet;
}

void setSignet(Signet signet)
{
  this.signet = signet;
}

public SubjectTypeAdapterImpl()
{
  super();
}

public boolean isModifiable()
{
  // Native Signet subjects are always modifiable.
  return true;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#getSubject(java.lang.String)
 */
public Subject getSubject
	(SubjectType 	subjectType,
	 String 			id)
throws SubjectNotFoundException
{
  SubjectImpl subjectImpl = null;
  
  try
  {
    subjectImpl = this.signet.getNativeSignetSubject(subjectType, id);
  }
  catch (ObjectNotFoundException onfe)
  {
    throw new SubjectNotFoundException
    	("Unable to find subject with subjectType '"
    	 + subjectType
    	 + "' and id '"
    	 + id
    	 + "'.",
    	 onfe);
  }
  
  if (subjectImpl == null)
  {
    throw new SubjectNotFoundException
    	("The native Signet SubjectTypeAdapter was unable to find the"
    	 + " Subject with ID='"
    	 + id
    	 + "'.");
  }
  
  subjectImpl.setSignet(this.signet);
  return subjectImpl;
}

public Subject getSubjectByDisplayId
(SubjectType 	subjectType,
 String 			displayId)
throws SubjectNotFoundException
{
Subject subject
	= this.signet.getNativeSignetSubjectByDisplayId(subjectType, displayId);

if (subject == null)
{
  throw new SubjectNotFoundException
  	("The native Signet SubjectTypeAdapter was unable to find the"
  	 + " Subject with displayId='"
  	 + displayId
  	 + "'.");
}

return subject;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#quickSearch(java.lang.String)
 */
public Subject quickSearch(String searchValue)
{
  throw new SignetRuntimeException
  	("I need more information about this method's function.");
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#searchByIdentifier(java.lang.String)
 */
public Subject[] searchByIdentifier(SubjectType type, String id)
{
  Subject 	subject;
  Subject[]	subjectArray;
  
  try
  {
    subject = this.signet.getNativeSignetSubject(type, id);
  }
  catch (ObjectNotFoundException e)
  {
    subject = null;
  }
  
  if (subject == null)
  {
    subjectArray = new Subject[0];
  }
  else
  {
    subjectArray = new Subject[1];
    subjectArray[0] = subject;
  }
  
  return subjectArray;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#init()
 */
public void init() throws AdapterUnavailableException
{
  // This SubjectTypeAdapter has no initialization to perform.
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#destroy()
 */
public void destroy()
{
  // This SubjectTypeAdapter has no destroy-time actions to perform.
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#newSubject(java.lang.String)
 */
public Subject newSubject
	(SubjectType 	type,
	 String 			id, 
	 String 			name, 
	 String 			description, 
	 String 			displayId)
throws OperationNotSupportedException
{
  if (this.isModifiable() == false)
  {
    throw new OperationNotSupportedException
    						("The SubjectTypeAdapter '" 
    						 + this.getClass().getName() 
    						 + "' is read-only. The attempt to create the new"
    						 + " Subject '"
    						 + id
    						 + "' failed.");
  }
  
  this.signet = ((SubjectTypeImpl)type).getSignet();
  
  SubjectImpl subjectImpl
  	= new SubjectImpl(this.signet, type, id, name, description, displayId);
  
  // Signet application programs will never explicitly persist a new
  // Subject, because they won't know where that Subject actually
  // resides. That's why we're doing it here, for a Subject that we know
  // resides in the SQL database. Signet transactiona always nest, so this
  // operation will either be part of some larger transaction that's
  // already in progress, or will commit as its own small transaction.
  this.signet.beginTransaction();
  this.signet.save(subjectImpl);
  this.signet.commit();
  
  return subjectImpl;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.subject.SubjectTypeAdapter#getSubjects()
 */
public Subject[] getSubjects(SubjectType subjectType)
{
  List subjectList = this.signet.getNativeSignetSubjects(subjectType);
  Subject[] subjectArray = new Subject[0];
  subjectArray = (Subject[])(subjectList.toArray(subjectArray));
  
  return subjectArray;
}
}
