/*--
  $Id: Subject.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject;

import edu.internet2.middleware.signet.ObjectNotFoundException;

/**
 * A Subject represents an entity, such as a person, group, or 
 * organization.
 * 
 */
public interface Subject
{
  /**
   * @return Returns a short mnemonic ID which will appear in XML
   * 		documents and other documents used by analysts. This ID, when
   * 		paired with a {@link SubjectType}, uniquely identifies a
   * 		single Subject.
   */
	public String getId();

	/**
	 * 
	 * @return The {@link SubjectType} of this Subject. The SubjectType
	 * 		describes both the nature of the Subject (e.g. "person",
	 *    "program", etc.) and the {@link SubjectTypeAdapter} that
	 * 		provides access to the Subject.
	 */
	public SubjectType getSubjectType();

	/**
	 * 
	 * @return A short alphanumeric ID that this Subject uses for
	 * 		self-identification, perhaps in a login or email context. While a
	 *    Subject's primary ID (see {@link #getId()}) will never change,
	 * 		the Subject may choose to change his or her displayId from
	 *    time to time. For example, a Subject with the primary ID of
	 * 		"0014368214" may choose to change his displayID from "cat.stevens"
	 * 		to "yusuf.islam". Similarly, a department whose role has been
	 * 		re-defined may change its name from "buses" to "transportation".
	 * @throws SubjectNotFoundException
	 * @throws ObjectNotFoundException
	 * 
	 */
	public String getDisplayId() throws SubjectNotFoundException, ObjectNotFoundException;

	/**
	 * 
	 * @return A printable String, containing the name of this Subject in
	 * 		some default displayable format. The exact details
   * 		of the representation are unspecified and subject to change.
   * 		Keep in mind that this will sometimes be the name of a person,
   * 		and sometimes the name of an application program or other
   * 		enterprise entity.
	 * @throws SubjectNotFoundException
	 * @throws ObjectNotFoundException
	 * @throws ObjectNotFoundException
	 */
	public String getName() throws ObjectNotFoundException;

	/**
	 * 
	 * @return A textual description of this Subject. For example, a person's
	 * 		description might be "Chair, Signet Working Group" or "Chicago,
	 * 		Grouper Project".
	 */
	public String getDescription();
	
	public void addAttribute(String name, String value);

	public String[] getAttributeArray(String name);
}
