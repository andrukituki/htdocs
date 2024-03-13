/*
* Fetch the list of contacts, search for a contact with nickname "JonnyD", 
* and modify his address.
*/

// The new address for JonnyD
var newAddress = 
{
    street : "John Road",
    locality : "Johntown",
    region : "John Island",
    postalcode : "123123",
    country : "USA"
}


connection.fetchContacts(fetchSuccess, fetchError);

function fetchSuccess(connection, contacts) {
  display("Fetched contacts");
  var contact;

  for (var i = 0; i < contacts.length; i++) {
    if (contacts[i].nickname == "JonnyD") {
      contact = contacts[i];
      display("Modifying ", contact.nickname, " with id ", contact.uid);
      contact.address = newAddress;

      // Call the SDK function to modify this contact.
      connection.modifyContact(contact, saveSuccess, saveError);
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
  display("Successfully modified contact");
}

function saveError(connection, error) {
  display("Error modifying contact: ");
  errorHandler(connection, error);
}

                                                                    



