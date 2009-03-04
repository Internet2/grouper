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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;


/** 
 * {@link Subject} from id, type and source. Used when an actual subject could not be resolved.
 * Allows the UI to continue working when, otherwise, a SubjectNotFoundException would cause an error.
 * <p/>
 * @author  Gary Brown.
 * @version $Id: UnresolvableSubject.java,v 1.1 2009-03-04 15:33:31 isgwb Exp $
 */

public class UnresolvableSubject implements Subject {

 private String subjectId;
 private String subjectTypeId;
 private String subjectSourceId;
 private Map attributes = new HashMap();

 private SubjectType subjectType = new LazySubjectType();
 private Source subjectSource = new LazySource();



  // CONSTRUCTORS //
  public UnresolvableSubject(String subjectId, String subjectTypeId, String sourceId) 
  {
    this.subjectId=subjectId;
    this.subjectTypeId=subjectTypeId;
    this.subjectSourceId=sourceId;
  } 

   
  /**
 * @see edu.internet2.middleware.subject.Subject#getAttributes()
 */
public Map getAttributes() {
	return attributes;
}


	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String name) {
		return null;
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
	 */
	public Set getAttributeValues(String name) {
		return new HashSet();
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getDescription()
	 */
	public String getDescription() {
		return "Unresolvable:" + subjectId;
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getId()
	 */
	public String getId() {
		return subjectId;
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getName()
	 */
	public String getName() {
		return "Unresolvable:" + subjectId;
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getSource()
	 */
	public Source getSource() {
		return subjectSource;
		}
	
  /** get the source id 
   * @return the soruce id */
  public String getSourceId() {
    return subjectSourceId;
	}
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getType()
	 */
	public SubjectType getType() {
    return subjectType;
		}
	
	/**
	 * 
	 * @return the subject
	 * @throws SubjectNotFoundException
	 */
	private Subject getSubject() throws SubjectNotFoundException{
		  throw new SubjectNotFoundException("Unresolvable subject:" + subjectSourceId);
	 }
	
	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		
	    return SubjectHelper.eq(this, other);
	  } // public boolean equals(other)
	
	/**
	   * @return the hashcode
	   * @since 1.3.0
	   */ 
	  public int hashCode() {
	    return new HashCodeBuilder()
	      .append(  this.getId()              )
	      .append(  subjectSourceId
	    		  )
	      .append(  subjectTypeId  )
	      .toHashCode()
	      ;
	  }
	
	/**
	 * Circumvent the need to instantiate a Subject to get a source id
	 * @since 1.3.1
	 *
	 */
	class LazySource implements Source {
		private Source source;
		LazySource() {
			
		}
		
		/**
		 * 
		 * @return source
		 */
		private Source getSource() {
			if(source!=null) return source;
			try {
				source=SubjectFinder.getSource(getId());
			}catch(SourceUnavailableException e) {
				throw new GrouperRuntimeException(e);
	}
			return source;
		}
  
		public String getId() {
			// TODO Auto-generated method stub
			return subjectSourceId;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return getSource().getName();
		}

		public Subject getSubject(String id) throws SubjectNotFoundException,
				SubjectNotUniqueException {
			// TODO Auto-generated method stub
			return getSource().getSubject(id);
		}

		public Subject getSubjectByIdentifier(String id)
				throws SubjectNotFoundException, SubjectNotUniqueException {
			// TODO Auto-generated method stub
			return getSource().getSubjectByIdentifier(id);
		}

		public Set getSubjectTypes() {
			// TODO Auto-generated method stub
			return getSource().getSubjectTypes();
		}

		public void init() throws SourceUnavailableException {
			getSource().init();
			
		}

		public Set search(String query) {
			// TODO Auto-generated method stub
			return getSource().search(query);
		}

		public void setId(String id) {
			getSource().setId(id);
			
		}

		public void setName(String name) {
			// TODO Auto-generated method stub
			getSource().setName(name);
			
		}

    /**
     * @see edu.internet2.middleware.subject.Source#checkConfig()
     */
    public void checkConfig() {
    }
    /**
     * @see edu.internet2.middleware.subject.Source#printConfig()
     */
    public String printConfig() {
      String message = "sources.xml lazy source id:   " + this.getId();
      return message;
    }
		
	}
	
	/**
	 * Circumvent the need to instantiate an actual Subject just to get the type
	 * @since 1.3.1
	 */
	class LazySubjectType extends SubjectType{

		LazySubjectType() {
			
		}
		
		public String getName() {
			// TODO Auto-generated method stub
			return subjectTypeId;
		}
		
	}
}

