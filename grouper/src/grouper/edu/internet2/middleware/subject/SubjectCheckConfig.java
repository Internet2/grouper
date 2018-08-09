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
/*
 * @author mchyzer
 * $Id: SubjectCheckConfig.java,v 1.3 2009-03-22 02:49:27 mchyzer Exp $
 */
package edu.internet2.middleware.subject;

import java.net.URL;
import java.util.Collection;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * make sure the subject.xml config is correct
 */
public class SubjectCheckConfig {

  /** */
  public static final String GROUPER_TEST_SUBJECT_BY_ID = "grouperTestSubjectByIdOnStartupASDFGHJ";

  /** */
  public static final String SUBJECT_ID_TO_FIND_ON_CHECK_CONFIG = "subjectIdToFindOnCheckConfig";

  /** */
  public static final String FIND_SUBJECT_BY_ID_ON_CHECK_CONFIG = "findSubjectByIdOnCheckConfig";
  
  /** logger */
  private static Log log = LogFactory.getLog(SubjectCheckConfig.class);

  /**
   * make surce classpath file is there
   * @param resourceName
   * @return true if ok
   */
  public static boolean checkConfig(String resourceName) {
    boolean foundResource = false;
    //first, there must be a subject.properties
    try {
      //get the url of the navigation file
      ClassLoader cl = SubjectCheckConfig.class.getClassLoader();

      URL url = cl.getResource(resourceName);

      foundResource = url != null;
    } catch (Exception e) {
      //this means it cant be found
      log.info("Exception looking for " + resourceName, e);
    }
    if (!foundResource) {
      String error = "Cant find required resource on classpath: " + resourceName;
      //this is serious, lets go out and error
      System.err.println("Subject API error: " + error);
      log.error(error);
    }
    return foundResource;
  }
  
  /**
   * check the subject config
   */
  public static void checkConfig() {
    
    //at this point, we have a subject.properties...  now check it out
    Collection<Source> sources = null;
    
    try {
      sources = SourceManager.getInstance().getSources();
    } catch (Exception e) {
      String error = "problem initting sources from subject.properties";
      System.err.println("Subject API error: " + error + ", " + ExceptionUtils.getFullStackTrace(e));
      log.error(error, e);
      return;
    }
    int sourceCount = 0;
    for (Source source: sources) {
      
      sourceCount++;
      String error = "error with subject source id: " + source.getId() + ", name: " + source.getName()
        + ", ";
      try {
        source.checkConfig();
        
        String findSubjectOnCheckConfigString = source.getInitParam(FIND_SUBJECT_BY_ID_ON_CHECK_CONFIG);
        boolean findSubjectOnCheckConfig = SubjectUtils.booleanValue(findSubjectOnCheckConfigString, true);
        
        if (findSubjectOnCheckConfig) {
          String subjectToFindOnCheckConfig = source.getInitParam(SUBJECT_ID_TO_FIND_ON_CHECK_CONFIG);
          subjectToFindOnCheckConfig = SubjectUtils.defaultIfBlank(subjectToFindOnCheckConfig, GROUPER_TEST_SUBJECT_BY_ID);
          source.getSubject(subjectToFindOnCheckConfig, false);
        }

      } catch (Exception e) {
        String theError = error + "problem with getSubject by id, in subject.properties: search searchSubject: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }
      
      try {
        
        String findSubjectOnCheckConfigString = source.getInitParam("findSubjectByIdentifiedOnCheckConfig");
        boolean findSubjectOnCheckConfig = SubjectUtils.booleanValue(findSubjectOnCheckConfigString, true);
        
        if (findSubjectOnCheckConfig) {
          String subjectIdentifierToFindOnCheckConfig = source.getInitParam("subjectIdentifierToFindOnCheckConfig");
          subjectIdentifierToFindOnCheckConfig = SubjectUtils.defaultIfBlank(subjectIdentifierToFindOnCheckConfig, "grouperTestSubjectByIdentifierOnStartupASDFGHJ");
          source.getSubjectByIdentifier(subjectIdentifierToFindOnCheckConfig, false);
        }

      } catch (SubjectNotFoundException snfe) {
        //good!
      } catch (Exception e) {
        String theError = error + "problem with getSubject by identifier, in subject.properties: serachType searchSubjectByIdentifier: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }
    
      try {
        
        String findSubjectOnCheckConfigString = source.getInitParam("findSubjectByStringOnCheckConfig");
        boolean findSubjectOnCheckConfig = SubjectUtils.booleanValue(findSubjectOnCheckConfigString, true);
        
        if (findSubjectOnCheckConfig) {
          String stringToFindOnCheckConfig = source.getInitParam("stringToFindOnCheckConfig");
          stringToFindOnCheckConfig = SubjectUtils.defaultIfBlank(stringToFindOnCheckConfig, "grouperTestStringOnStartupASDFGHJ");
          source.search(stringToFindOnCheckConfig);
        }
        
      } catch (Exception e) {
        String theError = error + "problem with search, in subject.properties: serachType search: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }
    }
    if (sourceCount == 0) {
      System.err.println("Subject API warning: there are no sources available from subject.properties");
      log.warn("there are no sources available from subject.properties");
    }

  }
  
}
