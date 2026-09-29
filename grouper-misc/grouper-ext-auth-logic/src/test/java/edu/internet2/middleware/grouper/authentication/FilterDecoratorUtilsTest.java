package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.plugin.GrouperAuthentication;
import edu.internet2.middleware.grouper.authentication.plugin.filter.FilterDecoratorUtils;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that pac4j login failures are reported to the user as a short message and not as a stacktrace
 */
public class FilterDecoratorUtilsTest {
    private static final String EXCEPTION_MESSAGE = "secret internal detail";

    private MockedStatic<FrameworkUtil> frameworkUtilMockedStatic;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        this.frameworkUtilMockedStatic = Mockito.mockStatic(FrameworkUtil.class);

        Bundle bundle = mock(Bundle.class);
        this.frameworkUtilMockedStatic.when(() -> FrameworkUtil.getBundle(GrouperAuthentication.class)).thenReturn(bundle);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);

        ServiceReference<LogFactory> logFactoryServiceReference = mock(ServiceReference.class);
        when(bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)).thenReturn(new ServiceReference[]{logFactoryServiceReference});
        when(bundleContext.getService(logFactoryServiceReference)).thenReturn(LogFactory.getFactory());
    }

    @After
    public void tearDown() {
        this.frameworkUtilMockedStatic.close();
    }

    /*
        the externalized text needs the Grouper text config (database backed) which is not available in this unit test,
        so this exercises the fallback message
     */
    @Test
    public void testSends500WithShortMessage() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterDecoratorUtils.handleLoginFailure(new RuntimeException(EXCEPTION_MESSAGE), response);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(response).sendError(Mockito.eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), message.capture());
        assertFalse(message.getValue().trim().isEmpty());
    }

    @Test
    public void testStacktraceNotSentToUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterDecoratorUtils.handleLoginFailure(new RuntimeException(EXCEPTION_MESSAGE), response);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(response).sendError(anyInt(), message.capture());
        // the fallback text has no exception detail
        assertFalse(message.getValue().contains(EXCEPTION_MESSAGE));
        assertFalse(message.getValue().contains("RuntimeException"));
        assertFalse(message.getValue().contains("\tat "));
        verify(response, never()).getWriter();
        verify(response, never()).getOutputStream();
    }

    @Test
    public void testExceptionMessageTokenSubstituted() throws Exception {
        String message = FilterDecoratorUtils.substituteExceptionMessage("Failed: %%exceptionMessage%% (%%exceptionMessage%%)", new RuntimeException(EXCEPTION_MESSAGE));
        assertEquals("Failed: " + EXCEPTION_MESSAGE + " (" + EXCEPTION_MESSAGE + ")", message);
    }

    @Test
    public void testExceptionMessageTokenWithNullMessage() throws Exception {
        assertEquals("Failed: ", FilterDecoratorUtils.substituteExceptionMessage("Failed: %%exceptionMessage%%", new RuntimeException()));
    }

    @Test
    public void testCommittedResponseLeftAlone() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.isCommitted()).thenReturn(true);

        FilterDecoratorUtils.handleLoginFailure(new RuntimeException(EXCEPTION_MESSAGE), response);

        verify(response, never()).sendError(anyInt(), anyString());
        verify(response, never()).resetBuffer();
    }
}
