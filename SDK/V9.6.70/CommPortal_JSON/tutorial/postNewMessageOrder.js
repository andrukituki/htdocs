/*
* Change the order in which new messages are played.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_MessageOrdering", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved message ordering settings");
  data.PlayNewMessage._ = "FIFO";

  // Save the data back to the server.
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Failed to retrieve message ordering settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully changed order");
}

function saveError(connection, error) {
  display("Error changing message order");
  errorHandler(connection, error);
}


