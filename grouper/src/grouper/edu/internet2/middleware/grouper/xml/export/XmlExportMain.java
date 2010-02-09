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

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportMain {

  
  
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
  public void writeAllTables(Writer writer, String fileName) {
    
    this.done = false;
    this.currentRecordIndex = 0;
    Thread thread = null;
    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    final SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
    try {

      final long totalRecordCount = XmlImportMain.dbCount();
      final long startTime = System.currentTimeMillis();
      
      XmlImportMain.logInfoAndPrintToScreen("Starting: " + GrouperUtil.formatNumberWithCommas(totalRecordCount) + " records in the DB to be exported");
      
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
      GrouperUtil.xmlAttribute(writer, "folderRoot", ":");
      GrouperUtil.xmlAttribute(writer, "members", "all");
      writer.write(">\n");

      XmlExportMember.exportMembers(writer, this);

      XmlExportStem.exportStems(writer, this);
      
      XmlExportGroup.exportGroups(writer, this);
      
      XmlExportGroupType.exportGroupTypes(writer, this);

      XmlExportField.exportFields(writer, this);

      XmlExportGroupTypeTuple.exportGroupTypeTuples(writer, this);

      XmlExportComposite.exportComposites(writer, this);

      XmlExportAttribute.exportAttributes(writer, this);

      XmlExportAttributeDef.exportAttributeDefs(writer, this);

      XmlExportMembership.exportMemberships(writer, this);

      XmlExportAttributeDefName.exportAttributeDefNames(writer, this);

      XmlExportRoleSet.exportRoleSets(writer, this);

      XmlExportAttributeAssignAction.exportAttributeAssignActions(writer, this);

      XmlExportAttributeAssignActionSet.exportAttributeAssignActionSets(writer, this);

      XmlExportAttributeAssign.exportAttributeAssigns(writer, this);

      XmlExportAttributeAssignValue.exportAttributeAssignValues(writer, this);

      XmlExportAttributeDefNameSet.exportAttributeDefNameSets(writer, this);

      XmlExportAttributeDefScope.exportAttributeDefScopes(writer, this);

      XmlExportAuditType.exportAuditTypes(writer, this);

      XmlExportAuditEntry.exportAuditEntries(writer, this);

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
  
}
