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
    var successMessage = '<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>[group|subject|folder]</strong> has been been added to My Favorites.</div>';
    $('#messaging').hide().empty().append(successMessage).slideDown('slow');
    $('#messaging').focus();
  });

  $('.remove-from-favorites').click(function() {
    var successMessage = '<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>[group|subject|folder]</strong> has been been removed from My Favorites.</div>';
    $('#messaging').hide().empty().append(successMessage).slideDown('slow');
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
      var successMessage = '<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>' + newMember + '</strong> has been added as a member of this group.</div>';
      $('#messaging').hide().empty().append(successMessage).slideDown('slow');
    }
    return false;
  });
  // Show confirmation message after revoking a membership
  $('.actions-revoke-membership').click(function() {
    var successMessage = '<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>[Entity name]</strong> has been removed from this group.</div>';
    $('#messaging').hide().empty().append(successMessage).slideDown('slow');
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
