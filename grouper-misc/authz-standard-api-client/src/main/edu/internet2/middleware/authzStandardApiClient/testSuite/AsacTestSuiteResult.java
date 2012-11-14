package edu.internet2.middleware.authzStandardApiClient.testSuite;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;

/**
 * result bean for test
 * @author mchyzer
 *
 */
public abstract class AsacTestSuiteResult {

  /**
   * results
   */
  private AsacTestSuiteResults results;

  /**
   * results
   * @return the results
   */
  public AsacTestSuiteResults getResults() {
    return this.results;
  }

  /**
   * 
   * @param results
   */
  public AsacTestSuiteResult(AsacTestSuiteResults results) {
    super();
    this.results = results;
  }

  /**
   * run this test by looking for instance methods that are void and start with "test"
   */
  public void runTests() {
    
    Set<Method> methods = new LinkedHashSet<Method>();
    
    StandardApiClientUtils.methodsHelper(this.getClass(), AsacTestSuiteResult.class, false, false, null, false, methods);
    
    Iterator<Method> methodIterator = methods.iterator();
    
    //look for methods of a certain name
    while (methodIterator.hasNext()) {
      Method method = methodIterator.next();
      
      if (!method.getName().startsWith("test")) {
        methodIterator.remove();
        continue;
      }
      if (StandardApiClientUtils.length(method.getGenericParameterTypes()) > 0) {
        this.addWarning("Test: " + method.getName() + " takes parameters but should not");
        methodIterator.remove();
        continue;
      }
      if (!void.class.equals(method.getReturnType())) {
        this.addWarning("Test: " + method.getName() + " should be a void method and is not: " + method.getReturnType());
        //dont remove... just ignore the results
      }
    }
    
    if (methods.size() == 0) {
      this.addWarning("No tests found");
    } else {
      for (Method method : methods) {
        this.testCount++;
        try {
          method.invoke(this, (Object[])null);
          successCount++;
          if (this.getResults().getVerbose().atLeastVerbose(AsacTestSuiteVerbose.high)) {
            this.appendToReport("Test: " + this.getName() + "." + method.getName() + " - Success");
          }
        } catch (AsacTestFailException atfe) {
          this.addFailure(atfe.getMessage(), atfe);
          //short circuit the test
        } catch (Throwable t) {
          this.addException(t);
        }
      }

      if (this.getResults().getVerbose().atLeastVerbose(AsacTestSuiteVerbose.medium)) {
        this.appendToReport("Test suite: " + this.getName() 
            + " - successes: " + this.getSuccessCount() + ", failures: "
            + this.getFailureCount() + ", warnings: " + this.getWarningCount());
      }

    }
    
  }
  
  /**
   * get the name of this test
   * @return the name
   */
  public abstract String getName();

  /**
   * 
   * @param warning
   */
  public void addWarning(String warning) {
    this.warningCount++;
    this.appendToReport("Test suite: " + this.getName() + " - Warning: " + warning);
  }

  /**
   * count of how many testCount
   */
  private int testCount = 0;
  
  /**
   * count of how many successCount
   */
  private int successCount = 0;
  
  /**
   * count of how many failureCount
   */
  private int failureCount = 0;
  
  /**
   * count of how many warningCount
   */
  private int warningCount = 0;
  
  /**
   * report of activity, failureCount, etc
   */
  private StringBuilder report = new StringBuilder();

  
  /**
   * count of how many testCount
   * @return the testCount
   */
  public int getTestCount() {
    return this.testCount;
  }

  
  /**
   * @return the successCount
   */
  public int getSuccessCount() {
    return this.successCount;
  }

  
  /**
   * @return the failureCount
   */
  public int getFailureCount() {
    return this.failureCount;
  }

  
  /**
   * @return the warningCount
   */
  public int getWarningCount() {
    return this.warningCount;
  }

  
  /**
   * @return the report
   */
  public String getReport() {
    return report.toString();
  }

  
  /**
   * append to report, a newline will be appended by this method
   * @param report the report to set
   */
  public void appendToReport(String line) {
    if (this.report.length() > 0) {
      this.report.append("\n");
    }
    this.report.append(line);
  }

  /**
   * 
   * @param warning
   */
  public void addFailure(String warning, Throwable t) {
    this.failureCount++;
    this.appendToReport("Test suite: " + this.getName() + " - Failure: " + warning + "\n" + StandardApiClientUtils.getFullStackTrace(t) + "\n");
  }
  
  /**
   * 
   * @param exception
   */
  public void addException(Throwable t) {
    this.failureCount++;
    this.appendToReport("Test suite: " + this.getName() + " - Exception: " + t.getMessage() + "\n" + StandardApiClientUtils.getFullStackTrace(t) + "\n");
  }
  
  /**
   * 
   * @param message
   * @param expectedString
   * @param actualString
   */
  public void assertEquals(String message, String expectedString, String actualString) {
    if (!StandardApiClientUtils.equals(expectedString, actualString)) {
      throw new AsacTestFailException(message + ", strings do not match, expected: '" 
          + expectedString +"', but was '" + actualString + "'");
    }
  }
  
  /**
   * 
   * @param message
   * @param expectedString
   * @param actualString
   */
  public void assertNotNull(String message, Object object) {
    if (object == null) {
      throw new AsacTestFailException(message + ", object is null");
    }
  }
  
}
