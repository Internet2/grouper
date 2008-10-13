/*
 * @author mchyzer
 * $Id: SubjectCheckConfig.java,v 1.2 2008-10-13 09:10:28 mchyzer Exp $
 */
package edu.internet2.middleware.subject;

import java.io.File;
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
  
  /** logger */
  private static Log log = LogFactory.getLog(SubjectCheckConfig.class);

  /**
   * make surce classpath file is there
   * @param resourceName
   * @return true if ok
   */
  public static boolean checkConfig(String resourceName) {
    boolean foundResource = false;
    //first, there must be a sources.xml
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
    
    if (!checkConfig("sources.xml")) {
      return;
    }
    
    //at this point, we have a sources.xml...  now check it out
    Collection<Source> sources = null;
    
    try {
      sources = SourceManager.getInstance().getSources();
    } catch (Exception e) {
      String error = "problem initting sources from sources.xml";
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
        
        source.getSubject("qwqertyuiopsdfasdsdf");

      } catch (SubjectNotFoundException snfe) {
        //good!
      } catch (Exception e) {
        String theError = error + "problem with getSubject by id, in sources.xml: serachType searchSubject: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }

      try {
        
        source.getSubjectByIdentifier("qwqertyuiopsadfsadfsdf");
      } catch (SubjectNotFoundException snfe) {
        //good!
      } catch (Exception e) {
        String theError = error + "problem with getSubject by identifier, in sources.xml: serachType searchSubjectByIdentifier: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }
    
      try {
        
        source.search("qwqertyuiop");
        
      } catch (Exception e) {
        String theError = error + "problem with search, in sources.xml: serachType search: ";
        System.err.println("Subject API error: " + theError + ", " + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
        continue;
      }
    }
    if (sourceCount == 0) {
      System.err.println("Subject API warning: there are no sources available from sources.xml");
      log.warn("there are no sources available from sources.xml");
    }

  }
  
}
