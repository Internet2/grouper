package edu.internet2.middleware.grouper.app.workflow;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * <p>Allowlist sanitizer for workflow form HTML.</p>
 *
 * <p>A workflow editor (a member of workflow.editorsGroup, not necessarily wheel) can supply the
 * raw HTML of a workflow form (workflowConfigForm). That HTML is shown to everyone who initiates
 * the workflow, and a copy of it (the instance snapshot) is shown to every approver, inside the
 * Grouper UI origin. Without sanitizing, an editor could put script or event handlers in the
 * form and run JavaScript in a wheel approver's session.</p>
 *
 * <p>This class keeps the structure a form needs (text formatting, tables, lists, links, labels
 * and the form controls whose names match the workflow params) and drops everything else:
 * script, style, iframe, object, embed, svg, math, img, form, button, every on* attribute,
 * style attributes, srcdoc, formaction, and links that are not http, https or mailto.</p>
 *
 * <p>Two uses:</p>
 * <ul>
 *   <li>{@link #sanitize(String)} at render time (WorkflowContainer.getHtmlForm), so configs and
 *   instance snapshots stored before the fix are also safe.</li>
 *   <li>{@link #isSafe(String)} at save time (GrouperWorkflowConfigValidator), so an editor gets an
 *   error instead of a form that is silently changed when shown.</li>
 * </ul>
 *
 * <p>Only uses jsoup Safelist / Cleaner APIs that exist from jsoup 1.14.1 on (Grouper 5.x ships
 * 1.15.3, 6.x 1.21.1, 7.x 1.23.x), so the same class works in every supported release.</p>
 */
public class GrouperWorkflowHtmlSanitizer {

  /**
   * the allowlist, built once (Safelist is not changed after this, so it is safe to share)
   */
  private static final Safelist SAFELIST = buildSafelist();

  /**
   * no instances
   */
  private GrouperWorkflowHtmlSanitizer() {
  }

  /**
   * build the allowlist for workflow forms
   * @return the safelist
   */
  private static Safelist buildSafelist() {

    // relaxed: text formatting, headings, lists, tables, div/span, a, img, blockquote, etc.
    Safelist safelist = Safelist.relaxed();

    // no images: an image on an approver page is a beacon (who opened it, when) and is not
    // needed for a form
    safelist.removeTags("img");

    // links: only http, https and mailto (relaxed also allows ftp); no javascript: or data:
    safelist.removeProtocols("a", "href", "ftp");

    // form controls used by workflow params (input, textarea), plus a few harmless helpers.
    // No form, button, or formaction / formmethod / formtarget attributes, so the snippet
    // cannot change where the surrounding Grouper form posts.  No input src (type=image).
    safelist.addTags("input", "textarea", "select", "option", "optgroup", "label", "fieldset", "legend");
    safelist.addAttributes("input", "type", "name", "value", "checked", "disabled", "readonly",
        "placeholder", "size", "maxlength");
    safelist.addAttributes("textarea", "name", "rows", "cols", "disabled", "readonly", "placeholder",
        "maxlength");
    safelist.addAttributes("select", "name", "disabled", "multiple", "size");
    safelist.addAttributes("option", "value", "selected", "disabled", "label");
    safelist.addAttributes("optgroup", "label", "disabled");
    safelist.addAttributes("label", "for");

    // id is needed for label for= (the default form uses ids), class for layout.
    // No style attribute: CSS cannot run script, but it can position content over the
    // Grouper page (fake buttons or links), so it is not allowed in an editor supplied form.
    safelist.addAttributes(":all", "id", "class", "title");

    return safelist;
  }

  /**
   * remove everything that is not on the allowlist.  The result is body HTML (no html, head or
   * body tags), which is what the workflow JSPs embed.  Null or blank passes through.
   * @param html form html (a config form, or a stored instance snapshot)
   * @return safe html
   */
  public static String sanitize(String html) {
    if (html == null || html.trim().length() == 0) {
      return html;
    }
    // keep the markup as written (no pretty print), so textarea values and spacing do not change
    Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
    return Jsoup.clean(html, "", SAFELIST, outputSettings);
  }

  /**
   * is this html already safe, i.e. would sanitize() remove nothing.  Unlike Jsoup.isValid this
   * does not fail on harmless parse errors (e.g. an end tag like &lt;/input&gt;, which Grouper's
   * own generated forms have).  Null or blank is safe.
   * @param html form html
   * @return true if nothing would be removed
   */
  public static boolean isSafe(String html) {
    if (html == null || html.trim().length() == 0) {
      return true;
    }
    Document dirty = Jsoup.parseBodyFragment(html, "");
    return new Cleaner(SAFELIST).isValid(dirty);
  }

}
