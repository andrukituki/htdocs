/*
* Change the subscriber's EAS pin.
*/

var newPin = prompt("Enter new Subscriber EAS PIN: ", "");

if (newPin != "" && newPin != null) {
  // Fetch the data in a format that can be saved back to the server.
  connection.fetchRawData("Meta_Subscriber_MetaSphere_MessageSecurity", fetchSuccess, fetchError);
} else {
  display("No user input");
}

function fetchSuccess(connection, dataType, data) {
  display("Changing PIN to ", newPin);
  data.PIN = { '_' : newPin };

  // Save the data back to the server.
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Error fetching base information: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully changed EAS PIN to ", newPin);
}

function saveError(connection, error) {
  display("Error saving EAS PIN:");
  errorHandler(connection, error);
}

