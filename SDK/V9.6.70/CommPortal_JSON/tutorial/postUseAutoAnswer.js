/*
* Turn Auto Answer ON/OFF for Click to Dial.
*/

// Fetch the data in a form that can be saved back to the server.
connection.fetchRawData("Meta_SubscriberDevice_MetaSphere_CTDDeviceConfig", fetchSuccess, fetchError);

var newStatus;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved click to dial settings");
  display("Auto Answer currently enabled: ", data.UseAutoAnswer._);
  newStatus = prompt("Turn Auto Answer ON or OFF?[ON/OFF]");
  if (newStatus != "" && newStatus != null) {
    data.UseAutoAnswer = {"_" : (newStatus.toUpperCase() == "ON") };

    // Call the SDK function to save this data.
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
  display("Successfully turned Auto Answer ", newStatus);
}

function saveError(connection, error) {
  display("Error turning Auto Answer ", newStatus, ":");
  errorHandler(connection, error);
}


