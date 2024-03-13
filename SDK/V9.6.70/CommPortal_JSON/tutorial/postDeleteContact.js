/*
* Fetch the list of contacts, search for a contact with nickname
* "JonnyD", and delete him.
*/

// Call the SDK function to fetch the list of contacts.
connection.fetchContacts(fetchSuccess, fetchError);

function fetchSuccess(connection, contacts) {

  display("Fetched contacts");
  var contact;
  for (var i = 0; i < contacts.length; i++) {
    if (contacts[i].nickname == "JonnyD") {
      contact = contacts[i];
      display("Deleting ", contact.nickname, " with id ", contact.uid);

      // Call the SDK function to delete this contact.
      // This function also accepts an array of ids.
      connection.deleteContacts(contact.uid, saveSuccess, saveError);
    }
  }

  if (!contact) {
    display("Failed to find contact");
  }
}


function fetchError(connection, error) {
  display("Error fetching contacts: ");
  errorHandler(connection, error);
}           
                                         
function saveSuccess(connection) {
  display("Successfully deleted contact");
}

function saveError(connection, error) {
  display("Error deleting contact: ");
  errorHandler(connection, error);
} 

