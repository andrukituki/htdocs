/*
* Enable anonymous call rejection.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_AnonymousCallRejection", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved anonymous call rejection settings");
  data.Enabled._ = true;

  // Save the data to the server.
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Failed to retrieve anonymous call rejection settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Successfully enabled anonymous call rejection");
}

function saveError(connection, error) {
  display("Failed to enable anonymous call rejection");
  errorHandler(connection, error);
}
