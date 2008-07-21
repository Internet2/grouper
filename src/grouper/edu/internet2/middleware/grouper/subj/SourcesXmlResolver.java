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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 * Wrapper around Subject sources configured in <code>sources.xml</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SourcesXmlResolver.java,v 1.7 2008-07-21 04:43:59 mchyzer Exp $
 * @since   1.2.1
 */
public class SourcesXmlResolver implements SubjectResolver {


  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(SourcesXmlResolver.class);
  
  
  private static  ParameterHelper param = new ParameterHelper();
  private         SourceManager   mgr;


  /**
   * Initialize a new <i>SourcesXmlResolver</i>.
   * @throws  IllegalArgumentException if <i>mgr</i> is null.
   * @since   1.2.1
   */
  public SourcesXmlResolver(SourceManager mgr) 
    throws  IllegalArgumentException
  {
    param.notNullSourceManager(mgr); 
    this.mgr = mgr;
    // TODO 20070809 this isn't the ideal or right place for this but in the interest of getting things going...
    this.mgr.loadSource(
      new InternalSourceAdapter( InternalSourceAdapter.ID, InternalSourceAdapter.NAME ) 
    );
  }


  /**
   * @see     SubjectResolver#find(String)
   * @since   1.2.1
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources() ) {
      try {
        subjects.add( sa.getSubject(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    return this.thereCanOnlyBeOne(subjects, id);
  }            

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources(type) ) {
      try {
        subjects.add( sa.getSubject(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    return this.thereCanOnlyBeOne(subjects, id);
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return this.getSource(source).search(query);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources() ) {
      try {
        subjects.add( sa.getSubjectByIdentifier(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    return this.thereCanOnlyBeOne(subjects, id);
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List<Subject> subjects = new ArrayList();
    for ( Source sa : this.getSources(type) ) {
      try {
        subjects.add( sa.getSubjectByIdentifier(id) );
      }
      catch (SubjectNotFoundException eSNF) {
        // ignore.  subject might be in another source.
      }
    }    
    return this.thereCanOnlyBeOne(subjects, id);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   1.2.1
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
   * @since   1.2.1
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException { 
    try {
      return this.mgr.getSource(id);
    } catch (SourceUnavailableException sue) {
      throw new SourceUnavailableException("Cant find source with id: '" + id + "', " 
          + this.sourceIdErrorSafe(), sue);
    }
  }
  
  /**
   * @see     SubjectResolver#getSources()
   * @since   1.2.1
   */
  public Set<Source> getSources() {
    return new LinkedHashSet( this.mgr.getSources() );
  }

  /**
   * return the error string related to source id's, dont fail
   * @return the error string related to source id's, dont fail
   */
  private String sourceIdErrorSafe() {
    try {
      StringBuilder result = new StringBuilder();
      result.append("Possible source id's: ");
      for (Source source : this.getSources()) {
        result.append("'").append(source.getId()).append("', ");
      }
      return result.toString();
    } catch (Exception e) {
      LOG.error("Error calculating source id error message: ", e);
    }
    return "";
  }
  
  /**
   * @see     SubjectResolver#getSources(String)
   * @since   1.2.1
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  { 
    return new LinkedHashSet( this.mgr.getSources( SubjectTypeEnum.valueOf(subjectType) ) );
  }

  /**
   * @param   subjects  List of found subjects.
   * @param   id        Subject identifier used in query.
   * @return  Matching subject if there is only one.
   * @throws  SubjectNotFoundException if less than 1 matching subjects found.
   * @throws  SubjectNotUniqueException if more than 1 matching subjects found.
   * @since   1.2.1
   */
  private Subject thereCanOnlyBeOne(List<Subject> subjects, String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if      (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    }
    else if (subjects.size() == 1) {
      return subjects.get(0);
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() );
  }

}

