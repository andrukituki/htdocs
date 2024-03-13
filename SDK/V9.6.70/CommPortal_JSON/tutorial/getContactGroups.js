/*
* Fetch the contact group information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_ContactGroups", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved contact group information");
  display("There are ", data.Group.length, " contact groups:");
  var group_names = get_group_names(data);
  // Display the number of contacts in each group.
  for (var i = 0; i < group_names.length; i++) {
    display(group_names[i], " contains ", get_number_of_contacts(data, i), " contacts");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve contact group information: ");
  errorHandler(connection, error);
}

// Pick out the group names from the data.
function get_group_names(data) {
  var group_names = [];
  for (var i = 0; i < data.Group.length; i++) {
    group_names.push(data.Group[i].Description);
  }
  return group_names;
}

// Get the number of contacts for group number i.
function get_number_of_contacts(data, i) {
  var number_of_contacts;
  try {
    number_of_contacts = data.Group[i].MembersUID.length;
  } catch(e) {
    number_of_contacts = 0;
  }
  return number_of_contacts;
}
