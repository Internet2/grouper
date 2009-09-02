/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.subj;

import java.util.Set;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * {@link Subject} from id, type and source. Used when an actual subject could not be resolved.
 * Allows the UI to continue working when, otherwise, a SubjectNotFoundException would cause an error.
 * <p/>
 * @author  Gary Brown.
 * @version $Id: UnresolvableSubject.java,v 1.5 2009-09-02 05:57:26 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class UnresolvableSubject extends SubjectImpl {

  /** */
  private SubjectType subjectType = new LazySubjectType();

  /** */
  private Source subjectSource = new LazySource();

  /**
   * 
   * @param subjectId
   * @param subjectTypeId
   * @param sourceId
   */
  public UnresolvableSubject(String subjectId, String subjectTypeId, String sourceId) {
    super(subjectId, "Unresolvable:" + subjectId, "Unresolvable:" + subjectId,
        subjectTypeId, sourceId);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    return this.subjectSource;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return this.subjectType;
  }

  /**
   * Circumvent the need to instantiate a Subject to get a source id
   * @since 1.3.1
   *
   */
  class LazySource implements Source {

    /** */
    private Source source;

    /**
     * 
     * @return source
     */
    private Source getSource() {
      if (source != null)
        return source;
      try {
        source = SubjectFinder.getSource(getId());
      } catch (SourceUnavailableException e) {
        throw new GrouperException(e);
      }
      return source;
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getId()
     */
    public String getId() {
      return UnresolvableSubject.this.getSourceId();
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getName()
     */
    public String getName() {
      return getSource().getName();
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String)
     */
    @Deprecated
    public Subject getSubject(String id) throws SubjectNotFoundException,
        SubjectNotUniqueException {
      return getSource().getSubject(id, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String)
     */
    @Deprecated
    public Subject getSubjectByIdentifier(String id) throws SubjectNotFoundException,
        SubjectNotUniqueException {
      return getSource().getSubjectByIdentifier(id, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String, boolean)
     */
    public Subject getSubject(String id, boolean exceptionIfNull)
        throws SubjectNotFoundException, SubjectNotUniqueException {
      return getSource().getSubject(id, exceptionIfNull);
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String, boolean)
     */
    public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull)
        throws SubjectNotFoundException, SubjectNotUniqueException {
      return getSource().getSubjectByIdentifier(id, exceptionIfNull);
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#getSubjectTypes()
     */
    public Set getSubjectTypes() {
      return getSource().getSubjectTypes();
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#init()
     */
    public void init() throws SourceUnavailableException {
      getSource().init();

    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#search(java.lang.String)
     */
    public Set search(String query) {
      return getSource().search(query);
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#setId(java.lang.String)
     */
    public void setId(String id) {
      getSource().setId(id);

    }

    /**
     * 
     * @see edu.internet2.middleware.subject.Source#setName(java.lang.String)
     */
    public void setName(String name) {
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
  @SuppressWarnings("serial")
  class LazySubjectType extends SubjectType {

    /**
     * 
     */
    LazySubjectType() {

    }

    /**
     * 
     * @see edu.internet2.middleware.subject.SubjectType#getName()
     */
    public String getName() {
      return UnresolvableSubject.this.getTypeName();
    }

  }
}
