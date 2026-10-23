/**
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.util;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * renders untrusted markdown, e.g. an AI model's answer, as HTML which is safe to put on a page.
 *
 * <p>two layers: flexmark escapes any HTML written in the markdown, and the HTML it renders is
 * then cleaned with an allowlist of formatting tags.  images are not allowed, since loading one
 * would send a request to wherever the model (or data a prompt injection put in front of it)
 * pointed, without the user doing anything.  links are only kept for http, https and mailto,
 * open in a new tab, and do not send the referrer.</p>
 */
public class GrouperUiMarkdown {

  /** parser, thread safe */
  private static final Parser PARSER;

  /** renderer, thread safe */
  private static final HtmlRenderer RENDERER;

  /** what the rendered HTML may contain */
  private static final Safelist SAFELIST;

  static {
    MutableDataSet options = new MutableDataSet();
    options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
    options.set(HtmlRenderer.ESCAPE_HTML, true);
    options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
    PARSER = Parser.builder(options).build();
    RENDERER = HtmlRenderer.builder(options).build();

    // basic: a, b, blockquote, br, code, em, i, li, ol, p, pre, strong, ul, etc, with links
    // limited to http, https, ftp and mailto
    SAFELIST = Safelist.basic()
        .addTags("h1", "h2", "h3", "h4", "h5", "h6", "hr", "del",
            "table", "thead", "tbody", "tr", "th", "td")
        .addAttributes("th", "align")
        .addAttributes("td", "align")
        .removeProtocols("a", "href", "ftp")
        .addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow")
        .addEnforcedAttribute("a", "target", "_blank")
        .addEnforcedAttribute("table", "class", "table table-condensed table-bordered");
  }

  /**
   * @param markdown untrusted markdown, may be null
   * @return safe HTML, empty if the markdown is blank
   */
  public static String toSafeHtml(String markdown) {
    if (StringUtils.isBlank(markdown)) {
      return "";
    }
    String html = RENDERER.render(PARSER.parse(markdown));
    return Jsoup.clean(html, SAFELIST);
  }

}
