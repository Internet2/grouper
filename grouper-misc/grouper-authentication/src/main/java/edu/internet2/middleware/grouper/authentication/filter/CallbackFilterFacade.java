package edu.internet2.middleware.grouper.authentication.filter;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import org.pac4j.jee.filter.CallbackFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CallbackFilterFacade implements Filter {
    private CallbackFilter uiDelegate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.uiDelegate = new CallbackFilter();
        this.uiDelegate.init(filterConfig);
        this.initDelegate();
    }

    public void initDelegate() {
        this.uiDelegate.setDefaultUrl(ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.defaultUrl", "/"));
        this.uiDelegate.setRenewSession(true);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean runGrouperUi = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);

        if (runGrouperUi && FilterFascadeUtils.isExternalAuthenticationEnabled() && isCallbackUrlCalled((HttpServletRequest) request)) {
            this.uiDelegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static boolean isCallbackUrlCalled(HttpServletRequest request) {
        return FilterFascadeUtils.getRequestPathInContext(request).matches(ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.callbackUrl", "/callback"));
    }

    @Override
    public void destroy() {
        this.uiDelegate.destroy();
    }
}
