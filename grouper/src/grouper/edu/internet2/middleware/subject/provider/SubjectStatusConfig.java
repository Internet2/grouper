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
package edu.internet2.middleware.subject.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * config for a source of the status params
 * @author mchyzer
 */
public class SubjectStatusConfig {

  /**
   * default constructor
   */
  public SubjectStatusConfig() {
    
  }
  
  /**
   * 
   * @param source
   */
  public SubjectStatusConfig(Source source) {

    this.sourceId = source.getId();
    this.statusAllFromUser = source.getInitParam("statusAllFromUser");
    this.statusDatastoreFieldName = source.getInitParam("statusDatastoreFieldName");
    
    //lets make these to lower so they are easy to search for
    {
      String statusesFromUser = source.getInitParam("statusesFromUser");
      if (!StringUtils.isBlank(statusesFromUser)) {
        for (String statusFromUser : SubjectUtils.splitTrim(statusesFromUser, ",")) {
          this.statusesFromUser.add(statusFromUser.toLowerCase());
        }
      }
    }
    
    this.statusLabel = source.getInitParam("statusLabel");
    this.statusSearchDefault = source.getInitParam("statusSearchDefault");
    
    for (int i=0;i<50;i++) {
      
      String statusTranslateUser = source.getInitParam("statusTranslateUser" + i);
      String statusTranslateDatastore = source.getInitParam("statusTranslateDatastore" + i);
      
      if (StringUtils.isBlank(statusTranslateUser)) {
        break;
      }
      
      if (StringUtils.isBlank(statusTranslateDatastore)) {
        throw new RuntimeException("Why is statusTranslateDatastore" + i + " blank in subject.properties for source: " + sourceId);
      }
      
      this.statusTranslateUserToDatastore.put(statusTranslateUser, statusTranslateDatastore);
    }
    

  }

  /**
   * 
   * @return the status
   */
  public boolean isStatusConfigured() {
    return !StringUtils.isBlank(this.statusDatastoreFieldName);
  }
  
  /** source id for exceptions */
  private String sourceId = null;
  
  /**
   * source id for exceptions
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id for exceptions
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }
  
  
  /**
   * see if the configuration is ok for this source
   */
  public void validate() {
    
    boolean hasStatusAllFromUser = !StringUtils.isBlank(this.statusAllFromUser);
    
    boolean hasStatusDatastoreFieldName = !StringUtils.isBlank(this.statusDatastoreFieldName);

    boolean hasStatusesFromUser = SubjectUtils.length(this.statusesFromUser) > 0;
    
    boolean hasStatusLabel = !StringUtils.isBlank(this.statusLabel);
    
    boolean hasStatusSearchDefault = !StringUtils.isBlank(this.statusLabel);
    
    boolean hasStatusTemplateUserToDatastore = SubjectUtils.length(this.statusTranslateUserToDatastore) > 0;
    
    //has nothing, thats ok
    if (!hasStatusAllFromUser && !hasStatusDatastoreFieldName && !hasStatusesFromUser 
        && !hasStatusLabel && !hasStatusSearchDefault && !hasStatusTemplateUserToDatastore) {
      return;
    }

    //which ones are required?
    if (!hasStatusDatastoreFieldName) {
      throw new RuntimeException("'statusDatastoreFieldName' is required in the configuration of source: " + this.getSourceId());
    }

    if (!hasStatusLabel) {
      throw new RuntimeException("'statusLabel' is required in the configuration of source: " + this.getSourceId());
    }
    
    if (!hasStatusAllFromUser) {
      throw new RuntimeException("'statusAllFromUser' is required in the configuration of source: " + this.getSourceId());
    }
    
    if (!hasStatusSearchDefault) {
      throw new RuntimeException("'statusSearchDefault' is required in the configuration of source: " + this.getSourceId());
    }
    
    
    
    
  }
  
  /**
   * column or attribute which represents the status
   */
  private String statusDatastoreFieldName;

  /**
   * search string from user which represents the status.  e.g. status=active
   */
  private String statusLabel;

  /**
   * available statuses from screen (if not specified, any will be allowed). comma separated list
   */
  private Set<String> statusesFromUser = new HashSet<String>();

  /**
   * all label from the user
   */
  private String statusAllFromUser;

  /**
   * if no status is specified, this will be used (e.g. for active only).  Note, the value should be of the
   * form the user would type in, e.g. status=active
   */
  private String statusSearchDefault;

  /**
   * translate between screen values of status, and the data store value.  Increment the 0 to 1, 2, etc for more translations.
   * so the user could enter: status=active, and that could translate to status_col=A.  The 'user' is what the user types in,
   * the 'datastore' is what is in the datastore.  The user part is not case-sensitive.  Note, this could be a many to one 
   */
  private Map<String, String> statusTranslateUserToDatastore = new HashMap<String, String>();

  /**
   * column or attribute which represents the status
   * @return column or attribute
   */
  public String getStatusDatastoreFieldName() {
    return this.statusDatastoreFieldName;
  }

  /**
   * column or attribute which represents the status
   * @param statusDatastoreFieldName1
   */
  public void setStatusDatastoreFieldName(String statusDatastoreFieldName1) {
    this.statusDatastoreFieldName = statusDatastoreFieldName1;
  }

  /**
   * search string from user which represents the status.  e.g. status=active
   * @return label
   */
  public String getStatusLabel() {
    return this.statusLabel;
  }

  /**
   * search string from user which represents the status.  e.g. status=active
   * @param statusLabel1
   */
  public void setStatusLabel(String statusLabel1) {
    this.statusLabel = statusLabel1;
  }

  /**
   * available statuses from screen (if not specified, any will be allowed). comma separated list
   * @return status
   */
  public Set<String> getStatusesFromUser() {
    return this.statusesFromUser;
  }

  /**
   * 
   * @return
   */
  public String getStatusAllFromUser() {
    return this.statusAllFromUser;
  }

  /**
   * 
   * @param statusAllFromUser1
   */
  public void setStatusAllFromUser(String statusAllFromUser1) {
    this.statusAllFromUser = statusAllFromUser1;
  }

  /**
   * if no status is specified, this will be used (e.g. for active only).  Note, the value should be of the
   * form the user would type in, e.g. status=active
   * @return status search default
   */
  public String getStatusSearchDefault() {
    return this.statusSearchDefault;
  }

  /**
   * if no status is specified, this will be used (e.g. for active only).  Note, the value should be of the
   * form the user would type in, e.g. status=active
   * @param statusSearchDefault1
   */
  public void setStatusSearchDefault(String statusSearchDefault1) {
    this.statusSearchDefault = statusSearchDefault1;
  }

  /**
   * translate between screen values of status, and the data store value.  Increment the 0 to 1, 2, etc for more translations.
   * so the user could enter: status=active, and that could translate to status_col=A.  The 'user' is what the user types in,
   * the 'datastore' is what is in the datastore.  The user part is not case-sensitive.  Note, this could be a many to one .
   * The key is what the user types in, the value is what goes to the database
   * 
   * @return the translation
   */
  public Map<String, String> getStatusTranslateUserToDatastore() {
    return this.statusTranslateUserToDatastore;
  }
  
}
