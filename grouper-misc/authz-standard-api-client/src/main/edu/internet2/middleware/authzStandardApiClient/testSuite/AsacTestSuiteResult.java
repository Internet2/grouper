package edu.internet2.middleware.authzStandardApiClient.testSuite;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacResponseBeanBase;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;
import edu.internet2.middleware.authzStandardApiClient.ws.StandardApiClientWs;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.lang.StringUtils;

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
   * more information about the test
   */
  private StringBuilder testReport;

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
        this.addWarning(" - Test: " + method.getName() + " takes parameters but should not");
        methodIterator.remove();
        continue;
      }
      if (!void.class.equals(method.getReturnType())) {
        this.addWarning(" - Test: " + method.getName() + " should be a void method and is not: " + method.getReturnType());
        //dont remove... just ignore the results
      }
    }
    
    if (methods.size() == 0) {
      this.addWarning(" - No tests found");
    } else {
      for (Method method : methods) {
        this.testCount++;
        this.testReport = new StringBuilder();
        try {
          method.invoke(this, (Object[])null);
          successCount++;
          if (this.getResults().getVerbose().atLeastVerbose(AsacTestSuiteVerbose.high)) {
            this.appendToReport(" - Success: test: " + this.getName() + "." + method.getName());
          }
          if (this.getResults().getVerbose().atLeastVerbose(AsacTestSuiteVerbose.higher) && this.testReport.length() > 0) {
            this.appendToReport(this.testReport.toString());
          }
          
        } catch (AsacTestFailException atfe) {
          this.addFailure(atfe.getMessage(), atfe);
          //short circuit the test
        } catch (Throwable t) {
          this.addException(t);
        }
      }

      if (this.getResults().getVerbose().atLeastVerbose(AsacTestSuiteVerbose.medium)) {
        this.prependToReport((this.isSuccess() ? "Success:" : "FAILURE!") 
            + " test suite: " + this.getName() 
            + " - tests: " + this.getTestCount()
            + ", successes: " + this.getSuccessCount() + ", failures: "
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
   * append to test report, a newline will be appended by this method
   * @param report the report to set
   */
  public void appendToTestReport(String line) {
    if (this.testReport.length() > 0) {
      this.testReport.append("\n");
    }
    this.testReport.append("   - " + StringUtils.uncapitalize(line));
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
   * @param describeObject
   * @param expectedString
   * @param actualString
   */
  public void assertEquals(String describeObject, String expectedString, String actualString) {
    if (!StandardApiClientUtils.equals(expectedString, actualString)) {
      throw new AsacTestFailException(describeObject + ", strings do not match, expected: '" 
          + expectedString +"', but was '" + actualString + "'");
    }
    this.appendToTestReport(describeObject + " equals: " + StandardApiClientUtils.abbreviate(expectedString, 40));
  }
  
  /**
   * 
   * @param describeObject
   * @param expected
   * @param actual
   */
  public void assertEquals(String describeObject, Object expected, Object actual) {
    if (!StandardApiClientUtils.equals(expected, actual)) {
      throw new AsacTestFailException(describeObject + ", do not match, expected: '" 
          + StandardApiClientUtils.toStringForLog(expected, 1000) + "', but was '" 
          + StandardApiClientUtils.toStringForLog(actual, 1000) + "'");
    }
    this.appendToTestReport(describeObject + " equals: " + StandardApiClientUtils.abbreviate(
        StandardApiClientUtils.toStringForLog(expected, 40), 45));
  }
  
  /**
   * 
   * @param describeObject
   * @param expected
   * @param actual
   */
  public void assertTrue(String descriptionWithShould, Object theBoolean) {
    if (theBoolean == null) {
      throw new AsacTestFailException(descriptionWithShould + ", theBoolean is null");
    }
    if ((!(theBoolean instanceof Boolean))) {
      throw new AsacTestFailException(descriptionWithShould + ", theBoolean is not a boolean: " + theBoolean);
    }
    if (!(Boolean)theBoolean) {
      throw new AsacTestFailException(descriptionWithShould);
    }
    this.appendToTestReport(descriptionWithShould + "... success");
  }
  
  /**
   * 
   * @param describeObject
   * @param expectedString
   * @param actualString
   */
  public void assertNotNull(String describeObject, Object object) {
    if (object == null) {
      throw new AsacTestFailException(describeObject + ", object is null");
    }
    this.appendToTestReport(describeObject + " is not null");
  }

  /**
   * 
   * @param describeDate
   * @param string
   */
  public void assertValidDate(String describeDate, String string) {
    if (string == null) {
      throw new AsacTestFailException(describeDate + ", object is null, should be a date");
    }
    try {
      StandardApiClientUtils.convertFromIso8601(string);
      this.appendToTestReport(describeDate + " is a valid date: " + string);
    } catch (Exception e) {
      throw new AsacTestFailException(describeDate + ", object is not a valid date, should be e.g. 2012-10-04T03:10:14.123Z, but was: '" + string + "'");
      //shouldnt do this
    }
  }

  /**
   * make sure string is not blank
   * @param describeObject
   * @param string
   */
  public void assertNotBlank(String describeObject, String string) {
    if (StandardApiClientUtils.isBlank(string)) {
      throw new AsacTestFailException(describeObject + ", string is blank");
    }
    this.appendToTestReport(describeObject + " is not blank");
  }

  /**
   * if the tests run successfully
   * @return the success
   */
  public boolean isSuccess() {
    //if there are no failures
    return this.getFailureCount() == 0;
  }

  /**
   * prepend to report, a newline will be appended by this method if needed
   * @param report the report to set
   */
  public void prependToReport(String line) {
    boolean hasLength = this.report.length() > 0;
    this.report.insert(0, line + (hasLength ? "\n" : ""));
  }

  /**
   * execute http tests
   */
  protected void executeTestsForHttp(int statusCode, AsacRestContentType asacRestContentType) {
    assertEquals("httpStatusCode", statusCode, StandardApiClientWs.mostRecentHttpStatusCode);
    Header header = StandardApiClientWs.mostRecentHttpMethod.getResponseHeader("Content-Type");
    String contentType = header == null ? "" : header.getValue();
    assertTrue("Content type header should contain '" + asacRestContentType.name() + "': " 
        + contentType, contentType.toLowerCase().contains(asacRestContentType.name()));
  }

  /**
   * 
   * @param asacResponseBeanBase
   */
  protected void executeTestsForServiceMeta(
      AsacResponseBeanBase asacResponseBeanBase) {
    assertNotNull("serviceMeta", asacResponseBeanBase.getServiceMeta());
    assertNotBlank("serviceMeta.pathSeparator", asacResponseBeanBase.getServiceMeta().getPathSeparator());
    assertNotNull("serviceMeta.serverVersion", asacResponseBeanBase.getServiceMeta().getServerVersion());
    assertTrue("serviceMeta.serverVersion should be 1.x: " + asacResponseBeanBase.getServiceMeta().getServerVersion(), 
        asacResponseBeanBase.getServiceMeta().getServerVersion().matches("^[0-9]\\.[0-9]+$"));
    assertNotBlank("serviceMeta.serviceRootUri", asacResponseBeanBase.getServiceMeta().getServiceRootUri());
    assertTrue("serviceMeta.serviceRootUri starts with http: " 
        + asacResponseBeanBase.getServiceMeta().getServiceRootUri(), 
        asacResponseBeanBase.getServiceMeta().getServiceRootUri().startsWith("http"));
  }

  /**
   * run tests for meta
   * @param asacResponseBeanBase
   * @param statusCode
   * @param structureName
   * @param expectedUriSuffix
   */
  protected void executeTestsForMeta(AsacResponseBeanBase asacResponseBeanBase, String statusCode, String structureName, String expectedUriSuffix) {
    assertNotNull("meta", asacResponseBeanBase.getMeta());
    assertValidDate("meta.lastModified", asacResponseBeanBase.getMeta().getLastModified());
    assertEquals("meta.selfUri", asacResponseBeanBase.getServiceMeta().getServiceRootUri() + expectedUriSuffix, 
        asacResponseBeanBase.getMeta().getSelfUri());
    assertEquals("meta.statusCode", statusCode, asacResponseBeanBase.getMeta().getStatusCode());
    assertEquals("meta.structureName", structureName, asacResponseBeanBase.getMeta().getStructureName());
    assertTrue("meta.success", asacResponseBeanBase.getMeta().getSuccess());
  }

  /**
   * run tests for response meta
   * @param asacResponseBeanBase
   * @param expectedHttpStatusCode
   */
  protected void executeTestsForResponseMeta(
      AsacResponseBeanBase asacResponseBeanBase, int expectedHttpStatusCode) {
    assertNotNull("responseMeta", asacResponseBeanBase.getResponseMeta());
    assertEquals("responseMeta.httpStatusCode", expectedHttpStatusCode, asacResponseBeanBase.getResponseMeta().getHttpStatusCode());
    assertNotNull("responseMeta.mills", asacResponseBeanBase.getResponseMeta().getMillis());
  }
}
