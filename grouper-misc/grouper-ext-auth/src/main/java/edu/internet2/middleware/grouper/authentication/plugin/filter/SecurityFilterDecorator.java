package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.Pac4jConfigFactory;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.filter.SecurityFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SecurityFilterDecorator extends SecurityFilter implements Reinitializable {
    private Timer timer = new Timer();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.initDecorator();
        int period = ConfigUtils.getBestGrouperConfiguration().propertyValueInt("external.authentication.config.reload.milliseconds", 60 * 1000);
        if (period > 0) {
            TimerTask timerTask = new ReinitializingTimer(this);
            this.timer.schedule(timerTask, period, period);
        }
    }

    public void initDecorator() {
        if (ConfigUtils.isGrouperUi() && FilterDecoratorUtils.isExternalAuthenticationEnabled()) {
            this.setSharedConfig(new Pac4jConfigFactory().build());
            this.setClients("client");
            this.setMatchers(String.join(Pac4jConstants.ELEMENT_SEPARATOR, "securityExclusions"));
            this.setAuthorizers(DefaultAuthorizers.NONE);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (ConfigUtils.isGrouperUi() && FilterDecoratorUtils.isExternalAuthenticationEnabled()) {
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
