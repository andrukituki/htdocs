/*
* Add a new contact named John Doe.
*/

// John Doe's address
var address =
{
      street : "1001 Marina Village Parkway",
    locality : "Alameda",
      region : "CA",
  postalcode : "94501",
     country : "USA"
}

// John Doe's contact details, including his address from above.
var contact =
{
     givenName : "John",
    familyName : "Doe",
      nickname : "JonnyD",
  organization : "MetaSwitch",
       address : [ address ],
   addressType : [ "work" ],
         phone : [ "5107488230", "5105551234" ],
     phoneType : [ "work", "cell" ],
         email : [ "john.doe@metaswitch.com", "john.doe@gmail.com" ]
}

display("Adding new contact named: ", contact.givenName, " ", contact.familyName);

// Call the SDK function to add a contact.
connection.addContact(contact, saveSuccess, saveError);

function saveSuccess(connection) {
  display("Added contact successfully");
}

function saveError(connection, error) {
  display("Error adding contact: ");
  errorHandler(connection, error);
}                                                                    

