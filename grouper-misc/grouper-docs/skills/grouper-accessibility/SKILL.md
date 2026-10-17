---
name: grouper-accessibility
description: Apply and review Grouper UI accessibility (WCAG 2.2 AA) standards. Use this skill whenever writing or changing Grouper UI code (grouperUi2 JSPs, grouperUi.js, grouperUi2.css, UI externalized text, GuiScreenAction / UiV2* response code), when reviewing a UI diff or PR, or when the user mentions accessibility, a11y, WCAG, 508, WAVE, axe, screen reader, keyboard navigation, focus, contrast, aria, or an accessibility audit finding against Grouper. Also trigger on "is this accessible", "fix the WAVE errors", "accessibility review", or a GRP issue about accessibility.
---

# Grouper UI accessibility

Target: **WCAG 2.2 level AA** for the Grouper UI. The canonical human-readable
standard is the GrIntDev wiki page "Grouper UI accessibility standards" (child of
"Grouper developers coding standards"). If you can reach the wiki (see the
grouper-wiki-edit skill for read access), read it fresh, because it can change.
The rules below are the same standard written as instructions for an assistant.

Most of these rules came from audits (2016 CU Boulder, 2023 Unicon, 2026 WAVE
sweep GRP-7056..GRP-7463). Regressions are the main risk, so when you touch a
screen, leave it at least as accessible as you found it.

## When writing UI code

Buttons, links and menus
- Actions and ajax use `<button type="button">`. Use `<a href>` only to navigate
  to a real URL. Never use `<a href="#" onclick=...>` (WCAG 4.1.2).
- If it should look like a link, use `class="grouper-linkbutton"`. For an action
  inside a dropdown menu, use `class="grouper-menuitem"`. Otherwise use the
  Bootstrap `btn` classes.
- Dropdown toggles are buttons with `aria-haspopup="true"` and `aria-expanded`
  kept in sync by the onclick. No `role="menu"` (WAVE "broken ARIA menu",
  GRP-7059).
- Ids are unique per rendered screen, including ids from included JSPs. Each
  `aria-controls` resolves to exactly one element. Browser tests click some
  elements by id, so before renaming an id, grep the tests (e.g.
  GrouperUiBrowser*) and keep the id the tests use.

Labels, text and icons
- Every control has a `<label for>` whose value matches its id. If the layout
  has no visible label, add
  `aria-label="${textContainer.textEscapeXml['key']}"`. Inside attributes use
  textEscapeXml, not text.
- Externalize all text, including aria-labels, alt text and visually-hidden
  text. Add new keys to the en_us base text properties.
- Decorative Font Awesome icons: `<i aria-hidden="true" class="fa ...">`.
  Icon-only buttons need an aria-label.
- Decorative images use `alt=""`. Meaningful images get alt text that says
  their purpose.
- No HTML markup in `title` attributes.
- Screen-reader-only text uses `class="visually-hidden"`.

Page structure
- Each screen sets a unique title with `${grouper:title('key')}` or
  `${grouper:titleFromKeyAndText('key', name)}`.
- Each screen has exactly one `<h1>`. To make it look smaller, use
  `class="grouper-heading-as-h4"`. Do not skip heading levels.
- Page shells include `assetsJsp/skipLink.jsp` first in body, and the content
  area is `<main id="grouperMainContentDivId" tabindex="-1">`. Ajax replaces
  only the inner HTML of that element. Never add a second `<main>` or replace
  the element itself.
- Breadcrumbs come from the Gui object's `breadcrumbs` property
  (GuiObjectBase), which adds the aria-label. Do not hand-build breadcrumbs.

Dynamic content and ajax
- Show messages with `GuiScreenAction.newMessage(...)`. It renders
  `role="alert"` and focuses `#messaging`. Never build custom message divs.
- Keep the blockUI spinner as the `role="status"` region with hidden "Loading"
  text.
- When ajax redraws a form, refocus the field that triggered it: pass
  `focusOnElementName=<name>` and add a `GuiScreenAction.newScript` that focuses
  the field (pattern from GRP-3074, UiV2ProvisionerConfiguration).
- When new content appears (panels, filtered lists, added sections), move focus
  to it or announce it.
- Tooltips use the standard `rel="tooltip"`. grouperUi.js makes the triggers
  focusable and adds `aria-describedby`. Information must never be available
  only on mouse hover.

Color, focus and size
- Contrast must be at least 4.5:1 for text and 3:1 for UI components, field
  borders and focus rings. The link color is #0076a5 and the teal is #1c6070.
- Never remove the focus outline. The global focus ring is in grouperUi2.css.
- Icon-only controls need a hit area of at least 24x24 CSS px (WCAG 2.5.8). Use
  `class="grouper-icon-btn"`.
- Styles go in grouperUi2.css (the only live v2 stylesheet). When you edit it,
  bump the `?updated=` cache-buster in the JSPs that link it.

Keyboard and time
- Every action works with the keyboard alone, and the tab order follows the
  visual order. Modals return focus to the control that opened them on close,
  and their close button is a real button.
- Do not add new time limits unless the user is warned first and can extend the
  time (WCAG 2.2.1). The existing session idle timeout (Tomcat, default 600 min,
  no warning) is a known gap.

## When reviewing a UI diff

1. Collect the changed UI files: `git diff` / `git diff --staged` limited to
   `grouper-ui/webapp/**` (JSP, JS, CSS) and UI text properties, plus UiV2*
   Java that builds markup or GuiScreenActions.
2. Check each change against the rules above. Grep the diff for these red
   flags:
   - `href="#"` together with onclick
   - `role="menu"`
   - `<i class="fa` without aria-hidden
   - `<img` without alt
   - `<input`, `<select` or `<textarea` without a matching label or aria-label
   - hard-coded English in markup
   - duplicate `id=` values
   - `title="` containing `<`
   - `outline: none`
   - new colors
   - custom message divs
   - a new screen without `grouper:title` or an `<h1>`
3. Report the findings, most severe first, each with file:line, the WCAG success
   criterion, and the concrete fix (use the house classes and helpers above).
   Do not report style nits that are not accessibility issues.
4. If you can run the UI (see the run / built-in browser skills), do a
   keyboard-only pass on the changed screen and run WAVE or axe. Otherwise, list
   those checks for the human to run.

## Pull request checklist (give this to the human)

- Run WAVE or axe on each changed screen, with no new errors.
- Keyboard-only walkthrough: every new control is reachable and works, focus is
  visible, and focus lands somewhere sensible after ajax.
- Quick screen-reader check (NVDA or VoiceOver) of new controls and messages.
- Contrast check of any new colors.
- New text is externalized and new ids are unique.

## Tracking

File accessibility work as GRP issues (grouper-jira skill), with "accessibility"
in the summary and the WCAG criterion in the description, so the issues can be
found later.
