/*
* Turn fast login on or off.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_MailboxSettings", fetchSuccess, fetchError);

var newStatus;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mailbox settings");
  display("Current fast login status: ", data.FastLoginStatus._);
  newStatus = prompt("Switch fast login ON or OFF?[ON/OFF]", "");
  if (newStatus != "" && newStatus != null) {
    data.FastLoginStatus = {"_" : (newStatus.toUpperCase() == "ON") }

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve mailbox settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned fast login ", newStatus);
}

function saveError(connection, error) {
  display("Error turning fast login ", newStatus, ": ");
  errorHandler(connection, error);
}


