/*
* Change whether the user is asked for a PIN on login..
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_MailboxSettings", fetchSuccess, fetchError);

var newStatus;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mailbox settings");
  display("Current Skip PIN login status: ", data.SkipPinStatus._);
  newStatus = prompt("Switch Skip PIN login ON or OFF?[ON/OFF]", "");
  if (newStatus != "" && newStatus != null) {
    data.SkipPinStatus = {"_" : (newStatus.toUpperCase() == "ON") }

    // Save the data back to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input")
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve mailbox settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned Skip PIN login ", newStatus);
}

function saveError(connection, error) {
  display("Error turning Skip PIN login ", newStatus, ": ");
  errorHandler(connection, error);
}


