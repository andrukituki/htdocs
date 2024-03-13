/*
* Turn auto-forwarding off for fax and voicemail.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_AutoForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved auto-forwarding settings");
  display("Forwarding currently enabled: ", data.ForwardingStatus._);
  data.ForwardingStatus._ = false;

  // Save the data to the server.
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Failed to retrieve auto-forwarding settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned auto-forwarding off");
}

function saveError(connection, error) {
  display("Error turning auto-forwarding off: ");
  errorHandler(connection, error);
}


