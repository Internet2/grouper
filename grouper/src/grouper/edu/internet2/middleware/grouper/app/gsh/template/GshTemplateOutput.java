package edu.internet2.middleware.grouper.app.gsh.template;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GshTemplateOutput {
  
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
  
  
}
