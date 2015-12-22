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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportMain {

  /** stem patterns to export (and all objects inside) */
  private Set<String> stems = new TreeSet<String>();
  
  /** object names to export */
  private Set<String> objectNames = new TreeSet<String>();
  
  /** attribute assign ids */
  private Set<String> attributeAssignIds;
  
  /** attribute assigns for second phase */
  private Map<String, AttributeAssign> attributeAssignsForSecondPhase;

  /** attribute assign values for second phase */
  private Map<String, AttributeAssignValue> attributeAssignValuesForSecondPhase;
  
  /**
   * add a stem pattern e.g. this:stem, that stem and all substems and objects will be exported
   * @param stem
   * @return this for chaining
   */
  public XmlExportMain addStem(String stem) {
    this.stems.add(stem);
    return this;
  }

  /**
   * add an object name to export e.g. this:stem or this:stem:group
   * @param objectName
   * @return this for chaining
   */
  public XmlExportMain addObjectName(String objectName) {
    this.objectNames.add(objectName);
    return this;
  }

  /**
   * get the object names filtering on
   * @return object names
   */
  public Set<String> getObjectNames() {
    return this.objectNames;
  }
  
  /**
   * stem patterns to filter on, e.g. a:b or a:%
   * @return stem patterns to filter on
   */
  public Set<String> getStems() {
    return this.stems;
  }
  
  /**
   * stem patterns to filter on, e.g. a:b or a:%.  This will return a:b:% or a:%:%
   * @return stem patterns to filter on
   */
  public Set<String> getStemNamePatterns() {
    Set<String> result = new HashSet<String>();
    for (String stem : this.stems) {
      result.add(stem + ":%");
    }
    return result;
  }
  
  /** if audits should be included */
  private boolean includeAudits = true;

  /**
   * include audits, default to true
   * @param theIncludeAudits
   */
  public void setIncludeAudits(boolean theIncludeAudits) {
    this.includeAudits = theIncludeAudits;
  }
  
  /**
   * if audits should be included
   * @return if audits should be included
   */
  public boolean isIncludeAudits() {
    return this.includeAudits;
  }
  
  /** if comments should be included for foreign keys, note, this slows down the export */
  private boolean includeComments;
  
  /**
   * if comments should be included for foreign keys, note, this slows down the export
   * @return the includeComments
   */
  public boolean isIncludeComments() {
    return this.includeComments;
  }

  
  /**
   * if comments should be included for foreign keys, note, this slows down the export
   * @param includeComments1 the includeComments to set
   */
  public void setIncludeComments(boolean includeComments1) {
    this.includeComments = includeComments1;
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    
    //test this with GSH
    /*
    addGroup("etc", "a", "a");
    addGroup("etc", "b", "b");
    addGroup("etc", "c", "c");
    addComposite("etc:a", CompositeType.INTERSECTION, "etc:b", "etc:c");
    typeAdd("test");
    typeAddAttr("test", "attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
    groupAddType("etc:b", "test");
    grouperSession = GrouperSession.startRootSession();
    groupB = GroupFinder.findByName(grouperSession, "etc:b");
    groupB.setAttribute("attr", "value");
    groupB.store();
    stem = StemFinder.findByName(grouperSession, "etc", true);
    studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    userSharerRole = stem.addChildRole("userSharer", "userSharer");
    userReceiverRole = stem.addChildRole("userReceiver", "userReceiver");
    userSharerRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRole);
    action = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
    action2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction2");
    action.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);

    studentsAttrDef.setAssignToGroup(true);
    studentsAttrDef.store();

    studentsAttrDef2 = stem.addChildAttributeDef("students2", AttributeDefType.attr);
    studentsAttrDef2.setAssignToGroupAssn(true);
    studentsAttrDef2.store();

    
    studentsAttrName = stem.addChildAttributeDefName(studentsAttrDef, "studentsName", "studentsName");
    studentsAttrName2 = stem.addChildAttributeDefName(studentsAttrDef2, "studentsName2", "studentsName2");

    attributeAssignResult = groupB.getAttributeDelegate().assignAttribute(studentsAttrName);
    attributeAssignResult.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrName2);

    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
    attributeAssignValue.setValueString("string");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
    */
    
    StringWriter stringWriter = new StringWriter();
    XmlExportMain xmlExportMain = new XmlExportMain();
    xmlExportMain.writeAllTables(stringWriter, "a string");
    System.out.println(stringWriter);
  }

  /** if we are done writing */
  private boolean done = false;

  /** record count is the progress */
  private long currentRecordIndex = 0;

  /**
   * 
   */
  public void incrementRecordCount() {
    this.currentRecordIndex++;
    
  }
  
  /**
   * 
   * @param file
   */
  public void writeAllTables(File file) {
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(file);
      
      writeAllTables(fileWriter, GrouperUtil.fileCanonicalPath(file));
    } catch (IOException ioe) {
      throw new RuntimeException("Problem writing to file: " + GrouperUtil.fileCanonicalPath(file), ioe);
    } finally {
      GrouperUtil.closeQuietly(fileWriter);
    }
  }

  /**
   * write the xml to a writer
   * @param writer
   * @param fileName for logging
   */
  public void writeAllTablesGsh(Writer writer, String fileName) {

    this.done = false;
    this.currentRecordIndex = 0;
    Thread thread = null;
    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    final SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
    try {

      final long totalRecordCount = XmlImportMain.dbCountGsh(this);
      final long startTime = System.currentTimeMillis();
      
      XmlImportMain.logInfoAndPrintToScreen("Starting: " + GrouperUtil.formatNumberWithCommas(totalRecordCount) + " records in the DB to be exported to GSH (not exact, might be less)");
      
      thread = new Thread(new Runnable() {
        
        public void run() {
          while (true) {
            //sleep for thirty seconds
            for (int i=0;i<30;i++) {
              if (XmlExportMain.this.done) {
                return;
              }
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {
                //nothing
              }
            }
            if (XmlExportMain.this.done) {
              return;
            }
            
            //give a status
            long now = System.currentTimeMillis();
            int percent = (int)Math.round(((double)XmlExportMain.this.currentRecordIndex*100D)/totalRecordCount);
            
            long endTime = startTime + (long)((now-startTime) * (100D / percent));
            
            XmlImportMain.logInfoAndPrintToScreen(format.format(new Date(now)) + ": completed "
                + GrouperUtil.formatNumberWithCommas(XmlExportMain.this.currentRecordIndex) + " of " 
                + GrouperUtil.formatNumberWithCommas(totalRecordCount) + " ("
                + percent + "%) estimated time done: " + estFormat.format(new Date(endTime)));
          }          
        }
      });
      
      thread.start();
      
      //note, cant use stax since you cant mix stax and non stax since it wont close elements
      writer.write("grouperSession = GrouperSession.startRootSession();\n");

      XmlExportStem.exportStemsGsh(writer, this);
      XmlExportGroup.exportGroupsGsh(writer, this);
      XmlExportComposite.exportCompositesGsh(writer, this);
      XmlExportAttributeDef.exportAttributeDefsGsh(writer, this);
      XmlExportRoleSet.exportRoleSetsGsh(writer, this);
      XmlExportAttributeAssignAction.exportAttributeAssignActionsGsh(writer, this);
      XmlExportAttributeAssignActionSet.exportAttributeAssignActionSetsGsh(writer, this);
      
      XmlExportMembership.exportMembershipsGsh(writer, this);

      XmlExportAttributeDefName.exportAttributeDefNamesGsh(writer, this);

      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      this.done = true;
      if (thread != null) {
        try {
          thread.join(2000);
        } catch (InterruptedException ie) {}
      }

    }
    XmlImportMain.logInfoAndPrintToScreen("DONE: " + format.format(new Date()) + ": exported "
        + GrouperUtil.formatNumberWithCommas(XmlExportMain.this.currentRecordIndex) + " records to: " + fileName);

  }
  
  /**
   * write the xml to a writer
   * @param writer
   * @param fileName for logging
   */
  public void writeAllTables(Writer writer, String fileName) {
    
    this.attributeAssignIds = new HashSet<String>();
    this.attributeAssignsForSecondPhase = new HashMap<String, AttributeAssign>();
    this.attributeAssignValuesForSecondPhase = new HashMap<String, AttributeAssignValue>();
    this.done = false;
    this.currentRecordIndex = 0;
    Thread thread = null;
    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    final SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
    try {

      final long totalRecordCount = XmlImportMain.dbCount(this);
      final long startTime = System.currentTimeMillis();
      
      XmlImportMain.logInfoAndPrintToScreen("Starting: " + GrouperUtil.formatNumberWithCommas(totalRecordCount) + " records in the DB to be exported (not exact, might be less)");
      
      thread = new Thread(new Runnable() {
        
        public void run() {
          while (true) {
            //sleep for thirty seconds
            for (int i=0;i<30;i++) {
              if (XmlExportMain.this.done) {
                return;
              }
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {
                //nothing
              }
            }
            if (XmlExportMain.this.done) {
              return;
            }
            
            //give a status
            long now = System.currentTimeMillis();
            int percent = (int)Math.round(((double)XmlExportMain.this.currentRecordIndex*100D)/totalRecordCount);
            
            long endTime = startTime + (long)((now-startTime) * (100D / percent));
            
            XmlImportMain.logInfoAndPrintToScreen(format.format(new Date(now)) + ": completed "
                + GrouperUtil.formatNumberWithCommas(XmlExportMain.this.currentRecordIndex) + " of " 
                + GrouperUtil.formatNumberWithCommas(totalRecordCount) + " ("
                + percent + "%) estimated time done: " + estFormat.format(new Date(endTime)));
          }          
        }
      });
      
      thread.start();
      
      //note, cant use stax since you cant mix stax and non stax since it wont close elements
      writer.write("<?xml version=\"1.0\" ?>\n<grouperExport");
      GrouperUtil.xmlAttribute(writer, "version", GrouperVersion.GROUPER_VERSION);
      
      if (!this.filterStemsOrObjects()) {
        GrouperUtil.xmlAttribute(writer, "folderRoot", ":");
        GrouperUtil.xmlAttribute(writer, "members", "all");
      } else {
        if (GrouperUtil.length(this.getStemNamePatterns()) > 0) {
          GrouperUtil.xmlAttribute(writer, "folders", StringUtils.join(this.getStemNamePatterns().iterator(), ", "));
        }
        if (GrouperUtil.length(this.getObjectNames()) > 0) {
          GrouperUtil.xmlAttribute(writer, "objects", StringUtils.join(this.getObjectNames().iterator(), ", "));
        }
        GrouperUtil.xmlAttribute(writer, "members", "allWithoutUnecessaryGroups");
      }
      writer.write(">\n");

      XmlExportMember.exportMembers(writer, this);

      XmlExportStem.exportStems(writer, this);
      
      XmlExportGroup.exportGroups(writer, this);
      
      XmlExportAttributeDef.exportAttributeDefs(writer, this);
      
      XmlExportAttributeDefName.exportAttributeDefNames(writer, this);

      XmlExportRoleSet.exportRoleSets(writer, this);

      XmlExportAttributeAssignAction.exportAttributeAssignActions(writer, this);

      XmlExportAttributeAssignActionSet.exportAttributeAssignActionSets(writer, this);

      XmlExportAttributeAssign.exportAttributeAssigns(writer, this);

      XmlExportAttributeAssignValue.exportAttributeAssignValues(writer, this);
      
      XmlExportField.exportFields(writer, this);

      XmlExportComposite.exportComposites(writer, this);

      XmlExportMembership.exportMemberships(writer, this);

      XmlExportAttributeAssign.exportAttributeAssignsSecondPhase(writer, this);

      XmlExportAttributeAssignValue.exportAttributeAssignValuesSecondPhase(writer, this);

      XmlExportAttributeDefNameSet.exportAttributeDefNameSets(writer, this);

      XmlExportAttributeDefScope.exportAttributeDefScopes(writer, this);
      
      if (this.isIncludeAudits()) {
        XmlExportAuditType.exportAuditTypes(writer, this);
  
        XmlExportAuditEntry.exportAuditEntries(writer, this);
      }
      
      writer.write("</grouperExport>\n");
      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      this.done = true;
      if (thread != null) {
        try {
          thread.join(2000);
        } catch (InterruptedException ie) {}
      }

    }
    XmlImportMain.logInfoAndPrintToScreen("DONE: " + format.format(new Date()) + ": exported "
        + GrouperUtil.formatNumberWithCommas(XmlExportMain.this.currentRecordIndex) + " records to: " + fileName);

  }

  
  /**
   * @return the attributeAssignIds
   */
  public Set<String> getAttributeAssignIds() {
    return this.attributeAssignIds;
  }
  
  /**
   * @return the attributeAssignsForSecondPhase
   */
  public Map<String, AttributeAssign> getAttributeAssignsForSecondPhase() {
    return this.attributeAssignsForSecondPhase;
  }
  
  /**
   * @return the attributeAssignValuesForSecondPhase
   */
  public Map<String, AttributeAssignValue> getAttributeAssignValuesForSecondPhase() {
    return this.attributeAssignValuesForSecondPhase;
  }

  /**
   * 
   * @return filter
   */
  public boolean filterStemsOrObjects() {
    return GrouperUtil.length(this.stems) > 0 || GrouperUtil.length(this.objectNames) > 0;
  }
  
  /**
   * 
   * @param queryBuilder
   * @param aliasName
   * @param fieldName
   * @param forStemsOnly true to export stems, false to do other objects
   */
  public void appendHqlStemLikeOrObjectEquals(StringBuilder queryBuilder, String aliasName, String fieldName, boolean forStemsOnly) {
    String[] stemNamePatternArray = GrouperUtil.toArray(this.getStemNamePatterns(), String.class);
    String[] stemNameArray = GrouperUtil.toArray(this.getStems(), String.class);
    
    for (int i=0;i<GrouperUtil.length(stemNamePatternArray);i++) {
      
      if (i != 0) {
        queryBuilder.append(" or ");
      }
      
      queryBuilder.append(" ").append(aliasName).append(".")
        .append(fieldName).append(" like '")
        .append(HibUtils.escapeSqlString(stemNamePatternArray[i])).append("' ");

      if (forStemsOnly) {

        queryBuilder.append(" or ").append(aliasName).append(".")
          .append(fieldName).append(" = '")
          .append(HibUtils.escapeSqlString(stemNameArray[i])).append("' ");
        
      }
      
    }

    String[] objectNameArray = GrouperUtil.toArray(this.getObjectNames(), String.class);
    
    for (int i=0;i<GrouperUtil.length(objectNameArray);i++) {
      
      //we need an or if not on first one or if there were stem names beforehand
      if (i != 0 || GrouperUtil.length(stemNamePatternArray) > 0) {
        queryBuilder.append(" or ");
      }
      
      queryBuilder.append(" ").append(aliasName).append(".")
        .append(fieldName).append(" = '")
        .append(HibUtils.escapeSqlString(objectNameArray[i])).append("' ");
      
    }

  }
  
}
