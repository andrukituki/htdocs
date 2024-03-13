/*
* Fetch the speed calling information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_SpeedCalling", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved speed calling information");
  if (data.Subscribed) {
    // Fetch the speed calling numbers list from the server
    connection.fetchData("Meta_Subscriber_SpeedCalling_NumbersList", numbersSuccess, numbersError);
  } else {
    display("Not subscribed to speed calling");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve speed calling information: ");
  errorHandler(connection, error);
}

function numbersSuccess(connection, dataType, data) {
  display("Retrieved speed calling numbers");
  if (typeof(data.Number) != "undefined") {
    display("There are ", data.Number.length, " speed calling numbers enabled:");
    // Display each speed calling number with its code.
    for (var i = 0; i < data.Number.length; i++) {
      display(data.Number[i].SpeedCallingNumber, " has speed dial ", data.Number[i].SpeedCallingCode);
    }
  } else {
    display("There are no speed calling numbers defined");
  }
}

function numbersError(connection, error) {
  display("Failed to retrieve speed calling numbers");
  errorHandler(connection, error);
}
                                                                                         
