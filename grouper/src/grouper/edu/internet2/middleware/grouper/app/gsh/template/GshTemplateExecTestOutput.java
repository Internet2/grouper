package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang.StringUtils;

/**
 * run a template
 * @author mchyzer
 *
 */
public class GshTemplateExecTestOutput {
  
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (GshValidationLine gshValidationLine : this.gshTemplateOutput.getValidationLines()) {
      result.append(gshValidationLine.toString()).append("\n");
    }
    for (GshOutputLine gshOutputLine : this.gshTemplateOutput.getOutputLines()) {
      result.append(gshOutputLine.toString()).append("\n");
    }
    result.append(toStringSummary());
    return StringUtils.replace(result.toString(), "<br />", "\n");
  }
  
  public String toStringSummary() {
    String result = (this.getSuccesses() == tests ? "Success" : "Error") + ": " + tests + " tests"
        + (this.getSuccesses() > 0 ? (", " + this.getSuccesses() + " successes") : "") 
        + (this.getFailures() > 0 ? (", " + this.getFailures() + " failures") : "")
        + (this.getExceptions() > 0 ? (", " + this.getExceptions() + " exceptions") : "")
        + (this.getInvalidTests() > 0 ? (", " + this.getInvalidTests() + " invalid tests") : "") + "!";
    return result;
  }
  
  private GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
  
  public GshTemplateOutput getGshTemplateOutput() {
    return gshTemplateOutput;
  }
  
  public void setGshTemplateOutput(GshTemplateOutput gshTemplateOutput) {
    this.gshTemplateOutput = gshTemplateOutput;
  }
  
  private int tests = 0;
  
  private int successes = 0;
  
  private int failures = 0;
  
  private int exceptions = 0;
  
  private int invalidTests = 0;

  public void addSuccess() {
    this.successes++;
  }
   
  public void addFailure() {
    this.failures++;
  }
   
  public void addTest() {
    this.tests++;
  }
   
  public void addException() {
    this.exceptions++;
  }
   
  public void addInvalidTest() {
    this.invalidTests++;
  }
   
  
  
  public int getTests() {
    return tests;
  }

  
  public void setTests(int tests) {
    this.tests = tests;
  }

  
  public int getSuccesses() {
    return successes;
  }

  
  public void setSuccesses(int successes) {
    this.successes = successes;
  }

  
  public int getFailures() {
    return failures;
  }

  
  public void setFailures(int failures) {
    this.failures = failures;
  }

  
  public int getExceptions() {
    return exceptions;
  }

  
  public void setExceptions(int exceptions) {
    this.exceptions = exceptions;
  }

  
  public int getInvalidTests() {
    return invalidTests;
  }

  
  public void setInvalidTests(int invalidTests) {
    this.invalidTests = invalidTests;
  }

  
  
}
