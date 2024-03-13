/*
* Enables or disable busy call forwarding.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_BusyCallForwarding", fetchSuccess, fetchError);

var newStatus;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved busy call forwarding settings");
  if (data.Subscribed.Value._) {
    display("Busy call forwarding currently enabled: ", data.Enabled._);
    newStatus = prompt("Turn busy call forwarding ON or OFF?[ON/OFF]", "");
    if (newStatus != "" && newStatus != null) {
      data.Enabled._ = (newStatus == "ON");

      // Save the data back to the server.
      connection.saveData(dataType, data, saveSuccess, saveError);
    } else {
      display("No user input");
    }
  } else {
    display("Not subscribed to busy call forwarding");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve busy call forwarding settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned busy call forwarding ", newStatus);
}

function saveError(connection, error) {
  display("Error turning busy call forwarding ", newStatus, ", ");
  errorHandler(connection, error);
}


