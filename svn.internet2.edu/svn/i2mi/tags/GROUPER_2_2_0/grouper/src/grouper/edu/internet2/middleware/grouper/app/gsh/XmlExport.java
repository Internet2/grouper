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
 * @author mchyzer
 * $Id: XmlExport.java,v 1.2 2008-10-30 22:32:27 isgwb Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlExporter;
import edu.internet2.middleware.grouper.xml.XmlUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * chaining object to export the registry
 */
public class XmlExport {

  /** group to export */
  private Group group = null;

  /** stem to export */
  private Stem stem = null;
  
  /** groups and stems to export */
  private Collection groupsAndStems = null;
  
  /** message that describes how this groups and stems came about */
  private String collectionMessage = null;
  
  /** user properties file */
  private File userProperties = null;
  
  /** if the export is relative */
  private boolean relative = false;
  
  /** if we should include parent */
  private boolean includeParent = false;
  
  /** if we should include export children only */
  private boolean childrenOnly = false;
  
  /** grouper session to use */
  private GrouperSession grouperSession = null;
  
  /**
   * assign the grouper session to this exporter
   * @param theGrouperSession
   * @return this for chaining
   */
  public XmlExport grouperSession(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
    return this;
  }
  
  /**
   * export a stem and chain
   * @param theStem
   * @return this object for chaining
   */
  public XmlExport stem(Stem theStem) {
    this.stem = theStem;
    return this;
  }
  
  /**
   * export a group, and chain
   * @param theGroup
   * @return this object for chaining
   */
  public XmlExport group(Group theGroup) {
    this.group = theGroup;
    return this;
  }

  /**
   * assign a collection message for a groups and strings export
   * @param theMessage
   * @return the collection message
   */
  public XmlExport collectionMessage(String theMessage) {
    this.collectionMessage = theMessage;
    return this;
  }
  
  /**
   * export this collection of groups and stems
   * @param theGroupsAndStems
   * @return the collection
   */
  public XmlExport groupsAndStems(Collection theGroupsAndStems) {
    this.groupsAndStems = theGroupsAndStems;
    return this;
  }
  
  /**
   * export this collection of groups and stems
   * @param theGroupsAndStems
   * @return the collection
   */
  public XmlExport groupsAndStems(Object[] theGroupsAndStems) {
    Set set = GrouperUtil.toSet(theGroupsAndStems);
    return groupsAndStems(set);
  }

  /**
   * set that this group or stem export is relative
   * @param isRelative
   * @return this for chaining
   */
  public XmlExport relative(boolean isRelative) {
    this.relative = isRelative;
    return this;
  }
  
  /**
   * for export of groups if we should include parent
   * @param isIncludeParent
   * @return if we should include parent
   */
  public XmlExport includeParent(boolean isIncludeParent) {
    this.includeParent = isIncludeParent;
    return this;
  }
  
  /**
   * for export of a stem if we should only export the children
   * and not the stem itself
   * @param isChildrenOnly
   * @return if we should only export children
   */
  public XmlExport childrenOnly(boolean isChildrenOnly) {
    this.childrenOnly = isChildrenOnly;
    return this;
  }
  
  /**
   * set external properties file
   * @param theUserProperties
   * @return the properties file
   */
  public XmlExport userProperties(File theUserProperties) {
    this.userProperties = theUserProperties;
    return this;
  }
  
  /**
   * export to string
   * @return the string of xml
   */
  public String exportToString() {
    Writer writer         = new StringWriter();
    this.exportHelper(writer);
    return writer.toString();

  }

  /**
   * export to string
   * @param file 
   */
  public void exportToFile(File file) {
    Writer writer = null;
    try {
      writer = new BufferedWriter( new FileWriter( file ) );
      this.exportHelper(writer);
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException("Error with export: " + e.getMessage(), e);
    } finally {
      GrouperUtil.closeQuietly(writer);
    }
    System.out.println("Exported " + file.length() 
        + " bytes to file: " + GrouperUtil.fileCanonicalPath(file));
  }

  /**
   * logger
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExport.class);

  /**
   * @param writer
   */
  private void exportHelper(Writer writer) {
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
      Properties properties = new Properties();
      if (this.userProperties != null) {
        String userPropertiesPath = GrouperUtil.fileCanonicalPath(this.userProperties);
        try {
          properties = XmlUtils.internal_getUserProperties(LOG, userPropertiesPath);
        } catch (IOException ioe) {
          throw new RuntimeException("Problem processing user properties: " + userPropertiesPath + ", " + ioe.getMessage(), ioe);
        }
      }
      XmlExporter     exporter  = new XmlExporter(theGrouperSession, properties);
      
      try {
        if (this.group == null && this.stem == null && this.groupsAndStems == null) {
          exporter.export(writer);
        } else if (this.group != null) {
          exporter.export(writer, group, relative, includeParent);
        } else if (this.stem != null) {
          if (this.includeParent) {
            throw new RuntimeException("Cannot include parent when exporting stems");
          }
          exporter.export(writer, this.stem, this.relative,this.childrenOnly);
        } else if (this.groupsAndStems != null) {
          exporter.export(writer, this.groupsAndStems, this.collectionMessage);
        }
      } catch (GrouperException ge) {
        throw new RuntimeException("Problem with export: " + ge.getMessage());
      }
    } finally {
      if (startedRootSession) {
        GrouperSession.stopQuietly(theGrouperSession);
      }
    }
  }
}
