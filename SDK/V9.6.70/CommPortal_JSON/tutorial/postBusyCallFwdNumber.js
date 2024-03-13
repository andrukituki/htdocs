/*
* Change the busy call forwarding number.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_BusyCallForwarding", fetchSuccess, fetchError);

var newNumber;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved busy call forwarding settings");
  if (data.Subscribed.Value._) {
    display("Current busy call forwarding number: ", data.Number._);
    newNumber = prompt("Enter new forwarding number: ", "");
    if (newNumber != "" && newNumber != null) {
      data.Number._ = newNumber;

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
  display("Successfully changed busy call forwarding number to ", newNumber);
}

function saveError(connection, error) {
  display("Error changing busy call forwarding number to ", newNumber);
  errorHandler(connection, error);
}


