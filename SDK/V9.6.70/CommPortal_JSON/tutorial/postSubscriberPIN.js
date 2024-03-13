/*
* Change the subscriber's CFS pin.
*/

var newPin = prompt("Enter new Subscriber CFS PIN: ", "");

if (newPin != "" && newPin != null) {
  // Fetch the data in a format that can be saved back to the server.
  connection.fetchRawData("Meta_Subscriber_BaseInformation", fetchSuccess, fetchError);
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
  display("Successfully changed CFS PIN to ", newPin);
}

function saveError(connection, error) {
  display("Error saving CFS PIN:");
  errorHandler(connection, error);
}

