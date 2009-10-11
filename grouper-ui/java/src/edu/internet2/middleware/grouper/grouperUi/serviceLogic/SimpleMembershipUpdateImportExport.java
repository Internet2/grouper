/**
 * @author mchyzer
 * $Id: SimpleMembershipUpdateImportExport.java,v 1.3 2009-10-11 07:32:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.ImportSubjectWrapper;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.grouperUi.util.HttpContentType;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * ajax methods for simple memebrship update import and export actions
 */
public class SimpleMembershipUpdateImportExport {

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleMembershipUpdateImportExport.class);
  /**
   * cols (tolower) which are cols which are not attributes
   */
  private static Set<String> nonAttributeCols = GrouperUtil.toSet(
      "subjectid", "entityid", "sourceid", "memberid", "name", "description", "screenlabel");

  /**
   * export all immediate subjects in csv format
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unchecked")
  public void exportAllCsv(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
    String groupName = null;
  
    String currentMemberUuid = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);
      groupName = group.getName();
      
      Set<Member> members = group.getImmediateMembers();
      
      HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse(); 
      
      String[] headers = GrouperUtil.splitTrim(TagUtils.mediaResourceString( 
          "simpleMembershipUpdate.exportAllSubjectFields"), ",");
      
      //note: isError is second to last col, error is the last column
      boolean[] isAttribute = new boolean[headers.length];
      int sortCol = 0;
      int sourceIdCol = -1;
      for (int i=0;i<headers.length;i++) {
        isAttribute[i] = !nonAttributeCols.contains(headers[i].toLowerCase());
        if (StringUtils.equalsIgnoreCase(headers[i], TagUtils.mediaResourceString(
            "simpleMembershipUpdate.exportAllSortField"))) {
          sortCol = i;
        } else if (StringUtils.equalsIgnoreCase("sourceId", headers[i])) {
          sourceIdCol = i;
        }
      }
      
      List<String[]> memberData = new ArrayList<String[]>(); 
      for (Member member : members) {
        // feed in your array (or convert your data to an array)
        String[] entries = exportAllStringArray(member, headers, isAttribute);
        memberData.add(entries);
      }      
  
      final int SOURCE_ID_COL = sourceIdCol;
      final int SORT_COL = sortCol;
      //sort
      Collections.sort(memberData, new Comparator() {
  
        /**
         * 
         * @param o1
         * @param o2
         * @return 1, -1, 0
         */
        public int compare(Object o1, Object o2) {
          String[] first = (String[])o1;
          String[] second = (String[])o2;
          if (SOURCE_ID_COL != -1 && !StringUtils.equals(first[SOURCE_ID_COL], second[SOURCE_ID_COL])) {
            return GuiUtils.compare(first[SOURCE_ID_COL], second[SOURCE_ID_COL], true);
          }
          return GuiUtils.compare(first[SORT_COL], second[SORT_COL], true);
        }
      });
      
      //say it is CSV
      response.setContentType(HttpContentType.TEXT_CSV.getContentType());
    
      String groupExtensionFileName = GuiGroup.getExportAllFileNameStatic(group);
      
      response.setHeader ("Content-Disposition", "inline;filename=\"" + groupExtensionFileName + "\"");
      
      //just write some stuff
      PrintWriter out = null;
    
      try {
        out = response.getWriter();
      } catch (Exception e) {
        throw new RuntimeException("Cant get response.getWriter: ", e);
      }
      
      CSVWriter writer = new CSVWriter(out);
      String[] headersNew = new String[headers.length+2];
      System.arraycopy(headers, 0, headersNew, 0, headers.length);
      headersNew[headers.length] = "success";
      headersNew[headers.length+1] = "errorMessage";
      writer.writeNext(headersNew);
      for (String[] entries: memberData) {
        // feed in your array (or convert your data to an array)
        writer.writeNext(entries);
      }      
      writer.close();
  
      throw new ControllerDone();
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error exporting all members from group: " + groupName 
          + ", " + currentMemberUuid + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * export all members
   * @param member
   * @param headers 
   * @param isAttribute which indexes are attributes
   * @return the stirng array for csv
   */
  String[] exportAllStringArray(Member member, String[] headers, boolean[] isAttribute) {
    String[] result = new String[headers.length+2];
    
    //lets see what we can get from the member
    for (int i=0;i<headers.length;i++) {
      String header = headers[i];
      if ("subjectId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectId();
      } else if ("entityId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectId();
      } else if ("sourceId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectSourceId();
      } else if ("memberId".equalsIgnoreCase(header)) {
        result[i] = member.getUuid();
      }
    }
    
    
    try {
      
      Subject subject = member.getSubject();
      
      //lets see what we can get from the subject
      for (int i=0;i<headers.length;i++) {
        String header = headers[i];
        if ("name".equalsIgnoreCase(header)) {
          result[i] = subject.getName();
        } else if ("description".equalsIgnoreCase(header)) {
          result[i] = subject.getDescription();
        } else if ("screenLabel".equalsIgnoreCase(header)) {
          result[i] = GuiUtils.convertSubjectToLabelConfigured(subject);
        } else if (isAttribute[i]) {
          result[i] = subject.getAttributeValue(header);
        }
      }
      
      result[headers.length] = "T";
    } catch (NoSessionException se) {
      throw se;
    } catch (Exception e) {
      result[headers.length] = "F";
      String error = "error with memberId: " + member.getUuid() + ", subjectId: " + member.getSubjectId()
        + ", " + ExceptionUtils.getFullStackTrace(e);
      LOG.error(error);
      result[headers.length + 1] = error;
    }
    return result;
  }

  /**
   * export all immediate subjects as subject ids
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unchecked")
  public void exportSubjectIdsCsv(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
    String groupName = null;
  
    String currentMemberUuid = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);
      groupName = group.getName();
      
      Set<Member> members = group.getImmediateMembers();
      
      HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse(); 
      
      List<String[]> memberData = new ArrayList<String[]>(); 
      for (Member member : members) {
        // feed in your array (or convert your data to an array)
        String[] entries = new String[]{member.getSubjectSourceId(), member.getSubjectId()};
        memberData.add(entries);
      }      
  
      //sort
      Collections.sort(memberData, new Comparator() {
  
        /**
         * 
         * @param o1
         * @param o2
         * @return 1, -1, 0
         */
        public int compare(Object o1, Object o2) {
          String[] first = (String[])o1;
          String[] second = (String[])o2;
          if (!StringUtils.equals(first[0], second[0])) {
            return GuiUtils.compare(first[0], second[0], true);
          }
          return GuiUtils.compare(first[1], second[1], true);
        }
      });
      
      //say it is CSV
      response.setContentType("text/csv");
    
      String groupExtensionFileName = GuiGroup.getExportSubjectIdsFileNameStatic(group);
      
      response.setHeader ("Content-Disposition", "inline;filename=\"" + groupExtensionFileName + "\"");
      
      //just write some stuff
      PrintWriter out = null;
    
      try {
        out = response.getWriter();
      } catch (Exception e) {
        throw new RuntimeException("Cant get response.getWriter: ", e);
      }
      
      CSVWriter writer = new CSVWriter(out);
      writer.writeNext(new String[]{"sourceId", "entityId"});
      for (String[] entries: memberData) {
        // feed in your array (or convert your data to an array)
        writer.writeNext(entries);
      }      
      writer.close();
            
  
      throw new ControllerDone();
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error exporting all members from group: " + groupName 
          + ", " + currentMemberUuid + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * import a CSV file
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unchecked")
  public void importCsv(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.setAddTextAreaTag(true);
    guiResponseJs.addAction(GuiScreenAction.newCloseModal());
  
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    String groupName = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);
      groupName = group.getName();
      
      List<Member> existingMembers = new ArrayList<Member>(GrouperUtil.nonNull(group.getImmediateMembers()));
      
      int existingCount = GrouperUtil.length(existingMembers);
  
      GrouperRequestWrapper grouperRequestWrapper = (GrouperRequestWrapper)httpServletRequest;
      
      FileItem importCsvFile = grouperRequestWrapper.getParameterFileItem("importCsvFile");
      
      String fileName = StringUtils.defaultString(importCsvFile == null ? "" : importCsvFile.getName());
  
      String fileSize = importCsvFile == null ? "" : (" : " + FileUtils.byteCountToDisplaySize(importCsvFile.getSize()));
      
      fileName += fileSize;
      
      //validate the inputs, file is required
      if (importCsvFile == null || importCsvFile.getSize() == 0 || 
          importCsvFile.getName() == null 
          || (!importCsvFile.getName().toLowerCase().endsWith(".csv")
            &&  !importCsvFile.getName().toLowerCase().endsWith(".txt"))) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert("<pre>" 
            + GuiUtils.message("simpleMembershipUpdate.importErrorNoWrongFile") + fileName + "</pre>"));
        return;
      }
      
      boolean importReplaceMembers = grouperRequestWrapper.getParameterBoolean("importReplaceMembers", false);
      
      //convert the import file to subjects
      List<String> subjectErrors = new ArrayList<String>();
      List<Subject> importedSubjectWrappers = parseCsvImportFile(
          importCsvFile, fileName, subjectErrors);
  
      GuiUtils.removeOverlappingSubjects(existingMembers, importedSubjectWrappers);
      
      int addedCount = 0;
      List<String> addErrors = new ArrayList<String>();
      
      //first lets add some members
      for (int i=0;i<importedSubjectWrappers.size();i++) {
        
        ImportSubjectWrapper importedSubjectWrapper = (ImportSubjectWrapper)importedSubjectWrappers.get(i);
        try {
          group.addMember(importedSubjectWrapper, false);
          addedCount++;
        } catch (Exception e) {
          String error = "Error adding subject from " + importedSubjectWrapper.errorLabelForError() + ", " + e.getMessage();
          LOG.warn(error, e);
          addErrors.add(error);
        }
  
      }
  
      boolean didntImportDueToSubjects = false;
      int deletedCount = 0;
      List<String> deleteErrors = new ArrayList<String>();
  
      //remove the ones which are already there
      if (importReplaceMembers) {
        
        if (GrouperUtil.length(subjectErrors) == 0) {
          
          for (Member existingMember : existingMembers) {
            
            try {
              group.deleteMember(existingMember, false);
              deletedCount++;
            } catch (Exception e) {
              String error = "Error deleting subject " + SubjectHelper.getPretty(existingMember) + e.getMessage();
              LOG.warn(error, e);
              deleteErrors.add(error);
            }
          }
        } else {
          didntImportDueToSubjects = true;
        }
        
        
      }
      
      //this might be a little wasteful, but I think it is a good sanity check
      int newSize = group.getImmediateMembers().size();
      
      StringBuilder result = new StringBuilder();
      //result.append("File: " + importCsvFile.getName() + ", " + importCsvFile.getFieldName() 
      //    + ", isFormField: " + importCsvFile.isFormField() + ", inMemory: " 
      //    + importCsvFile.isInMemory() + "\n\n" + importCsvFile.getString() + "\n\n");
      //result.append("importReplaceMembers: " + importReplaceMembers + "\n\n");
      
      //first of all, was it successful?
      int errorSize = GrouperUtil.length(subjectErrors) + GrouperUtil.length(addErrors) 
        + GrouperUtil.length(deleteErrors);
      boolean hasError = errorSize > 0;
      if (!hasError) {
        
        result.append("<b>").append(GuiUtils.message("simpleMembershipUpdate.importSuccessSummary")).append("</b><br /><br />\n");
        
      } else {
        
        result.append("<b>").append(GuiUtils.message("simpleMembershipUpdate.importErrorSummary", 
            false, false, Integer.toString(errorSize))).append("</b><br /><br />\n");
        
      }
      
      //give general summary
      result.append(GuiUtils.message("simpleMembershipUpdate.importSizeSummary", false, false, 
          Integer.toString(existingCount), Integer.toString(newSize))).append("<br />\n");
  
      if (didntImportDueToSubjects) {
        result.append(GuiUtils.message("simpleMembershipUpdate.importErrorSubjectProblems", false, false, 
            Integer.toString(existingCount), Integer.toString(newSize))).append("<br />\n");
      }
      
      //adds, deletes
      result.append(GuiUtils.message("simpleMembershipUpdate.importAddsDeletesSummary", false, 
          false, Integer.toString(addedCount), Integer.toString(deletedCount))).append("<br />\n");
      
      if (GrouperUtil.length(subjectErrors) > 0) {
        result.append("<br /><b>").append(GuiUtils.message("simpleMembershipUpdate.importSubjectErrorsLabel"))
            .append("</b><br />\n");
        for (String error: subjectErrors) {
          result.append(error).append("<br />\n");
        }
      }
      
      if (GrouperUtil.length(addErrors) > 0) {
        result.append("<br /><b>").append(GuiUtils.message("simpleMembershipUpdate.importAddErrorsLabel"))
            .append("</b><br />\n");
        for (String error: addErrors) {
          result.append(error).append("<br />\n");
        }
      }
  
      if (GrouperUtil.length(deleteErrors) > 0) {
        result.append("<br /><b>").append(GuiUtils.message("simpleMembershipUpdate.importRemoveErrorsLabel"))
          .append("</b><br />\n");
        for (String error: deleteErrors) {
          result.append(error).append("<br />\n");
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(result.toString(), false));
  
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error importing members to group: " + groupName 
          + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //refresh list... since it probably changed
    new SimpleMembershipUpdate().retrieveMembers(httpServletRequest, httpServletResponse);
    
  }

  /**
   * @param importCsvFile
   * @param fileName
   * @param subjectErrors pass in a list and errors will be put in here
   * @return the list, never null
   */
  @SuppressWarnings("unchecked")
  List<Subject> parseCsvImportFile(FileItem importCsvFile, String fileName, List<String> subjectErrors) {
    
    //convert from CSV to 
    CSVReader reader = null;
    
    //note, the first row is the title
    List<String[]> csvEntries = null;
  
    try {
      reader = new CSVReader(new InputStreamReader(importCsvFile.getInputStream()));
      csvEntries = reader.readAll();
    } catch (IOException ioe) {
      throw new RuntimeException("Error processing file: " + fileName, ioe);
    }
    
    List<Subject> uploadedSubjects = new ArrayList<Subject>();
    
    //lets get the headers
    int sourceIdColumn = -1;
    int subjectIdColumn = -1;
    int subjectIdentifierColumn = -1;
    int subjectIdOrIdentifierColumn = -1;
    
    //must have lines
    if (GrouperUtil.length(csvEntries) <= 1) {
      throw new RuntimeException(GuiUtils.message("simpleMembershipUpdate.importErrorNoWrongFile"));
    }
    
    //lets go through the headers
    String[] headers = csvEntries.get(0);
    int headerSize = headers.length;
    for (int i=0;i<headerSize;i++) {
      if ("sourceId".equalsIgnoreCase(headers[i])) {
        sourceIdColumn = i;
      }
      if ("subjectId".equalsIgnoreCase(headers[i]) || "entityId".equalsIgnoreCase(headers[i])) {
        subjectIdColumn = i;
      }
      if ("subjectIdentifier".equalsIgnoreCase(headers[i]) || "entityIdentifier".equalsIgnoreCase(headers[i])) {
        subjectIdentifierColumn = i;
      }
      if ("subjectIdOrIdentifier".equalsIgnoreCase(headers[i]) || "entityIdOrIdentifier".equalsIgnoreCase(headers[i])) {
        subjectIdOrIdentifierColumn = i;
      }
    }
    
    //must pass in an id
    if (subjectIdColumn == -1 && subjectIdentifierColumn == -1 && subjectIdOrIdentifierColumn == -1) {
      throw new RuntimeException(GuiUtils.message("simpleMembershipUpdate.importErrorNoIdCol"));
    }
    
    //ok, lets go through the rows, start after the headers
    for (int i=1;i<csvEntries.size();i++) {
      String[] csvEntry = csvEntries.get(i);
      int row = i+1;
      
      //try catch each one and see where we get
      try {
        String sourceId = null;
        String subjectId = null;
        String subjectIdentifier = null;
        String subjectIdOrIdentifier = null;
  
        sourceId = sourceIdColumn == -1 ? null : csvEntry[sourceIdColumn]; 
        subjectId = subjectIdColumn == -1 ? null : csvEntry[subjectIdColumn]; 
        subjectIdentifier = subjectIdentifierColumn == -1 ? null : csvEntry[subjectIdentifierColumn]; 
        subjectIdOrIdentifier = subjectIdOrIdentifierColumn == -1 ? null : csvEntry[subjectIdOrIdentifierColumn]; 
        
        ImportSubjectWrapper importSubjectWrapper = 
          new ImportSubjectWrapper(row, sourceId, subjectId, subjectIdentifier, subjectIdOrIdentifier, csvEntry);
        uploadedSubjects.add(importSubjectWrapper);
        
      } catch (Exception e) {
        LOG.info(e);
        subjectErrors.add("Error on " + ImportSubjectWrapper.errorLabelForRowStatic(row, csvEntry) + ": " +    e.getMessage());
      }
    
    }
    
    return uploadedSubjects;
    
    
    
  }

}
