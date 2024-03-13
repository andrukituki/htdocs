/*
* Add a contact to a contacts group.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_ContactGroups", fetchSuccess, fetchError);

var groupdata;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved contact groups");
  groupdata = data;
  if (data.Group.length > 0) {
    // Call the SDK function to fetch the list of contacts.
    connection.fetchContacts(contactSuccess, contactError);
  } else {
    display("There are no contact groups");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve mailbox settings: ");
  errorHandler(connection, error);
}

function contactSuccess(connection, contacts) {
  groupdata.Group[0].MembersUID.push( {"_" : contacts[0].uid } );

  // Save the data to the server.
  // When we save data for a contact group, we have to specify
  // the contact group in the service indication name.
  connection.saveData("Meta_Subscriber_MetaSphere_ContactGroups?Group.UniqueID=" + groupdata.Group[0].UniqueID._,
                      groupdata, 
                      saveSuccess, 
                      saveError);
}

function contactError(connection, error) {
  display("Error fetching contacts");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully added new contact to group");
}

function saveError(connection, error) {
  display("Error adding contact to group");
  errorHandler(connection, error);
}


