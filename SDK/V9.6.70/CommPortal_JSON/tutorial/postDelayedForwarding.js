/*
* Updates delayed call forwarding settings.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_DelayedCallForwarding", fetchSuccess, fetchError);

var newNumber;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved delayed call forwarding settings");
  if (data.Subscribed.Value._) {
    newNumber = prompt("Enter new forwarding number", "");
    if (newNumber != "" && newNumber != null) {
      data.Enabled._ = true;
      data.Number._ = newNumber;
      data.NoReplyTime.UseDefault._ = true;

      // Save the data to the server.
      connection.saveData(dataType, data, saveSuccess, saveError);
    } else {
      display("No user input");
    }
  } else {
    display("Not subscribed to delayed call forwarding");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve delayed call forwarding settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Scuccessfully updated delayed call forwarding settings")
}

function saveError(connection, error) {
  display("Failed to update delayed call forwarding settings");
  errorHandler(connection, error);
}
