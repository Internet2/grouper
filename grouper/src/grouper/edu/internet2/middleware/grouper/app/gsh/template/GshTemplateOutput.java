package edu.internet2.middleware.grouper.app.gsh.template;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
    
    return result.toString();
  }
  
  private boolean isError;
  
  private static ThreadLocal<WeakReference<GshTemplateOutput>> threadLocalGshTemplateOutput = new InheritableThreadLocal<>();
  
  private List<GshOutputLine> outputLines = new ArrayList<GshOutputLine>();
  
  private List<GshValidationLine> validationLines = new ArrayList<GshValidationLine>();
  
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
