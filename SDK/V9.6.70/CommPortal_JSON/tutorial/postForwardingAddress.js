/*
* Change the addresses for auto-forwarding and turn on auto-forwarding.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_AutoForwarding", fetchSuccess, fetchError);

var newAddress;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved auto-forwarding settings");
  newAddress = prompt("Enter new address", "");
  if (newAddress != "" && newAddress != null) {
    data.ForwardingAddresses = [{ "_" : newAddress}];
    data.ForwardingStatus._ = true;

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve auto-forwarding settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully added forwarding address: ", newAddress);
}

function saveError(connection, error) {
  display("Error adding forwarding address, ", newAddress, ":");
  errorHandler(connection, error);
}


