package edu.vt.middleware.ldap;

import java.util.Arrays;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

// TODO this is a kludge to provide modifyAttributes(String dn, ModificationItem[] mods)

public class LdappcLdap extends Ldap {

  private static final long serialVersionUID = -9022990068084904260L;

  public LdappcLdap() {    
  }
  
  public LdappcLdap(LdapConfig config) {
    super(config);
  }

  public void modifyAttributes(final String dn, final ModificationItem[] mods) throws NamingException {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Modifiy attributes with the following parameters:");
      this.logger.debug("  dn = " + dn);
      this.logger.debug("  mods = " + Arrays.asList(mods));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("  config = " + this.config.getEnvironment());
      }
    }

    LdapContext ctx = null;
    try {
      for (int i = 0; i <= this.config.getOperationRetry(); i++) {
        try {
          ctx = this.getContext();
          ctx.modifyAttributes(dn, mods);
          break;
        } catch (CommunicationException e) {
          if (i == this.config.getOperationRetry()) {
            throw e;
          }
          if (this.logger.isWarnEnabled()) {
            this.logger.warn("Error while communicating with the LDAP, retrying", e);
          }
          this.reconnect();
        }
      }
    } finally {
      if (ctx != null) {
        ctx.close();
      }
    }
  }
}
