/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.subj;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import  edu.internet2.middleware.subject.*;
import  java.util.Map;
import  java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * {@link Subject} from a {@link Membership} - getMember().getSubject()
 * only called if necessary i.e. the UI pages results and so it is often not
 * necessary to instantiate all the Subjects (and Members) 
 * <p/>
 * @author  Gary Brown.
 * @version $Id: LazySubject.java,v 1.1 2008-07-21 04:43:59 mchyzer Exp $
 */

public class LazySubject implements Subject {

 private Membership membership;
 private Subject subject;


  // CONSTRUCTORS //
  public LazySubject(Membership ms) 
  {
    this.membership = ms;
  } // protected LazySubject(ms)

  
  
  /* (non-Javadoc)
 * @see edu.internet2.middleware.subject.Subject#getAttributes()
 */
public Map getAttributes() {
	try {
		return getSubject().getAttributes();
	}catch(Exception e) {
		throw new GrouperRuntimeException(e);
	}
}


	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String name) {
		try {
			return getSubject().getAttributeValue(name);
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
	 */
	public Set getAttributeValues(String name) {
		try {
			return getSubject().getAttributeValues(name);
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getDescription()
	 */
	public String getDescription() {
		try {
			return getSubject().getDescription();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getId()
	 */
	public String getId() {
		try {
			return membership.getMember().getSubjectId();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getName()
	 */
	public String getName() {
		try {
			return getSubject().getName();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getSource()
	 */
	public Source getSource() {
		try {
			return membership.getMember().getSubjectSource();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.subject.Subject#getType()
	 */
	public SubjectType getType() {
		try {
			return membership.getMember().getSubjectType();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	private Subject getSubject() throws SubjectNotFoundException,MemberNotFoundException{
		  if(subject==null) subject=membership.getMember().getSubject();
		  return subject;
	 }
	
	public boolean equals(Object other) {
		if(other instanceof LazySubject) {
			return membership.getMemberUuid().equals(((LazySubject)other).getMembership().getMemberUuid());
		}
	    return SubjectHelper.eq(this, other);
	  } // public boolean equals(other)
	
	/**
	   * @since 1.3.0
	   */ 
	  public int hashCode() {
	    return new HashCodeBuilder()
	      .append(  this.getId()              )
	      .append(  this.getSource().getId()  )
	      .append(  this.getType().getName()  )
	      .toHashCode()
	      ;
	  } // public int hashCode()
	
	public Membership getMembership() {
		return membership;
	}
  

} // public class LazySubject implements Subject

