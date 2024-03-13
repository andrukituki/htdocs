/*
* Enables/disable the first mailbox in the list of secondary mailboxes.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_SecondaryMailboxList", fetchSuccess, fetchError);

var input;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved secondary mailbox list");
  input = prompt("Switch first mailbox ON or OFF? [ON/OFF]", "");
  if (input != "" && input != null) {
    if (data.length > 0) {
      data[0].Enabled._ = (input.toUpperCase() == "ON");

      // Save the data to the server
      connection.saveData(dataType, data, saveSuccess, saveError);
    } else {
      display("There are no boxes");
    }
  } else {
      display("No user input")
  }
}

function fetchError(connection, error) {
  display("Failed to find secondary mailbox list: ");
  errorHandler(connection, error);
  display("N.B. the subscriber must be a group subscriber.");
}

function saveSuccess(connection) {
  display("Successfully turned first secondary mailbox ", input);
}

function saveError(connection, error) {
  display("Error saving new number: ");
  errorHandler(connection, error);
}


