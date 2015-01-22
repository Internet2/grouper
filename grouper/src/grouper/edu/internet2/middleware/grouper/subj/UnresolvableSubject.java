/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.LazySource;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * {@link Subject} from id, type and source. Used when an actual subject could not be resolved.
 * Allows the UI to continue working when, otherwise, a SubjectNotFoundException would cause an error.
 * <p/>
 * @author  Gary Brown.
 * @version $Id: UnresolvableSubject.java,v 1.6 2009-10-31 16:27:12 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class UnresolvableSubject extends SubjectImpl {

  /** */
  private SubjectType subjectType = new LazySubjectType();

  /** */
  private Source subjectSource = null;

  /**
   * string to label unresolvable
   */
  private String unresolvableString = null;
  
  /**
   * string to label unresolvable
   * @return the unresolvableString
   */
  public String getUnresolvableString() {
    return this.unresolvableString;
  }

  /**
   * 
   * @param subjectId
   * @param subjectTypeId
   * @param sourceId
   */
  public UnresolvableSubject(String subjectId, String subjectTypeId, String sourceId) {
    this(subjectId, subjectTypeId, sourceId, "Unresolvable");
  }

  /**
   * format an id and unresolvable string with a colon
   * @param unresolvableString
   * @param id
   * @return the string
   */
  private static String formatUnresolvableString(String unresolvableString, String id) {
    if (!StringUtils.isBlank(id)) {
      return unresolvableString + ": " + id;
    }
    return unresolvableString;
  }
  
  /**
   * 
   * @param subjectId
   * @param subjectTypeId
   * @param sourceId
   * @param theUnresolvableString string to be before the subject id
   */
  public UnresolvableSubject(String subjectId, String subjectTypeId, 
      String sourceId, String theUnresolvableString) {
    //note, if you change this "Unresolvable:", you need to change it in the UI too where it is subsituted
    super(subjectId, formatUnresolvableString(theUnresolvableString, subjectId), 
        formatUnresolvableString(theUnresolvableString, subjectId),
        subjectTypeId, sourceId);
    this.unresolvableString = theUnresolvableString;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    if (this.subjectSource == null) {
      this.subjectSource = new LazySource(this.getSourceId());
    }
    return this.subjectSource;
  }

  /**
   * @see edu.internet2.middleware.subject.provider.SubjectImpl#setSourceId(java.lang.String)
   */
  @Override
  public void setSourceId(String sourceId1) {
    super.setSourceId(sourceId1);
    this.subjectSource = null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return this.subjectType;
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
