/*
* Subscribe/unsubscribe to busy call forwarding.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_BusyCallForwarding", fetchSuccess, fetchError);

var newStatus;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved busy call forwarding settings");
  display("Currently subscribed to busy call forwarding: ", data.Subscribed.Value._);
  newStatus = prompt("Subscribed to busy call forwarding?[ON/OFF]", "");
  if (newStatus != "" && newStatus != null) {
    data.Subscribed.UseDefault._ = false;
    data.Subscribed.Value._ = (newStatus.toUpperCase() == "ON");

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve busy call forwarding settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully changed busy call forwarding subscription. Now subscribed: ", newStatus);
}

function saveError(connection, error) {
  display("Error (un)subscribing to busy call forwarding ");
  errorHandler(connection, error);
}


