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
/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee.status;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectCheckConfig;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticSourceTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagnosticSourceTest) {
      DiagnosticSourceTest other = (DiagnosticSourceTest)obj;
      return new EqualsBuilder().append(this.sourceId, other.sourceId).isEquals();
    }
    return false;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.sourceId).toHashCode();
  }

  /** sourceId */
  private String sourceId;

  /**
   * construct with source id
   * @param theSourceId
   */
  public DiagnosticSourceTest(String theSourceId) {
    this.sourceId = theSourceId;
  }
  
  /**
   * cache the results
   */
  private static GrouperCache<String, Boolean> sourceCache = new GrouperCache<String, Boolean>("sourceDiagnostic", 100, false, 120, 120, false);
  
  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    if (sourceCache.containsKey(this.sourceId)) {

      this.appendSuccessTextLine("Source checked successfully recently");

    } else {

      Source source = SourceManager.getInstance().getSource(this.sourceId);
      
      String findSubjectOnCheckConfigString = source.getInitParam(SubjectCheckConfig.FIND_SUBJECT_BY_ID_ON_CHECK_CONFIG);
      boolean findSubjectOnCheckConfig = SubjectUtils.booleanValue(findSubjectOnCheckConfigString, true);
      
      if (findSubjectOnCheckConfig) {
        String subjectToFindOnCheckConfig = source.getInitParam(SubjectCheckConfig.SUBJECT_ID_TO_FIND_ON_CHECK_CONFIG);
        subjectToFindOnCheckConfig = SubjectUtils.defaultIfBlank(subjectToFindOnCheckConfig, SubjectCheckConfig.GROUPER_TEST_SUBJECT_BY_ID);
        source.getSubject(subjectToFindOnCheckConfig, false);
        this.appendSuccessTextLine("Searched for subject by id: " + subjectToFindOnCheckConfig);
        sourceCache.put(this.sourceId, Boolean.TRUE);
      } else {
        this.appendSuccessTextLine("Not configured to check source by id");
      }

    }
    
    return true;
    
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "source_" + this.sourceId;
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Source " + this.sourceId;
  }

}
