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
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;


/** 
 * {@link Subject} from a {@link Membership} - getMember().getSubject()
 * only called if necessary i.e. the UI pages results and so it is often not
 * necessary to instantiate all the Subjects (and Members) 
 * <p/>
 * @author  Gary Brown.
 * @version $Id: LazySubject.java,v 1.5.2.1 2009-04-14 15:14:15 mchyzer Exp $
 */

public class LazySubject implements Subject {

  /**
   * 
   * @see java.lang.Object#toString()
   */
 @Override
  public String toString() {
   try {
     Member theMember = this.getMember();
     return "'" + theMember.getSubjectId() + "'/'"
     + theMember.getSubjectTypeId() + "'/'" + theMember.getSubjectSourceId() + "'";
   } catch (Exception e) {
     return "LazySubject with member uuid: " + this.member.getUuid();
     
   }
  }

/** membership if built from membership */
 private Membership membership;
 
 /** member if built from it or already retrieved it */
 private Member member;
 
 /** subject if it has lazily retrieved it already */
 private Subject subject;
 private SubjectType subjectType = new LazySubjectType();
 private Source subjectSource = new LazySource();
 boolean unresolvable = false;


  // CONSTRUCTORS //
  public LazySubject(Membership ms) 
  {
    this.membership = ms;
    try {
    	this.member = ms.getMember();
    }catch(MemberNotFoundException e) {
    	throw new GrouperRuntimeException(e);
    }
  } 

  /**
   * 
   * @param m
   */
  public LazySubject(Member member) {
	  this.member=member;
  }
  
  /**
 * @see edu.internet2.middleware.subject.Subject#getAttributes()
 */
public Map getAttributes() {
	try {
		return getSubject().getAttributes();
	}catch(Exception e) {
		unresolvable=true;
		throw new GrouperRuntimeException(e);
	}
}


	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String name) {
		try {
			return getSubject().getAttributeValue(name);
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
	 */
	public Set getAttributeValues(String name) {
		try {
			return getSubject().getAttributeValues(name);
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getDescription()
	 */
	public String getDescription() {
		try {
			return getSubject().getDescription();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getId()
	 */
	public String getId() {
		try {
			return member.getSubjectId();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
	}
	
	
	
	/**
	 * @see edu.internet2.middleware.subject.Subject#getName()
	 */
	public String getName() {
		try {
			return getSubject().getName();
		}catch(Exception e) {
			throw new GrouperRuntimeException(e);
		}
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
    return this.member.getSubjectSourceId();
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
		  if(subject==null) {
		    final String[] error = new String[1];
			  try {
			        this.subject = SubjectFinder.findById(
			          this.member.getSubjectId(), this.member.getSubjectTypeId(), this.member.getSubjectSourceId()
			        );
			        return subject;
            }
            catch (SubjectNotFoundException snfe) {
              error[0] = this.member.getSubjectId() + " entity not found";
			      }
			      catch (SourceUnavailableException eSU) {
              error[0] = this.member.getSubjectId() + " source unavailable " + this.member.getSubjectSourceId();
			      }
			      catch (SubjectNotUniqueException eSNU) {
              error[0] = this.member.getSubjectId() + " entity not unique";
			      }
			      //there was an error
			      this.subject = new Subject() {

              public String getAttributeValue(String name) {
                return error[0];
              }

              public Set getAttributeValues(String name) {
                return GrouperUtil.toSet(error[0]);
              }

              public Map getAttributes() {
                return new HashMap();
              }

              public String getDescription() {
                return error[0];
              }

              public String getId() {
                return LazySubject.this.member.getSubjectId();
              }

              public String getName() {
                return error[0];
              }

              public Source getSource() {
                return LazySubject.this.getSource();
              }

              public SubjectType getType() {
                return LazySubject.this.getType();
              }
			        
			      };
		  }
		  return subject;
	 }
	
	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other instanceof LazySubject) {
			return member.getUuid().equals(((LazySubject)other).getMember().getUuid());
		}
	    return SubjectHelper.eq(this, other);
	  } // public boolean equals(other)
	
	/**
	   * @return the hashcode
	   * @since 1.3.0
	   */ 
	  public int hashCode() {
	    return new HashCodeBuilder()
	      .append(  this.getId()              )
	      .append(  member.getSubjectSourceId()
	    		  )
	      .append(  member.getSubjectTypeId()  )
	      .toHashCode()
	      ;
	  }
	
	  /**
	   * 
	   * @return
	   */
	private Member getMember() {
		return member;
	}
	
	public Membership getMembership() {
		return membership;
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
			return member.getSubjectSourceId();
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
			return member.getSubjectTypeId();
		}
		
	}
}

