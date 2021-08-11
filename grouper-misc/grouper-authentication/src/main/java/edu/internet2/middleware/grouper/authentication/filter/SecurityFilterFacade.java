package edu.internet2.middleware.grouper.authentication.filter;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.Pac4jConfigFactory;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.filter.SecurityFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class SecurityFilterFacade implements Filter {
    private SecurityFilter uiDelegate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.uiDelegate = new SecurityFilter();
        this.uiDelegate.init(filterConfig);
        this.initDelegate();
    }

    public void initDelegate() {
        if (ConfigUtils.isGrouperUi() && FilterFascadeUtils.isExternalAuthenticationEnabled()) {
            this.uiDelegate.setSharedConfig(ConfigBuilder.build(Pac4jConfigFactory.class.getCanonicalName()));
            this.uiDelegate.setClients("client");
            this.uiDelegate.setMatchers(String.join(Pac4jConstants.ELEMENT_SEPARATOR, "securityExclusions"));
            this.uiDelegate.setAuthorizers(DefaultAuthorizers.NONE);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (ConfigUtils.isGrouperUi() && FilterFascadeUtils.isExternalAuthenticationEnabled()) {
            this.uiDelegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        this.uiDelegate.destroy();
    }
}
