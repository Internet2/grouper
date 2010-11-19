/**
 * @author mchyzer
 * $Id: ImportSubjectWrapper.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * bean which is a subject, though a thin wrapper just for import purposes
 */
public class ImportSubjectWrapper implements Subject {

  /** sourceId */
  private String sourceId;
  
  /** subjectId */
  private String subjectId;
  
  /** this is the row of the file, row 1 is the header */
  private int row;
  
  /** keep row data for error message */
  private String[] rowData;
  
  /** wrapped subject */
  private Subject wrapped;
  
  /**
   * 
   * @return lazy loaded wrapped subject
   */
  public Subject wrappedSubject() {
    if (this.wrapped == null) {
      try {
        this.wrapped = SourceManager.getInstance()
          .getSource(this.sourceId).getSubject(this.subjectId, true);
      } catch (Exception e) {
        throw new RuntimeException("Problem with subject: " + this.sourceId + ", " + this.subjectId, e);
      }
    }
    return this.wrapped;
  }
  
  /**
   * this is the row of the file, row 1 is the header
   * @return the row
   */
  public int getRow() {
    return this.row;
  }

  /**
   * @param theRow
   * @param theSourceId
   * @param theSubjectId
   * @param subjectIdentifier
   * @param subjectIdOrIdentifier
   * @param theRowData 
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @throws SourceUnavailableException 
   * @throws Exception 
   */
  public ImportSubjectWrapper(int theRow, String theSourceId, String theSubjectId, 
      String subjectIdentifier, String subjectIdOrIdentifier, String[] theRowData) 
        throws SubjectNotFoundException, SubjectNotUniqueException, SourceUnavailableException,
        Exception {
    
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    this.row = theRow;
    this.rowData = theRowData;
    boolean hasSourceId = !StringUtils.isBlank(theSourceId);
    if (hasSourceId) {
      this.sourceId = theSourceId;
    }
    boolean hasSubjectId = !StringUtils.isBlank(theSubjectId);
    if (hasSubjectId) {
      this.subjectId = theSubjectId;
    }

    //if we have both, we are all good
    if (!hasSourceId || !hasSubjectId) {
    
      Subject subject = null;
      
      //if not, we have to do more
      if (!hasSourceId && hasSubjectId) {
        subject = SubjectFinder.findById(this.subjectId, true);
        this.sourceId = subject.getSource().getId();
        
      } else {
      
        //ok, we arent done, look at all columns
        boolean hasSubjectIdentifier = !StringUtils.isBlank(subjectIdentifier);
        boolean hasSubjectIdOrIdentifier = !StringUtils.isBlank(subjectIdOrIdentifier);
        
        if (!hasSubjectId && !hasSubjectIdentifier && !hasSubjectIdOrIdentifier) {
          throw new RuntimeException(simpleMembershipUpdateContainer.getText().getImportErrorNoId());
        }
        
        //ok, we have an id
        if (hasSourceId) {
          
          //try by identifier (e.g. loginid)
          if (hasSubjectIdentifier) {
            subject = SourceManager.getInstance().getSource(this.sourceId).getSubjectByIdentifier(subjectIdentifier, true);
          } else if (hasSubjectIdOrIdentifier) {
            //if not, first try by id, e.g. 12345
            try {
              subject = SourceManager.getInstance().getSource(this.sourceId).getSubject(subjectIdOrIdentifier, true);
            } catch (SubjectNotFoundException snfe) {
              //and if not found, then 
              subject = SourceManager.getInstance().getSource(this.sourceId).getSubjectByIdentifier(subjectIdOrIdentifier, true);
            }
            
          } else {
            throw new RuntimeException("Shouldnt get here");
          }
          
        } else {
          //we have no sourceId
          if (hasSourceId) {
            subject = SubjectFinder.findById(this.subjectId, true);
          } else if (hasSubjectIdentifier) {
            subject = SubjectFinder.findByIdentifier(subjectIdentifier, true);
          } else if (hasSubjectIdOrIdentifier) {
            subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, true);
          } else {
            throw new RuntimeException("Should not get here either");
          }
          
        }
        if (subject != null) {
          this.sourceId = subject.getSource().getId();
          this.subjectId = subject.getId();
        } else {
          throw new RuntimeException("Not sure why we are here... " + hasSourceId + ", " + hasSubjectId + ", " + hasSubjectIdentifier + ", " + hasSubjectIdOrIdentifier );
        }
      }
    }
    
    //filter out the require sources
    String requireSources = simpleMembershipUpdateContainer.configValue(
        "simpleMembershipUpdate.subjectSearchRequireSources", false);
    
    if (!StringUtils.isBlank(requireSources)) {
      Set<String> sourceIds = GrouperUtil.splitTrimToSet(requireSources, ",");
      if (!sourceIds.contains(this.sourceId)) {
        String sourceId2 = this.sourceId;
        String subjectId2 = this.subjectId;
        this.sourceId = null;
        this.subjectId = null;
        throw new RuntimeException("Source not allowed: " + sourceId2 + ", for subject: " + subjectId2);
      }
    }
    
  }
  
  /**
   * get the data for an error message
   * @return the row
   */
  public String errorLabelForError() {
    return errorLabelForRowStatic(this.row, this.rowData);
  }

  /**
   * get the data for an error message
   * @param row 
   * @param rowData 
   * @return the error label
   */
  public static String errorLabelForRowStatic(int row, String[] rowData) {
    return "row: " + row + ", " + GrouperUtil.join(rowData, ',');
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name) {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set<String> getAttributeValues(String name) {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription() {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    return this.subjectId;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    try {
      return SourceManager.getInstance().getSource(this.sourceId);
    } catch (Exception e) {
      //TODO take out this try/catch on upgrade to 1.5
      throw new RuntimeException(e);
    }
  }

  
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    //TODO make this illegal, it causes a lookup
    return this.wrappedSubject().getType();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String)
   */
  public String getAttributeValueSingleValued(String attributeName) {
    throw new RuntimeException("Dont call this method on an import subject");
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSourceId()
   */
  public String getSourceId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return null;
  }

  
}
