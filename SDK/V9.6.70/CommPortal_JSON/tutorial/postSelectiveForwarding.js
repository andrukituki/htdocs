/*
* Adds a number to the selective call forwarding numbers list.
*/

//Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_SelectiveCallForwarding_NumbersList", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved selective call forwarding number list");
  var newNumber = prompt("Enter new number for selective call forwarding", "");
  if (newNumber != "" && newNumber != null) {
    if (typeof(data.Number) != "undefined") {
      // Add a number to the list.
      data.Number.push({"_" : newNumber});
    } else {
      data.Number = [{"_" : newNumber}];
    }

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve selective call forwarding settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Successfully saved new number to selective call forwarding list");
}

function saveError(connection, error) {
  display("Failed to add new number to selective call forwarding list:");
  errorHandler(connection, error);
}
