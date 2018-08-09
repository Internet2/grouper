/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*--
$Id: NullSourceAdapter.java,v 1.4 2009-03-22 02:49:26 mchyzer Exp $
$Date: 2009-03-22 02:49:26 $

Copyright (C) 2006 Internet2 and The University Of Chicago.  
All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import edu.internet2.middleware.subject.*;

import java.util.*;
import org.apache.commons.logging.*;

/**
 * Null {@link Source} which will never return any {@link Subject}s.
 * @author  blair christensen.
 * @version $Id: NullSourceAdapter.java,v 1.4 2009-03-22 02:49:26 mchyzer Exp $
 */
public class NullSourceAdapter extends BaseSourceAdapter {

  /** */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(NullSourceAdapter.class);

  /** */
  private static final Set<SubjectType> TYPES = new HashSet<SubjectType>();

  static {
    TYPES.add(SubjectTypeEnum.valueOf("application"));
    TYPES.add(SubjectTypeEnum.valueOf("group"));
    TYPES.add(SubjectTypeEnum.valueOf("person"));
  } // static

  // Constructors //	

  /**
   * Allocates new {@link NullSourceAdapter}.
   */
  public NullSourceAdapter() {
    super();
  } // public NullSourceAdapter()

  /**
   * Allocates new {@link NullSourceAdapter}.
   * @param id1    The source id for the new adapter.
   * @param name1  The source name for the new adapter.
   */
  public NullSourceAdapter(String id1, String name1) {
    super(id1, name1);
  } // public NullSourceAdapter(id, name)

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String, boolean)
   */
  @Override
  public Subject getSubject(String id1, boolean exceptionIfNull) throws SubjectNotFoundException {
    if (exceptionIfNull) {
      throw new SubjectNotFoundException("Subject " + id1 + " not found.");
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String, boolean)
   */
  @Override
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull) throws SubjectNotFoundException {
    if (exceptionIfNull) {
      throw new SubjectNotFoundException("Subject " + id1 + " not found.");
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectTypes()
   */
  @Override
  public Set<SubjectType> getSubjectTypes() {
    return TYPES;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#init()
   */
  @Override
  public void init() throws SourceUnavailableException {
    // Nothing
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#search(java.lang.String)
   */
  @Override
  public Set<Subject> search(String searchValue) {
    return new HashSet<Subject>();
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
    String message = "subject.properties null source id:   " + this.getId();
    return message;
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubject(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubject(id1, true);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubjectByIdentifier(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubjectByIdentifier(id1, true);
  }

}
