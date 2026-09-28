<%--
  Skip link (WCAG 2.4.1 Bypass Blocks).

  This must be included as the FIRST thing inside <body> on every page shell, ahead of
  the environment banner, the logo and the navbar, so that it is the first focusable
  element on the page.  It is positioned off screen until it receives keyboard focus,
  at which point it appears pinned to the top left corner (see .grouper-skip-link in
  grouperUi2.css).

  It targets the main landmark, which carries id="grouperMainContentDivId" in every
  shell.  That element also needs tabindex="-1" so that activating this link actually
  moves focus into it rather than only scrolling to it.
--%>
<a href="#grouperMainContentDivId" class="grouper-skip-link">${textContainer.text['guiSkipToMainContent']}</a>
