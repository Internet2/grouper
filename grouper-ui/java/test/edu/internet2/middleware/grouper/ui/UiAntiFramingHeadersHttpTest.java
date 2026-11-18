/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.ui;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;

/**
 * <p>Against the running UI (junit.test.tomcat=true): UI responses carry the anti-framing headers,
 * which proves the patched CommonServletContainerInitializer registered GrouperUiAntiFramingFilter
 * in the real container.  GrouperUiAntiFramingFilterTest covers the filter logic and the
 * registration calls; this covers the deployment.  Assumes the running UI has the default config (grouperUi.antiFraming.* not set).</p>
 *
 * <p>No login needed: the headers are set before authentication runs, so the login page or
 * redirect the anonymous request gets must carry them too.  Uses HttpURLConnection (Java 8, so
 * the same file compiles on 2.6.19).</p>
 *
 * <p>Goes in grouper-ui/java/test/edu/internet2/middleware/grouper/ui/.</p>
 */
public class UiAntiFramingHeadersHttpTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiAntiFramingHeadersHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiAntiFramingHeadersHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiAntiFramingHeadersHttpTest(String name) {
    super(name);
  }

  /**
   * base url of the running UI: grouper.ui.url if set, else the junit.test.tomcat.* keys
   * @return e.g. http://localhost:8080/grouper
   */
  static String uiBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    String uiUrl = config.propertyValueString("grouper.ui.url");
    if (uiUrl != null && uiUrl.trim().length() > 0) {
      return uiUrl.trim().replaceAll("/+$", "");
    }
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String host = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    String context = config.propertyValueString("junit.test.tomcat.uiContext", "grouper");
    return (ssl ? "https" : "http") + "://" + host + ":" + port + "/" + context;
  }

  /**
   * UI screen URL, a static file under grouperExternal and a JSP all send frame-ancestors 'self' and SAMEORIGIN
   * @throws Exception
   */
  public void testUiResponsesHaveAntiFramingHeaders() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String[] paths = new String[] {
        "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain",
        "/grouperExternal/public/assets/js/jquery.form.js",
        "/index.jsp"};
    for (String path : paths) {
      HttpURLConnection connection = (HttpURLConnection)new URL(baseUrl + path).openConnection();
      try {
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        int code = connection.getResponseCode();
        String csp = connection.getHeaderField("Content-Security-Policy");
        String xFrameOptions = connection.getHeaderField("X-Frame-Options");
        String context = path + " (http " + code + ")";
        assertNotNull("Content-Security-Policy missing on " + context, csp);
        assertTrue("frame-ancestors missing on " + context + ": " + csp, csp.contains("frame-ancestors 'self'"));
        assertEquals("X-Frame-Options on " + context, "SAMEORIGIN", xFrameOptions);
        drain(connection);
      } finally {
        connection.disconnect();
      }
    }
  }

  /**
   * read and close the body so the connection can be reused
   * @param connection
   */
  private static void drain(HttpURLConnection connection) {
    InputStream inputStream = null;
    try {
      inputStream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
      if (inputStream != null) {
        byte[] buffer = new byte[4096];
        while (inputStream.read(buffer) != -1) {
          // discard
        }
      }
    } catch (Exception e) {
      // ignore, only draining
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
}
