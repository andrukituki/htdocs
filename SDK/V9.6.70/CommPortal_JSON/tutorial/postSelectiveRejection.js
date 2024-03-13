/*
* Add a number to the selective call rejection numbers list.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_SelectiveCallRejection_NumbersList", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved selective call rejection number list");
  var newNumber = prompt("Enter new number for selective call rejection: ", "");
  if (newNumber != "" && newNumber != null) {
    if (typeof(data.Number) != "undefined") {
      // Add a number to the list.
      data.Number.push({"_" : newNumber});
    } else {
      data.Number = [{"_" : newNumber}];
    }

    // Save the data back to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve selective call rejection settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Successfully saved new number to selective call rejection list");
}

function saveError(connection, error) {
  display("Failed to add new number to selective call rejection list");
  errorHandler(connection, error);
}
