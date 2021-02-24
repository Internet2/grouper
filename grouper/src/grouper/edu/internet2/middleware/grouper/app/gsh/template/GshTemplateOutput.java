package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;

public class GshTemplateOutput {
  
  private boolean isError;
  
  private static ThreadLocal<GshTemplateOutput> threadLocalGshTemplateOutput = new InheritableThreadLocal<GshTemplateOutput>();
  
  private List<GshOutputLine> outputLines = new ArrayList<GshOutputLine>();
  
  private List<GshValidationLine> validationLines = new ArrayList<GshValidationLine>();
  
  
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

  
  public List<GshValidationLine> getValidationLines() {
    return validationLines;
  }

  public static GshTemplateOutput retrieveGshTemplateOutput() {
    return threadLocalGshTemplateOutput.get();
  }
  
  
  public static void assignThreadLocalGshTemplateOutput(GshTemplateOutput gshTemplateOutput) {
    threadLocalGshTemplateOutput.set(gshTemplateOutput);
  }
  
  public static void removeThreadLocalGshTemplateOutput() {
    threadLocalGshTemplateOutput.remove();
  }
  
  
}
