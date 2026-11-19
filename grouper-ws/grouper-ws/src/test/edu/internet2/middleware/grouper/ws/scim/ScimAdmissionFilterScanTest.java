/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ws.scim;

import java.util.Set;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Security advisory H7 (SCIM part): on the releases where SCIM is the stock Jersey ServletContainer
 * (2.6.x, 4.17.x and older, 5.0.0 to 5.17.2) the admission check is the JAX-RS request filter
 * GrouperScimAdmissionFilter, and nothing registers it by name: Jersey finds it through the
 * provider package scan of the SCIM servlet.  If the scan did not pick it up (wrong package, a
 * missing annotation, a class file the scanner cannot read) SCIM would silently stay open.
 * ScimAdmissionTest calls the filter directly, so it cannot see that; this test does the same
 * package scan the servlet does and checks the filter is in the result.
 *
 * <p>The scan is configured exactly as CommonServletContainerInitializer configures the
 * SCIMRestServlet on every release (read from each release tag 2026-11-14): init parameter
 * jersey.config.server.provider.packages = edu.internet2.middleware.grouper.ws.scim.providers.
 * Jersey's ServletContainer copies init parameters into its ResourceConfig properties, and
 * ResourceConfig.getClasses() runs the scan (classes annotated @Path or @Provider).</p>
 *
 * <p>Releases with GrouperScimServlet (4.18.0 and later 4.x, 5.18.0 and later) admit in the
 * servlet and do not have the filter: the test prints a skip line and passes there.  No
 * database; junit.framework.TestCase so it compiles on 2.6.19 (Java 8).</p>
 *
 * <p>Goes in grouper-ws/grouper-ws/src/test/edu/internet2/middleware/grouper/ws/scim/.</p>
 */
public class ScimAdmissionFilterScanTest extends TestCase {

  /** the servlet init parameter CommonServletContainerInitializer sets on the SCIM servlet */
  private static final String PROVIDER_PACKAGES_PARAM = "jersey.config.server.provider.packages";

  /** its value on every release */
  private static final String PROVIDERS_PACKAGE = "edu.internet2.middleware.grouper.ws.scim.providers";

  /** front door before 4.18.0 / 5.18.0 (the patch adds it there) */
  private static final String FILTER_CLASS = PROVIDERS_PACKAGE + ".GrouperScimAdmissionFilter";

  /** front door from 4.18.0 / 5.18.0 */
  private static final String SERVLET_CLASS = "edu.internet2.middleware.grouper.ws.scim.GrouperScimServlet";

  /** a SCIM resource class every release has, to show the scan itself worked */
  private static final String GROUP_PROVIDER_CLASS = PROVIDERS_PACKAGE + ".TierGroupProvider";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ScimAdmissionFilterScanTest.class);
  }

  /**
   *
   */
  public ScimAdmissionFilterScanTest() {
    super();
  }

  /**
   * @param name
   */
  public ScimAdmissionFilterScanTest(String name) {
    super(name);
  }

  /**
   * the SCIM servlet's provider package scan registers GrouperScimAdmissionFilter, and the filter
   * is a pre-matching request filter (so it also runs for paths no resource matches)
   */
  public void testProviderScanRegistersAdmissionFilter() {
    Class<?> filterClass = classOrNull(FILTER_CLASS);
    if (filterClass == null) {
      if (classOrNull(SERVLET_CLASS) != null) {
        // this release admits SCIM callers in GrouperScimServlet.service (ScimAdmissionTest covers it)
        System.out.println("skip ScimAdmissionFilterScanTest: this release has GrouperScimServlet, no admission filter to scan for");
        return;
      }
      fail("this release has neither " + FILTER_CLASS + " nor " + SERVLET_CLASS
          + ": the SCIM admission patch is not applied");
    }

    // what Jersey's ServletContainer does with the init parameter, then the scan
    ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.property(PROVIDER_PACKAGES_PARAM, PROVIDERS_PACKAGE);
    Set<Class<?>> scanned = resourceConfig.getClasses();

    // sanity: the scan found the SCIM resources, so an empty result is not mistaken for a pass
    Class<?> groupProviderClass = classOrNull(GROUP_PROVIDER_CLASS);
    assertNotNull(GROUP_PROVIDER_CLASS + " missing from the test classpath", groupProviderClass);
    assertTrue("the provider scan did not find the SCIM resources, scanned: " + scanned,
        scanned.contains(groupProviderClass));

    // the point of the test: the admission filter is registered by the scan
    assertTrue("the SCIM provider scan of " + PROVIDERS_PACKAGE + " does not register " + FILTER_CLASS
        + ", so SCIM would not check ws.client.user.group.name; scanned: " + scanned,
        scanned.contains(filterClass));

    // how it is registered matters too: a request filter, @Provider (what the scan looks for),
    // and @PreMatching (runs before resource matching, so discovery and unknown paths are covered)
    assertTrue(FILTER_CLASS + " must be a ContainerRequestFilter",
        ContainerRequestFilter.class.isAssignableFrom(filterClass));
    assertNotNull(FILTER_CLASS + " must be annotated @Provider", filterClass.getAnnotation(Provider.class));
    assertNotNull(FILTER_CLASS + " must be annotated @PreMatching", filterClass.getAnnotation(PreMatching.class));
  }

  /**
   * @param className
   * @return the class or null if this release does not have it
   */
  private static Class<?> classOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }
}
