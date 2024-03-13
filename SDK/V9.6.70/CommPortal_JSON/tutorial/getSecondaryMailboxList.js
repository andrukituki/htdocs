/*
* Fetch the list of secondary mailboxes.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_SecondaryMailboxList", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved secondary mailbox list");
  display("There are ", data.length, " mailboxes in this group:");
  // Display summary information about each secondary mailbox
  if (data.length > 0) {
    for (var i=0; i < data.length; i++) {
      display("DN=", data[i].SecondaryDirectoryNumber, " Enabled=", data[i].Enabled, " First Name= ", data[i].FirstName);
    }
  }
}

function fetchError(connection, error) {
  display("Failed to find secondary mailbox list: ");
  errorHandler(connection, error);
}

