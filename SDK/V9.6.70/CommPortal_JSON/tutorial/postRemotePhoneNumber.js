/*
* Change the Click to Dial remote phone number.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_SubscriberDevice_MetaSphere_CTDDeviceConfig", fetchSuccess, fetchError);

var newNumber;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved click to dial settings");
  if (typeof(data.RemotePhoneNumber) != "undefined") {
    display("Current remote phone number: ", data.RemotePhoneNumber._);
  } else {
    display("Currently no remote phone number");
  }
  newNumber = prompt("Enter new remote phone number: ", "");
  if (newNumber != "" && newNumber != null) {
    data.RemotePhoneNumber = {"_" : newNumber };

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve click to dial settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully changed remote phone number to ", newNumber);
}

function saveError(connection, error) {
  display("Error changing remote phone number to ", newNumber, ": ");
  errorHandler(connection, error);
}


