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
import  edu.internet2.middleware.grouper.InternalSourceAdapter;
import  edu.internet2.middleware.subject.Source;
import  edu.internet2.middleware.subject.SourceUnavailableException;
import  edu.internet2.middleware.subject.Subject;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;
import  edu.internet2.middleware.subject.provider.SourceManager;
import  edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import  java.util.ArrayList;
import  java.util.LinkedHashSet;
import  java.util.List;
import  java.util.Set;


/**
 * Wrapper around Subject sources configured in <code>sources.xml</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SourcesXmlResolver.java,v 1.1 2007-08-09 18:55:21 blair Exp $
 * @since   @HEAD@
 */
public class SourcesXmlResolver implements SubjectResolver {


  private SourceManager mgr;


  /**
   * Initialize a new <i>SourcesXmlResolver</i>.
   * @throws  IllegalArgumentException if <i>mgr</i> is null.
   * @since   @HEAD@
   */
  public SourcesXmlResolver(SourceManager mgr) 
    throws  IllegalArgumentException
  {
    if (mgr == null) { // TODO 20070803 ParameterHelper
      throw new IllegalArgumentException("null SourceManager");
    }
    this.mgr = mgr;
    // TODO 20070809 this isn't the ideal or right place for this but in the interest of getting things going...
    this.mgr.loadSource(
      new InternalSourceAdapter( InternalSourceAdapter.ID, InternalSourceAdapter.NAME ) 
    );
  }


  /**
   * @see     SubjectResolver#find(String)
   * @since   @HEAD
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#find(String, String)
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources() ) {
      try {
        subjects.add( sa.getSubject(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    if      (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    }
    else if (subjects.size() == 1) {
      return subjects.get(0);
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() );
  }            

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#find(String)
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources(type) ) {
      try {
        subjects.add( sa.getSubject(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    if      (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    }
    else if (subjects.size() == 1) {
      return subjects.get(0);
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() );
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getSource(source).getSubject(id);
    if ( type.equals( subj.getType().getName() ) ) {
      return subj;
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    Set<Subject> subjects = new LinkedHashSet();
    for ( Source sa : this.getSources() ) {
      subjects.addAll( sa.search(query) );
    }
    return subjects;
  }

  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return this.getSource(source).search(query);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#findByIdentifier(String, String)
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources() ) {
      try {
        subjects.add( sa.getSubjectByIdentifier(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    if      (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    }
    else if (subjects.size() == 1) {
      return subjects.get(0);
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() );
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#findByIdentifier(String)
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources(type) ) {
      try {
        subjects.add( sa.getSubjectByIdentifier(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    if      (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    }
    else if (subjects.size() == 1) {
      return subjects.get(0);
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() );
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getSource(source).getSubjectByIdentifier(id);
    if ( type.equals( subj.getType().getName() ) ) {
      return subj;
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   @HEAD@
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return this.mgr.getSource(id);
  }
 
  /**
   * @see     SubjectResolver#getSources()
   * @since   @HEAD@
   */
  public Set<Source> getSources() {
    return new LinkedHashSet( this.mgr.getSources() );
  }

  /**
   * @see     SubjectResolver#getSources(String)
   * @since   @HEAD@
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  {
    return new LinkedHashSet( this.mgr.getSources( SubjectTypeEnum.valueOf(subjectType) ) );
  }

}

