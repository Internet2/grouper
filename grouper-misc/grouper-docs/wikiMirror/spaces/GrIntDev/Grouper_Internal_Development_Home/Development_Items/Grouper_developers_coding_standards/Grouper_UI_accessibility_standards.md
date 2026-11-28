---
title: "Grouper UI accessibility standards"
space: GrIntDev
pageId: 303038468
version: 1
lastUpdated: 2026-10-15T19:37:36.915Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/303038468/Grouper+UI+accessibility+standards
---

Accessibility rules for anyone changing the Grouper UI (grouperUi2 JSPs, grouperUi.js, grouperUi2.css, and externalized text). The target is WCAG 2.2 level AA. These rules are part of the [Grouper developers coding standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards). Most of them came out of audits, so following them keeps fixed issues from coming back. The 2016 audit history is on [Grouper accessibility](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793113/Grouper+accessibility).

 

## Buttons, links and menus

 

- Use `<button type="button">` for anything that runs an action or ajax. Use `<a href>` only to navigate to a new URL. Never use `<a href="#" onclick=...>` as a fake button (WCAG 4.1.2).
- To make a button look like a link, use class `grouper-linkbutton`. For an action inside a dropdown menu, use class `grouper-menuitem`. For a normal button, use the Bootstrap `btn` classes.
- Dropdown toggles are `<button>` elements with `aria-haspopup="true"` and `aria-expanded`, and the onclick keeps `aria-expanded` in sync. Do not put `role="menu"` on these lists, because WAVE flags them as broken ARIA menus.
- Every id on a screen must be unique, including ids from included JSPs. Every `aria-controls` must point to exactly one element.

 

## Labels, text and icons

 

- Every form control has a `<label for>` whose value matches the control's id. If the layout has no visible label, add `aria-label="${textContainer.textEscapeXml['someKey']}"`.
- Externalize all visible and screen-reader text, including aria-labels, alt text and visually-hidden text.
- Decorative Font Awesome icons get `aria-hidden="true"`, for example `<i aria-hidden="true" class="fa fa-folder"></i>`. Icon-only buttons need an aria-label.
- Decorative images use `alt=""`. Meaningful images get alt text that describes their purpose, for example "Grouper home" on the logo link.
- Do not put HTML markup in `title` attributes, because screen readers read the tags out loud.
- To give screen readers text that is not shown on screen, use class `visually-hidden`.

 

## Page structure

 

- Every screen sets a unique, descriptive page title with `${grouper:title('key')}` or `${grouper:titleFromKeyAndText('key', objectName)}`.
- Every screen has exactly one `<h1>`. If it should look smaller, use class `grouper-heading-as-h4`. Do not skip heading levels.
- Page shells include `assetsJsp/skipLink.jsp` as the first element in body. The content area is `<main id="grouperMainContentDivId" tabindex="-1">`. Ajax replaces only the inner HTML of main, so do not add another main element or replace that element itself.
- Screens have breadcrumbs. Use the standard `breadcrumbs` property of the Gui object (GuiObjectBase), which adds the `guiBreadcrumbsAriaLabel` label. Do not hand-build breadcrumbs.

 

## Dynamic content and ajax

 

- Show success and error messages with `GuiScreenAction.newMessage()`. That renders a `role="alert"` message and moves focus to it. Do not build custom message divs.
- The blockUI loading spinner is a `role="status"` region with hidden "Loading" text. Do not replace it with a bare image.
- When an ajax call redraws a form, put focus back on the field that triggered it. One way is to pass `focusOnElementName=fieldName` and have the server add a focus script (see GRP-3074).
- When new content appears (for example the Add members panel or a filtered list), move focus to it or announce it, so screen-reader users know something changed.
- Use the standard `rel="tooltip"` tooltips. grouperUi.js makes their triggers keyboard-focusable and links them with `aria-describedby`. Information must not be available only on mouse hover.

 

## Color, focus and size

 

- Contrast must be at least 4.5:1 for text and 3:1 for UI components, form-field borders and focus indicators. The link color is #0076a5 and the brand teal is #1c6070, and both pass on white.
- Never remove the keyboard focus outline. The global focus ring is defined in grouperUi2.css.
- Icon-only controls need a click area of at least 24x24 CSS pixels (WCAG 2.5.8). Use class `grouper-icon-btn`, which enlarges the hit area without changing the layout.
- Put UI styles in grouperUi2.css and bump its `?updated=` cache-buster in the JSPs that link it.

 

## Keyboard

 

- Every action can be reached and run with the keyboard alone, and the tab order follows the visual order.
- Dropdowns and modals can be opened, used and closed with the keyboard. When a modal closes, focus returns to the control that opened it. Modal close buttons are real buttons that assistive technology can reach.

 

## Time limits

 The UI session times out after the Tomcat idle timeout, which the container sets to 600 minutes by default with GROUPER_TOMCAT_SESSION_TIMEOUT_MINUTES. There is no warning before it expires, so this does not yet meet WCAG 2.2.1 Timing Adjustable. New features must not add their own time limits unless the user is warned first and can extend the time.

 

## Pull request checklist

 

- Run WAVE or axe on each changed screen, with no new errors.
- Do a keyboard-only walkthrough: reach and run every new control, and confirm that focus is visible and lands somewhere sensible after ajax.
- Do a quick screen-reader check (NVDA or VoiceOver) of new controls and messages.
- Check the contrast of any new colors.
- Confirm that new text is externalized and new ids are unique.

 

## Reference issues

 These GRP issues set the patterns above: GRP-7056 (search landmark), GRP-7059 (dropdown ARIA), GRP-7066 (form labels), GRP-7087 (page titles), GRP-7091 (focus ring), GRP-7094 and GRP-7188 (modal close), GRP-7096 (tooltips), GRP-7267 (skip link and main), GRP-7268 (title attributes), GRP-7299 and GRP-7301 (unique ids), GRP-7302 (field and focus contrast), GRP-7305 (h1), GRP-7306, GRP-7307 and GRP-7315 (native buttons), GRP-7309 (loading status), GRP-7313 (aria-hidden icons), GRP-7314 (target size), GRP-7463 (alert contrast).
