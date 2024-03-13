/*
* Add a number to the list of speed calling numbers.
*/

connection.fetchData("Meta_Subscriber_SpeedCalling", configSuccess, configError)

function configSuccess(connection, dataType, data) {
  if (data.Subscribed.Value._) {
    // Fetch the data in a format that can be saved back to the server.
    connection.fetchRawData("Meta_Subscriber_SpeedCalling_NumbersList", fetchSuccess, fetchError);
  } else {
    display("Not subscribed to speed calling");
  }
}

function configError(connection, error) {
  display("Failed to retrieve speed calling settings");
  errorHandler(connection, error);
}

function fetchSuccess(connection, dataType, data) {
  display("Retrieved speed callling numbers");
  var newNumber = prompt("Enter new number for speed calling: ", "");
  var newCode = prompt("Enter new speed calling code: ", "");
  if (newNumber != "" && newNumber != null && newCode != "" && newCode != null) {
    // If there are no numbers, we need to begin a new array.
    if (typeof(data.Number) == "undefined") {
      data.Number = [];
    }

    // Add a speed calling number to the data.
    data.Number.push( { "SpeedCallingNumber" : { "_" : newNumber }, "SpeedCallingCode" : { "_" : newCode} } );

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve speed calling numbers settings: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully added new number");
}

function saveError(connection, error) {
  display("Failed to add new number");
  errorHandler(connection, error);
}

