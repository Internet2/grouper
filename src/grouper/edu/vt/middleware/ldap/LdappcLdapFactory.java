package edu.vt.middleware.ldap;

import java.io.InputStream;

import javax.naming.NamingException;

import edu.vt.middleware.ldap.pool.AbstractLdapFactory;
import edu.vt.middleware.ldap.pool.ConnectLdapValidator;

// TODO this is a kludge to provide modifyAttributes(String dn, ModificationItem[] mods)
// copied directly from vt-ldap

/**
 * <code>DefaultLdapFactory</code> provides a simple implementation of a ldap factory.
 * Uses {@link ConnectLdapValidator} by default.
 * 
 * @author Middleware Services
 * @version $Revision: 1.2 $ $Date: 2009-09-24 20:35:12 $
 */
public class LdappcLdapFactory extends AbstractLdapFactory<Ldap> {

  /** Ldap config to create ldap objects with. */
  private LdapConfig config;

  /** Whether to connect to the ldap on object creation. */
  private boolean connectOnCreate = true;

  /**
   * This creates a new <code>DefaultLdapFactory</code> with the default properties file,
   * which must be located in your classpath.
   */
  public LdappcLdapFactory() {
    this.config = LdapConfig.createFromProperties(null);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }

  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied input
   * stream.
   *
   * @param  is  <code>InputStream</code>
   */
  public LdappcLdapFactory(final InputStream is) {
    this.config = LdapConfig.createFromProperties(is);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }

  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied ldap
   * configuration. The ldap configuration will be marked as immutable by this factory.
   * 
   * @param lc
   *          ldap config
   */
  public LdappcLdapFactory(final LdapConfig lc) {
    this.config = lc;
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }

  /**
   * Returns whether ldap objects will attempt to connect after creation. Default is true.
   * 
   * @return <code>boolean</code>
   */
  public boolean getConnectOnCreate() {
    return this.connectOnCreate;
  }

  /**
   * This sets whether newly created ldap objects will attempt to connect. Default is
   * true.
   * 
   * @param b
   *          connect on create
   */
  public void setConnectOnCreate(final boolean b) {
    this.connectOnCreate = b;
  }

  /** {@inheritDoc}. */
  public LdappcLdap create() {
    LdappcLdap l = new LdappcLdap(this.config);

    if (this.connectOnCreate) {
      try {
        l.connect();
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("unabled to connect to the ldap", e);
        }
        l = null;
      }
    }
    return l;
  }

  /** {@inheritDoc}. */
  public void destroy(final Ldap l) {
    l.close();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("destroyed ldap object: " + l);
    }
  }
}