/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.subject.provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.SubjectUtils;

/**
 * process a subject status
 * 
 * @author mchyzer
 */
public class SubjectStatusProcessor {

  /**
   * append a string to a stringbuilder and a space in between if necessary
   * @param a builder to append to
   * @param b string to add
   */
  public static void appendWithSpace(StringBuilder a, String b) {
    if (StringUtils.isBlank(b)) {
      return;
    }
    
    if (StringUtils.isBlank(a.toString())) {
      a.setLength(0);
      
    } else {
      a.append(" ");
    }
    
    a.append(StringUtils.trimToEmpty(b));
    
  }

  /**
   * after the status part is stripped out
   */
  private String strippedQuery;

  /**
   * original query from user
   */
  private String originalQuery;

  /**
   * status value that the user is querying on
   */
  private String statusValueFromUser;

  /**
   * if the user wants equals or notEquals
   */
  private boolean equalsFromUser;

  /**
   * after the status part is stripped out
   * @return stripped query
   */
  public String getStrippedQuery() {
    return this.strippedQuery;
  }

  /**
   * status value that the user is querying on
   * @return status
   */
  public String getStatusValueFromUser() {
    return this.statusValueFromUser;
  }

  /**
   * if the user wants equals or notEquals
   * @return is equals
   */
  public boolean isEqualsFromUser() {
    return this.equalsFromUser;
  }

  /**
   * original query from user
   * @param originalQuery1
   */
  public void setOriginalQuery(String originalQuery1) {
    this.originalQuery = originalQuery1;
  }
  
  /**
   * config of the status
   */
  private SubjectStatusConfig subjectStatusConfig;
  
  /**
   * 
   * @param subjectStatusConfig1
   */
  public void setSubjectStatusConfig(SubjectStatusConfig subjectStatusConfig1) {
    this.subjectStatusConfig = subjectStatusConfig1;
  }

  /**
   * construct with query and the config
   * @param originalQuery
   * @param subjectStatusConfig
   */
  public SubjectStatusProcessor(String originalQuery,
      SubjectStatusConfig subjectStatusConfig) {
    super();
    this.originalQuery = originalQuery;
    this.subjectStatusConfig = subjectStatusConfig;
  }

  /**
   * pattern for something=something or something!=something
   */
  private static Pattern statusPattern = Pattern.compile("([a-zA-Z0-9_\\-]+)\\s*(<>|=|!=)\\s*([a-zA-Z0-9_\\-]+)");
  
  /**
   * if the originalQuery didnt have status info, and there was a default, then put that here
   */
  private String queryWithDefault = null;

  /**
   * 
   */
  private static Log log = LogFactory.getLog(SubjectStatusProcessor.class);
  
  
  
  /**
   * if the originalQuery didnt have status info, and there was a default, then put that here
   * @return originalQuery
   */
  public String getQueryWithDefault() {
    return this.queryWithDefault;
  }

  /**
   * process the search string.  set the fields in this object or status value,
   * isequals, and stripped query
   * @param searchString
   */
  public void processOriginalQuery() {
    
    //should only need to go twice
    this.processOriginalQueryHelper(true);
    
  }


  
  public String getOriginalQuery() {
    return originalQuery;
  }

  /**
   * process the search string.  set the fields in this object or status value,
   * isequals, and stripped query
   * @param firstPass true if this is the first pass through the process
   */
  private void processOriginalQueryHelper(boolean firstPass) {

    //operate on the stripped query, since it will be evolving
    String queryToOperateOn = firstPass ? StringUtils.defaultString(this.originalQuery) : StringUtils.defaultString(this.queryWithDefault);
    
    boolean hasStatus = this.subjectStatusConfig.isStatusConfigured();
    
    if (hasStatus && StringUtils.isBlank(this.subjectStatusConfig.getStatusLabel())) {
      throw new RuntimeException("Cant find status label for source");
    }
    
    boolean foundStatus = false;
    
    for (Pattern pattern : new Pattern[]{statusPattern}) {
       
      //lets look for a status part
      Matcher matcher = pattern.matcher(queryToOperateOn);
      
      StringBuilder queryToOperateOnBuilder = new StringBuilder();
      
      //current index of query
      int currentIndex = 0;
      
      while (matcher.find(currentIndex)) {
        
        String operation = matcher.group(1).toLowerCase();
        String equalsOperator = matcher.group(2);
        String value = matcher.group(3);
        
        boolean valueValid = true;
        
        if (this.subjectStatusConfig.getStatusesFromUser().size() > 0) {
          valueValid = this.subjectStatusConfig.getStatusesFromUser().contains(value.toLowerCase());
        }
        
        //if this is a match for this operator
        if (StringUtils.equalsIgnoreCase(operation, this.subjectStatusConfig.getStatusLabel()) && valueValid) {
          
          //just use the first status
          if (!foundStatus) {
            
            this.equalsFromUser = StringUtils.equals("=", equalsOperator);
            this.statusValueFromUser = value;
            foundStatus = true;
            
          }
          
          //strip it out
          appendWithSpace(queryToOperateOnBuilder, queryToOperateOn.substring(currentIndex, matcher.start()));
          
        } else {
          //see if it is a status label in another source
          if (SourceManager.getInstance().getSourceManagerStatusBean().getStatusLabels().contains(operation.toLowerCase())) {

            //strip it out since it is applicable to another source... right?  We want it stripped out??? 
            appendWithSpace(queryToOperateOnBuilder, queryToOperateOn.substring(currentIndex, matcher.start()));

          } else {

            //leave it in, wasnt a status call
            appendWithSpace(queryToOperateOnBuilder, queryToOperateOn.substring(currentIndex, matcher.end()));
            
          }
        }
        
        currentIndex = matcher.end();
      }
      
      appendWithSpace(queryToOperateOnBuilder, queryToOperateOn.substring(currentIndex, queryToOperateOn.length()));
      
      queryToOperateOn = queryToOperateOnBuilder.toString();
      
    }

    if (firstPass && !foundStatus && hasStatus && !StringUtils.isBlank(this.subjectStatusConfig.getStatusSearchDefault())) {
      
      this.queryWithDefault = this.originalQuery + " " + this.subjectStatusConfig.getStatusSearchDefault();
      this.processOriginalQueryHelper(false);
      return;
      
    }
    
    if (firstPass) {
      
      this.queryWithDefault = this.originalQuery;
    
    }
    
    this.strippedQuery = queryToOperateOn;

  }
  
  /**
   * take in a search string, and return the search status result
   * @param searchString
   * @return the search status result
   */
  public SubjectStatusResult processSearch() {
    
    SubjectStatusResult subjectStatusResult = new SubjectStatusResult();

    //this should only be null if things arent configured properly...
    if (this.subjectStatusConfig == null) {
      log.info("Why is config null???");
      subjectStatusResult.setAll(true);
      subjectStatusResult.setStrippedQuery(this.originalQuery);
      return subjectStatusResult;
    }
        
    this.processOriginalQuery();

    subjectStatusResult.setEquals(this.equalsFromUser);
    subjectStatusResult.setStrippedQuery(this.strippedQuery);

    if (!StringUtils.isBlank(this.statusValueFromUser)) {

      //see if all
      if (StringUtils.equalsIgnoreCase(this.statusValueFromUser, this.subjectStatusConfig.getStatusAllFromUser())) {
        subjectStatusResult.setAll(true);
      }
      
      subjectStatusResult.setDatastoreFieldName(this.subjectStatusConfig.getStatusDatastoreFieldName());
     
      String translatedStatus = this.subjectStatusConfig.getStatusTranslateUserToDatastore().get(this.statusValueFromUser);
      
      //default to what was typed in
      translatedStatus = StringUtils.defaultString(translatedStatus, this.statusValueFromUser);
      
      subjectStatusResult.setDatastoreValue(translatedStatus);
      
    }
    
    return subjectStatusResult;
  }

}
