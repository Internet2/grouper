package edu.internet2.middleware.grouper.app.gsh.template;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.file.GrouperFileDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GshTemplateOutput {
  
  public String toString() {
    
    StringBuilder result = new StringBuilder();

    if (GrouperUtil.length(this.validationLines) > 0) {
      for (GshValidationLine gshValidationLine : this.validationLines) {
        result.append(gshValidationLine.toString()).append("\n");
      }
    }
    
    if (GrouperUtil.length(this.outputLines) > 0) {
      for (GshOutputLine gshOutputLine : this.outputLines) {
        result.append(gshOutputLine.toString()).append("\n");
      }
    }
    
    if (this.wsOutput != null) {
      result.append("wsOutput: ").append(GrouperUtil.jsonConvertTo(this.wsOutput, false)).append("\n");
    }
    
    if (this.downloadGrouperFileId != null) {
      result.append("downloadGrouperFileId: ").append(this.downloadGrouperFileId).append("\n");
    }
    
    return result.toString();
  }
  
  private boolean isError;
  
  private static ThreadLocal<WeakReference<GshTemplateOutput>> threadLocalGshTemplateOutput = new InheritableThreadLocal<>();
  
  private List<GshOutputLine> outputLines = new ArrayList<GshOutputLine>();
  
  private List<GshValidationLine> validationLines = new ArrayList<GshValidationLine>();
  
  private String abacScript;
  
  //defaults to false
  private Boolean abacIncludeInternalSubjectSources;
  
  
  /**
   * operation to redirect to from grouper, e.g. operation=UiV2Stem.viewStem&stemId=abc123
   */
  private String redirectToGrouperOperation;
  
  /**
   * operation to redirect to from grouper, e.g. operation=UiV2Stem.viewStem&stemId=abc123
   * If the String is NONE, then dont redirect anywhere
   * @return the redirect
   */
  public String getRedirectToGrouperOperation() {
    return redirectToGrouperOperation;
  }

  /**
   * operation to redirect to from grouper, e.g. operation=UiV2Stem.viewStem&stemId=abc123
   * If the String is NONE, then dont redirect anywhere
   * @param redirectToGrouperOperation
   */
  public GshTemplateOutput assignRedirectToGrouperOperation(String redirectToGrouperOperation) {
    this.redirectToGrouperOperation = redirectToGrouperOperation;
    return this;
  }
  
  public GshTemplateOutput assignAbacScript(String abacScript) {
    this.abacScript = abacScript;
    return this;
  }
  
  public GshTemplateOutput assignAbacIncludeInternalSubjectSources(boolean abacIncludeInternalSubjectSources) {
    this.abacIncludeInternalSubjectSources = abacIncludeInternalSubjectSources;
    return this;
  }

  public boolean isError() {
    return isError;
  }
  
  public List<GshOutputLine> getOutputLines() {
    return outputLines;
  }

  public GshTemplateOutput assignIsError(boolean isError) {
    this.isError = isError;
    return this;
  }
  
  public GshTemplateOutput addOutputLine(GshOutputLine outputLine) {
    outputLines.add(outputLine);
    return this;
  }
  
  public GshTemplateOutput addOutputLine(String outputLine) {
    outputLines.add(new GshOutputLine(outputLine));
    return this;
  }
  
  /**
   * 
   * @param messageType success (default), info, error
   * @param outputLine
   * @return
   */
  public GshTemplateOutput addOutputLine(String messageType, String outputLine) {
    outputLines.add(new GshOutputLine(messageType, outputLine));
    return this;
  }
  
  public GshTemplateOutput addValidationLine(GshValidationLine validationLine) {
    validationLines.add(validationLine);
    return this;
  }
  
  public GshTemplateOutput addValidationLine(String validationLine) {
    validationLines.add(new GshValidationLine(validationLine));
    return this;
  }
  
  public GshTemplateOutput addValidationLine(String inputName, String validationLine) {
    validationLines.add(new GshValidationLine(inputName, validationLine));
    return this;
  }

  /**
   * set a map or javabean
   */
  private Object wsOutput;
  
  /**
   * set a map or javabean
   * @return
   */
  public Object getWsOutput() {
    return wsOutput;
  }
  
  /**
   * set a map or javabean
   * @param wsOutput
   */
  public void setWsOutput(Object wsOutput) {
    this.wsOutput = wsOutput;
  }

  public List<GshValidationLine> getValidationLines() {
    return validationLines;
  }
  
  
  public String getAbacScript() {
    return abacScript;
  }

  
  public Boolean getAbacIncludeInternalSubjectSources() {
    return abacIncludeInternalSubjectSources;
  }

  public static GshTemplateOutput retrieveGshTemplateOutput() {
    WeakReference<GshTemplateOutput> weakReference = threadLocalGshTemplateOutput.get();
    return weakReference == null ? null : weakReference.get();
  }
  
  
  public static void assignThreadLocalGshTemplateOutput(GshTemplateOutput gshTemplateOutput) {
    threadLocalGshTemplateOutput.set(new WeakReference(gshTemplateOutput));
  }
  
  public static void removeThreadLocalGshTemplateOutput() {
    threadLocalGshTemplateOutput.remove();
  }
  
  public GshTemplateOutput assignGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
    return this;
  }

  GrouperProvisioner grouperProvisioner;
  
  public GrouperProvisioner retrieveGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  /**
   * GRP-7438: config id of the template that is running, set by GshTemplateExec.  Used to
   * namespace download files
   */
  private String templateConfigId;
  
  /**
   * GRP-7438: config id of the template that is running
   * @param templateConfigId
   * @return this for chaining
   */
  public GshTemplateOutput assignTemplateConfigId(String templateConfigId) {
    this.templateConfigId = templateConfigId;
    return this;
  }
  
  /**
   * GRP-7438: config id of the template that is running.  If not assigned, get it from the
   * thread local template runtime
   * @return the config id or null if not known
   */
  public String getTemplateConfigId() {
    if (StringUtils.isBlank(this.templateConfigId)) {
      GshTemplateRuntime gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime();
      if (gshTemplateRuntime != null) {
        return gshTemplateRuntime.getTemplateConfigId();
      }
    }
    return this.templateConfigId;
  }
  
  /**
   * @return the template config id, or exception if not known
   */
  private String retrieveTemplateConfigIdRequired() {
    String theTemplateConfigId = this.getTemplateConfigId();
    if (StringUtils.isBlank(theTemplateConfigId)) {
      throw new RuntimeException("Template config id is not known, cannot use download files");
    }
    return theTemplateConfigId;
  }
  
  /**
   * GRP-7438: grouper_file id the user should be able to download after the template runs, or null
   */
  private String downloadGrouperFileId;
  
  /**
   * GRP-7438: grouper_file id the user should be able to download after the template runs
   * @return the id or null if no download
   */
  public String getDownloadGrouperFileId() {
    return this.downloadGrouperFileId;
  }
  
  /**
   * GRP-7438: let the user download an existing grouper_file row (any system name).  The row is not
   * copied and is never deleted by the download cleanup.  The template is responsible for only
   * handing back files the user is allowed to see.
   * @param grouperFileId id of the grouper_file row, or null to not download anything
   * @return this for chaining
   */
  public GshTemplateOutput assignDownloadGrouperFileId(String grouperFileId) {
    // fail now in the template rather than later in the download.  Only select the file name so
    // the contents are not loaded
    if (grouperFileId != null && GrouperFileDao.findFileNameById(grouperFileId) == null) {
      throw new RuntimeException("Cant find grouper file by id: " + grouperFileId);
    }
    this.downloadGrouperFileId = grouperFileId;
    return this;
  }
  
  /**
   * GRP-7438: find the id of the file this template saved for a date, e.g. to see if today's report
   * is already computed.  If found, pass it to assignDownloadGrouperFileId().  Does not load the contents
   * @param date yyyy-MM-dd
   * @param fileName e.g. myReport_2026-10-04.csv
   * @return the grouper_file id or null if not there
   */
  public String retrieveDownloadFileId(String date, String fileName) {
    return GshTemplateDownloadFile.findIdByDate(this.retrieveTemplateConfigIdRequired(), date, fileName);
  }
  
  /**
   * GRP-7438: save text contents to grouper_file (unencrypted) for this template and date, and let the
   * user download it.  Replaces the contents if the file for this date and name is already there.
   * Not deleted automatically: the template should call deleteExpiredDownloadFiles()
   * @param date yyyy-MM-dd, the day the file is for
   * @param fileName name the browser saves the file as, e.g. myReport_2026-10-04.csv
   * @param contents text contents, e.g. csv
   * @return this for chaining
   */
  public GshTemplateOutput assignDownloadFile(String date, String fileName, String contents) {
    GrouperFile grouperFile = GshTemplateDownloadFile.save(this.retrieveTemplateConfigIdRequired(), date, fileName, contents);
    this.downloadGrouperFileId = grouperFile.getId();
    return this;
  }

  /**
   * GRP-7446: save binary contents (e.g. a zip, xlsx) as this template's download file for a date and file name,
   * and download it after the template runs.  Same as the text version otherwise: an existing file for the same
   * date and name is replaced
   * @param date yyyy-MM-dd, the day this file is for (used by deleteExpiredDownloadFiles)
   * @param fileName file name the browser saves, the extension decides the content type (e.g. .zip)
   * @param contents binary contents
   * @return this for chaining
   */
  public GshTemplateOutput assignDownloadFile(String date, String fileName, byte[] contents) {
    GrouperFile grouperFile = GshTemplateDownloadFile.save(this.retrieveTemplateConfigIdRequired(), date, fileName, contents);
    this.downloadGrouperFileId = grouperFile.getId();
    return this;
  }
  
  /**
   * GRP-7438: delete this template's download files whose date is more than retentionDays before
   * today.  There is no central cleanup, so templates that save download files should call this,
   * e.g. after assignDownloadFile().  Only this template's files are affected
   * @param retentionDays 0 keeps only today, 1 keeps today and yesterday, 7 keeps a week, etc
   * @return number of files deleted
   */
  public int deleteExpiredDownloadFiles(int retentionDays) {
    return GshTemplateDownloadFile.deleteExpired(this.retrieveTemplateConfigIdRequired(),
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), retentionDays);
  }
  
  
}
