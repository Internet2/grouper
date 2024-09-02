package edu.internet2.middleware.grouper.app.browser;

/**
 * This is the programmatic browser class.
 * @param <T>
 */
public abstract class GrouperUiBrowser {

  /**
   * Field that represents which page the programmatic browser is interacting with.
   */
  private GrouperPage grouperPage;

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @return the page
   */
  public GrouperPage getGrouperPage() {
    return grouperPage;
  }

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @param page is the page to be interacted with
   */
  public GrouperUiBrowser(GrouperPage grouperPage) {
    super();
    this.grouperPage = grouperPage;
  }

}
