/*
* Fetch unconditonal call forwarding settings.
*/

// Fetch the data in a form that we can save back to the server. 
connection.fetchRawData("Meta_Subscriber_UnconditionalCallForwarding", fetchSuccess, fetchError);

var newNumber;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved unconditional call forwarding settings");
  if (data.Subscribed.Value._) {
    newNumber = prompt("Enter new forwarding number: ", "");
    if (newNumber != "" && newNumber != null) {
      data.Number._ = newNumber;
      data.Enabled._ = true;
      // Save the data back to the server.
      connection.saveData(dataType, data, saveSuccess, saveError);
    } else {
      display("No user input");
    }
  } else {
    display("Not subscribed to unconditional call forwarding");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve unconditional call forwarding settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Successfully saved new settings");
}

function saveError(connection, error) {
  display("Error saving new settings: ");
  errorHandler(connection, error);
}
