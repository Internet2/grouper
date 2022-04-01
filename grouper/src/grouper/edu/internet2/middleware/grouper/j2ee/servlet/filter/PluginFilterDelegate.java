package edu.internet2.middleware.grouper.j2ee.servlet.filter;

import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * PluginFilterDelegate will delegate behavior to the correct grouper-authentication plugin filter. If the plugin is not properly
 * installed, this delegate should fail silently and simply pass control onto the next filter in the filter chain without issue
 */
public class PluginFilterDelegate implements Filter {
    private static final Log LOG = GrouperUtil.getLog(PluginFilterDelegate.class);

    protected Filter delegateFilter = new Filter() {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                        throws IOException, ServletException {
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    };

    public PluginFilterDelegate(String moduleJarNameInput, String pluginClassName) {
        try {
            delegateFilter = GrouperPluginManager.retrievePluginImplementation(moduleJarNameInput, Filter.class, pluginClassName);
        }
        catch (Throwable e) {
            LOG.error("Error retrieving plugin implementation : [" + pluginClassName + "]", e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        delegateFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                    throws IOException, ServletException {
        delegateFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {
        delegateFilter.destroy();
    }

}