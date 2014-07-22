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
 * $Id: XmlImport.java,v 1.2 2008-10-30 22:32:27 isgwb Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;
import edu.internet2.middleware.grouper.xml.XmlUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * chaining object to import to the registry
 */
public class XmlImport {

  /** stem to import */
  private Stem stem = null;
  
  /** groups and stems file to import */
  private boolean updateList = false;
  
  /** user properties file */
  private File userProperties = null;
  
  /** grouper session to use */
  private GrouperSession grouperSession = null;
  
  /** if we should ignore internal attributes including uuids */
  private boolean ignoreInternal = false;
  
  /**
   * assign the grouper session to this importer
   * @param theGrouperSession
   * @return this for chaining
   */
  public XmlImport grouperSession(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
    return this;
  }
  
  /**
   * import a stem and chain
   * @param theStem
   * @return this object for chaining
   */
  public XmlImport stem(Stem theStem) {
    this.stem = theStem;
    return this;
  }
  
  /**
   * import only groups or stems listed in this file
   * @param isUpdateList
   * @return the collection
   */
  public XmlImport updateList(boolean isUpdateList) {
    this.updateList = isUpdateList;
    return this;
  }
  
  /**
   * set external properties file
   * @param theUserProperties
   * @return the properties file
   */
  public XmlImport userProperties(File theUserProperties) {
    this.userProperties = theUserProperties;
    return this;
  }
  
  /**
   * set external properties file
   * @param theUserProperties
   * @return the properties file
   */
  public XmlImport ignoreInternal(boolean isIgnoreInternal) {
    this.ignoreInternal = isIgnoreInternal;
    return this;
  }
  
  
  
  
  /**
   * import from file
   * @param file 
   */
  public void importFromFile(final File file) {
    String filePath = GrouperUtil.fileCanonicalPath(file);
    try {
      Document doc = XmlReader.getDocumentFromFile(filePath);
      importHelper(doc);
      System.out.println("Imported file: " + GrouperUtil.fileCanonicalPath(file));
    } catch (Exception e) {
      throw new RuntimeException("Error importing file: " + filePath, e);
    }
  }

  /**
   * import from string
   * @param string 
   */
  public void importFromString(String string) {
    try {
      Document doc = XmlReader.getDocumentFromString(string);
      importHelper(doc);
      System.out.println("Imported from string");
    } catch (Exception e) {
      throw new RuntimeException("Error importing string", e);
    }
  }

  /**
   * import from string
   * @param url 
   */
  public void importFromUrl(URL url) {
    try {
      Document doc = XmlReader.getDocumentFromURL(url);
      importHelper(doc);
      System.out.println("Imported from url: " + url);
    } catch (Exception e) {
      throw new RuntimeException("Error importing url: " + url, e);
    }
  }

  /**
   * 
   * @param doc
   */
  private void importHelper(final Document doc) {
    boolean startedRootSession = false;
    //dont assign if creating a new one
    GrouperSession theGrouperSession = this.grouperSession;
    try {
      if (theGrouperSession == null) {
        startedRootSession = true;
        Subject subject = SubjectFinder.findRootSubject();
        try {
          theGrouperSession = GrouperSession.start(subject);
        } catch (SessionException gse) {
          throw new RuntimeException("Problem starting session: " + gse.getMessage(), gse);
        }
      }
      GrouperSession.callbackGrouperSession(theGrouperSession, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession innerGrouperSession)
            throws GrouperSessionException {
          try {
            Properties properties = new Properties();
            if (XmlImport.this.userProperties != null) {
              String userPropertiesPath = GrouperUtil.fileCanonicalPath(XmlImport.this.userProperties);
              try {
                properties = XmlUtils.internal_getUserProperties(LOG, userPropertiesPath);
              } catch (IOException ioe) {
                throw new RuntimeException("Problem processing user properties: " + userPropertiesPath + ", " + ioe.getMessage(), ioe);
              }
            }

            XmlImporter importer  = new XmlImporter(innerGrouperSession, properties);
            importer.setIgnoreInternal(ignoreInternal);
            if (XmlImport.this.updateList) {
              if (XmlImport.this.stem != null) {
                throw new RuntimeException("Cannot pass stem with updateList as true");
              }
              importer.update(doc);
            } 
            else {
              if (XmlImport.this.stem == null) {
                importer.load(doc);
              } 
              else {
                importer.load(XmlImport.this.stem, doc);
              }
            } 
          } catch (GrouperException grouperException) {
            throw new RuntimeException(grouperException.getMessage(), grouperException);
          }
          return null;
        }
        
      });
    } finally {
      if (startedRootSession) {
        GrouperSession.stopQuietly(theGrouperSession);
      }
    }


  }
  
  /**
   * logger
   */
  private static final Log LOG = GrouperUtil.getLog(XmlImport.class);

}
