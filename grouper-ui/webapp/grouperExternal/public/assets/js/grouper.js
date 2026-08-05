$(document).ready(function() {

  // Initialize footables
  $('.footable').footable({
    breakpoints: {
      medium: 600
    },
    toggleSelector: ' > tbody > tr:not(.footable-row-detail) > td.foo-clicker'
  });

  // Toggle the Favorites star
  $('.favorite').click(function() {
    $(this).toggleClass('icon-star');
    $(this).toggleClass('icon-star-empty');
  });

  // Show confirm message when added to favorites
  $('.add-to-my-favorites').click(function() {
    var successMessage = '<div class="alert alert-success" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close">&times;</button><strong>[group|subject|folder]</strong> has been been added to My Favorites.</div>';
    $('#messaging').hide().empty().append('<span class="messageText">').append(successMessage).append('</span>').slideDown('slow');
    $('#messaging').focus();
  });

  $('.remove-from-favorites').click(function() {
    var successMessage = '<div class="alert alert-success" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close">&times;</button><strong>[group|subject|folder]</strong> has been been removed from My Favorites.</div>';
    $('#messaging').hide().empty().append('<span class="messageText">').append(successMessage).append('</span>').slideDown('slow');
    $('#messaging').focus();
  });

  //Show the add members well
  $('#show-add-block').click(function() {
    $('#add-block-container').slideDown();
    $('#add-block-input').focus();
  });

  //Toggle collapsible group details
  $('#group-details').on('shown', function() {
    $('#toggle-group-details').text('Less');
    $('#toggle-group-details').append('&nbsp;<i class="icon-angle-up"></i>');
  });
  $('#group-details').on('hidden', function() {
    $('#toggle-group-details').text('More');
    $('#toggle-group-details').append('&nbsp;<i class="icon-angle-down"></i>');
  });

  //Toggle collapsible advanced properties
  $('#advanced-properties').on('shown', function() {
    $('#toggle-advanced-properties').text('Hide advanced properties');
    $('#toggle-advanced-properties').append('&nbsp;<i class="icon-angle-up"></i>');
  });
  $('#advanced-properties').on('hidden', function() {
    $('#toggle-advanced-properties').text('Show advanced properties');
    $('#toggle-advanced-properties').append('&nbsp;<i class="icon-angle-down"></i>');
  });

  // Show the autocomplete box when you enter the add member input
  $('#add-members, #add-groups').keydown(function() {
    $('#autocomplete').show();
  });
  $('#add-members, #add-groups').focusout(function() {
    $('#autocomplete').hide();
  });
  // Show confirmation message after adding a member
  $('#add-members-button').click(function() {
    $('#add-members-confirmation').show();
  });

  // Show custom privileges when radio option is selected
  $('input[name="privilege-options"]').change(function() {
    if ($(this).val() === 'custom') {
      $('#add-members-privileges').slideDown('fast');
    } else {
      $('#add-members-privileges').slideUp('fast');
    }
  });

  // Activate tooltips
  $('.top-container').tooltip({
    selector: "a[rel=tooltip],span[rel=tooltip]"
  });

  // Accessibility for rel=tooltip triggers.
  // Bootstrap shows these tooltips on 'hover focus', but a <span> cannot receive
  // keyboard focus, so keyboard-only and screen-reader users never see them (WAVE
  // does not catch this - it is a keyboard/focus gap, not a static markup error).
  // This mirrors the a11y treatment already applied to the grouperTooltip()/Tip()
  // path in grouperUi.js:
  //   1) give non-focusable triggers tabindex=0 so the tooltip is reachable by Tab, and
  //   2) when shown, mark the tooltip role=tooltip and link it to its trigger via
  //      aria-describedby so screen readers announce it (Bootstrap 2 does not do this).
  function grouperTooltipA11yMakeFocusable() {
    $('.top-container').find('a[rel=tooltip],span[rel=tooltip]').each(function () {
      var $el = $(this);
      // links/inputs are already focusable; only the plain spans need tabindex
      if (!$el.is('a,button,input,select,textarea') && $el.attr('tabindex') == null) {
        $el.attr('tabindex', '0');
      }
    });
  }
  grouperTooltipA11yMakeFocusable();
  // Re-apply to content added later by ajax (new rows, reloaded panels, etc.).
  $(document).ajaxComplete(function () {
    grouperTooltipA11yMakeFocusable();
  });

  // Link trigger <-> tooltip for screen readers while the tooltip is visible.
  $('.top-container').on('shown.grouperTooltipA11y', 'a[rel=tooltip],span[rel=tooltip]', function () {
    try {
      var data = $(this).data('tooltip');
      var $tip = (data && data.tip) ? data.tip() : null;
      if ($tip && $tip.length) {
        var tipId = $tip.attr('id');
        if (!tipId) {
          tipId = 'grouperTooltip_' + (new Date().getTime()) + '_' + Math.floor(Math.random() * 100000);
          $tip.attr('id', tipId);
        }
        if (!$tip.attr('role')) {
          $tip.attr('role', 'tooltip');
        }
        $(this).attr('aria-describedby', tipId);
      }
    } catch (e) {
      // ignore
    }
  });
  $('.top-container').on('hidden.grouperTooltipA11y', 'a[rel=tooltip],span[rel=tooltip]', function () {
    $(this).removeAttr('aria-describedby');
  });

  // On this Bootstrap (2.2.x) rel=tooltip is initialized hover-only, so keyboard focus
  // never shows the tooltip even though the trigger already has tabindex. Show it on
  // focus and hide it on blur. aria-describedby is set synchronously here because the
  // shown/hidden handlers above only fire after Bootstrap's async fade, which would
  // race the screen reader on focus. grouperTooltipA11ySetDescribedby is declared below
  // (function declarations are hoisted within this ready() callback).
  $('.top-container').on('focus.grouperRelTooltipA11yFocus', 'a[rel=tooltip],span[rel=tooltip]', function () {
    $(this).tooltip('show');
    grouperTooltipA11ySetDescribedby($(this));
  });
  $('.top-container').on('blur.grouperRelTooltipA11yFocus', 'a[rel=tooltip],span[rel=tooltip]', function () {
    $(this).tooltip('hide');
    $(this).removeAttr('aria-describedby');
  });

  // Accessibility for the legacy hover tooltips wired through inline handlers:
  //   onmouseover="grouperTooltip('...')" onmouseout="UnTip()"   (detail-row labels like
  //     Name:, Path:, ID path:, ... and privilege column headers), and
  //   onmouseover="Tip('...')" onmouseout="UnTip()"              (permission page
  //     allow/disallow icons - note Tip() is not even defined, so these never worked).
  // Both were mouse-only, so keyboard and screen-reader users could never reach the help
  // text (WAVE does not catch this - it is a keyboard/focus gap, not a static markup error).
  // Convert each to a single Bootstrap tooltip shown on BOTH mouse hover and keyboard
  // focus, and expose it to screen readers via aria-describedby. We use trigger:'manual'
  // plus our own handlers because this Bootstrap (2.2.x) does not support a combined
  // 'hover focus' trigger; one manual instance per element is reused, so no stale
  // tooltip nodes accumulate. Same visual as before (top placement, html, on body).
  function grouperTooltipA11ySetDescribedby($el) {
    var data = $el.data('tooltip');
    var $tip = (data && data.tip) ? data.tip() : null;
    if ($tip && $tip.length) {
      var tipId = $tip.attr('id');
      if (!tipId) {
        tipId = 'grouperTooltip_' + (new Date().getTime()) + '_' + Math.floor(Math.random() * 100000);
        $tip.attr('id', tipId);
      }
      if (!$tip.attr('role')) { $tip.attr('role', 'tooltip'); }
      $el.attr('aria-describedby', tipId);
    }
  }
  function grouperClassTooltipA11yInit() {
    $('[onmouseover]').each(function () {
      var $el = $(this);
      var match = ($el.attr('onmouseover') || '').match(/(?:grouperTooltip|Tip)\('([\s\S]*)'\)\s*;?\s*$/);
      if (!match) { return; }  // not a grouperTooltip()/Tip() trigger
      // grouperTooltip() messages are HTML-entity escaped (rendered with html:true below);
      // Tip() messages are javascript-escaped, so undo the backslash escaping of quotes.
      var message = match[1].replace(/\\'/g, "'").replace(/\\"/g, '"');
      // Drop the mouse-only inline handlers so grouperTooltip()/UnTip() no longer run for
      // this element (avoids a duplicate tooltip). Removing onmouseover also makes this
      // pass idempotent - a converted element no longer matches the selector.
      $el.removeAttr('onmouseover');
      if (/UnTip\(\)/.test($el.attr('onmouseout') || '')) { $el.removeAttr('onmouseout'); }
      // plain spans are not keyboard-focusable by default
      if (!$el.is('a,button,input,select,textarea') && $el.attr('tabindex') == null) {
        $el.attr('tabindex', '0');
      }
      $el.tooltip({ trigger: 'manual', html: true, container: 'body', placement: 'top', title: message });
      $el.on('mouseenter.grouperClassTooltipA11y focus.grouperClassTooltipA11y', function () {
        $(this).tooltip('show');
        grouperTooltipA11ySetDescribedby($(this));
      });
      $el.on('mouseleave.grouperClassTooltipA11y blur.grouperClassTooltipA11y', function () {
        $(this).tooltip('hide');
        $(this).removeAttr('aria-describedby');
      });
    });
  }
  grouperClassTooltipA11yInit();
  // Convert content added later by ajax (reloaded panels, new rows, etc.).
  $(document).ajaxComplete(function () {
    grouperClassTooltipA11yInit();
  });

  // Show/hide bulk add options
  $('input[name="bulk-add-options"]').change(function() {
    if ($(this).val() === 'input') {
      $('.bulk-add-import-container').slideUp('fast');
      $('.bulk-add-list-container').slideUp('fast');
      $('.bulk-add-input-container').slideDown('fast');
    } else if ($(this).val() === 'import') {
      $('.bulk-add-input-container').slideUp('fast');
      $('.bulk-add-list-container').slideUp('fast');
      $('.bulk-add-import-container').slideDown('fast');
    } else if ($(this).val() === 'list') {
      $('.bulk-add-input-container').slideUp('fast');
      $('.bulk-add-import-container').slideUp('fast');
      $('.bulk-add-list-container').slideDown('fast');
    }
  });

  // Add another bulk add input
  $('.bulk-add-another').click(function() {
    $('#bulk-block').clone().insertAfter('.bulk-add-block:last').show();
    return false;
  });
  $('.bulk-add-another-group').click(function() {
    $('#bulk-group-block').clone().insertAfter('.bulk-add-group-block:last').show();
    return false;
  });
  // Remove bulk add rows
  $('body').delegate('.bulk-block-remove','click',function() {
    $(this).closest('.bulk-add-block').remove();
  });
  $('body').delegate('.bulk-group-block-remove','click',function() {
    $(this).closest('.bulk-add-group-block').remove();
  });

  // Show/hide external invite options
  $('input[name="external-invite-options"]').change(function() {
    if ($(this).val() === 'email') {
      $('.invite-external-id-container').slideUp('fast');
      $('.invite-external-email-container').slideDown('fast');
    } else if ($(this).val() === 'id') {
      $('.invite-external-email-container').slideUp('fast');
      $('.invite-external-id-container').slideDown('fast');
    }
  });

  $('.btn.assign').click(function() {
    window.confirm('Are you sure you want to assign this privilege?');
    $(this).replaceWith('<i class="icon-ok icon-direct"></i>');
  });

  $('.btn.remove').click(function() {
    window.confirm('Are you sure you want to remove this privilege?');
    $(this).siblings().remove();
  });

  // Cancel buttons should always go back to the previous page
  $('.btn-cancel').click(function() {
    history.go(-1);
    return false;
  });

  // Set a max height for the explore tree based on the user's viewport size
  $('#tree1').css('max-height',function() {
    var viewportHeight = document.documentElement.clientHeight;
    var maxHeight = viewportHeight - 300;
    return maxHeight;
  });

  // Show confirmation message after adding a new user
  $('#add-members-form').submit(function() {
    var newMember = $('#add-block-input').val();
    if (newMember.length) {
      var successMessage = '<div class="alert alert-success" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close">&times;</button><strong>' + newMember + '</strong> has been added as a member of this group.</div>';
      $('#messaging').hide().empty().append('<span class="messageText">').append(successMessage).append('</span>').slideDown('slow');
    }
    return false;
  });
  // Show confirmation message after revoking a membership
  $('.actions-revoke-membership').click(function() {
    var successMessage = '<div class="alert alert-success" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close">&times;</button><strong>[Entity name]</strong> has been removed from this group.</div>';
    $('#messaging').hide().empty().append('<span class="messageText">').append(successMessage).append('</span>').slideDown('slow');
  });

  //Check to see if notes and nav should be hidden
  var develNotesHide = $.cookie('devel_notes_hide');

  // If not hidden, then show them
  if (develNotesHide === 'no') {
    $('.devel-notes-show').hide();
    var viewportHeight = $(window).height();
    var topHeight = (viewportHeight * 0.65);
    $('.devel-notes').css('height',(viewportHeight * 0.35));
    $('.top-container').css({height: topHeight, overflow: 'scroll'});
    $('.devel-notes').show();
    $('.devel-notes-hide').show();
    $.cookie('devel_notes_hide', 'no');
  }

  // Show developer notes
  $('.devel-notes-show').click(function() {
    $(this).hide();
    var viewportHeight = $(window).height();
    var topHeight = (viewportHeight * 0.65);
    $('.devel-notes').css('height',(viewportHeight * 0.35));
    $('.top-container').css({height: topHeight, overflow: 'scroll'});
    $('.devel-notes').show();
    $('.devel-notes-hide').show();
    $.cookie('devel_notes_hide', 'no');
  });
  // Hide developer notes
  $('.devel-notes-hide').click(function() {
    $(this).hide();
    $('.devel-notes-show').show();
    $('.devel-notes').hide();
    $('.top-container').css({height:'auto', overflow: 'visible'});
    $.cookie('devel_notes_hide', 'yes');
  });

  var data = [
    { label: 'Root',
      id: 'root',
      children: [
        {
            label: 'Applications',
            id: 'applications',
            children: [
                { label: 'Directories' },
                { label: 'Service Q' },
                { label: 'Virtual Private Network' },
                { label: 'Wiki', id: 'wiki' },
                { label: 'Wordpress' }
            ]
        },
        {
            label: 'Departments',
            id: 'departments',
            children: [
                { label: 'Central Administration' },
                { label: 'Financial Services' },
                { label: 'Information Technology' }
            ]
        },
        {
            label: 'Reference Groups',
            id: 'reference',
            children: [
                { label: 'Academic Staff' },
                { label: 'Administrative Staff' },
                { label: 'Faculty' },
                { label: 'Students' }
            ]
        }
      ]
    }
  ];

  // Create Tree
  var $tree = $('#tree1');
  $tree.tree({
      data: data,
      selectable: false
  });


  // Get this node for use in the switch statement
  var applicationsNode = $tree.tree('getNodeById','applications');
  var rootNode = $tree.tree('getNodeById','root');
  $tree.tree('openNode',rootNode,false);

  // Add a highlight class if we are on a certain page
  var href = $(location).attr('href');
  var currentPage = href.substr(href.lastIndexOf('/') + 1);
  switch (currentPage) {
    case 'view-folder.html':
    case 'view-folder-privileges.html':
    case 'view-group.html':
    case 'view-group-privileges.html':
    case 'view-group-membership.html':
    case 'view-group-group-privileges.html':
      var wikiNode = $tree.tree('getNodeById','wiki');
      $tree.tree('selectNode',wikiNode);
      // Automatically open the applications folder
      $tree.tree('openNode',applicationsNode,false);
      break;
    case 'view-folder-applications.html':
      $tree.tree('selectNode',applicationsNode);
      $tree.tree('openNode',applicationsNode,false);
      break;
  }

  // Send user to certain pages based on where they click
  $('#tree1').bind(
      'tree.select',
      function(event) {
          var node = event.node;
          if (node.name === 'Wiki') {
            window.location = 'view-folder.html';
          }
          else if (node.name === 'Applications') {
            window.location = 'view-folder-applications.html';
          }
      }
  );

});
