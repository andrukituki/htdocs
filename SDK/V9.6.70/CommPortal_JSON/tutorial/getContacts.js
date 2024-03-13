/*
* Fetch a list of all the contacts
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchContacts(fetchSuccess, fetchError);

function fetchSuccess(connection, contacts) {
  display("Fetched contacts. There are ", contacts.length, " contacts");
  for (var i = 0; i < contacts.length; i++) {
    display(contacts[i].displayName);
  }
}

function fetchError(connection, error) {
  display("Error fetching contacts: ");
  errorHandler(connection, error);
}                                                                    

