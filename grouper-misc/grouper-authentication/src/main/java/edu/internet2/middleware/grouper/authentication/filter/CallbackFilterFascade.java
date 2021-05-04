package edu.internet2.middleware.grouper.authentication.filter;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.pac4j.jee.filter.CallbackFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CallbackFilterFascade implements Filter {
    private CallbackFilter uiDelegate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.uiDelegate = new CallbackFilter();
        this.uiDelegate.init(filterConfig);
        this.initDelegate();
    }

    private void initDelegate() {
        this.uiDelegate.setDefaultUrl(GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.defaultUrl", "/"));
        this.uiDelegate.setRenewSession(true);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean runGrouperUi = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);

        if (runGrouperUi && isExternalAuthenticationEnabled() && isCallbackUrlCalled((HttpServletRequest) request)) {
            this.uiDelegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static boolean isCallbackUrlCalled(HttpServletRequest request) {
        return getRequestPathInContext(request).matches(GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.callbackUrl", "/callback"));
    }

    private static String getRequestPathInContext(HttpServletRequest request) {
        return request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
    }

    private static boolean isExternalAuthenticationEnabled() {
        return GrouperUiConfig.retrieveConfig().propertyValueBoolean("external.authentication.enabled", false);
    }

    @Override
    public void destroy() {
        this.uiDelegate.destroy();
    }
}
