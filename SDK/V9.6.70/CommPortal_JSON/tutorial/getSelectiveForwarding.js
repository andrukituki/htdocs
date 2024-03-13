/*
* Fetch selective call forwarding settings.
*/

// Fetch the data from the server
connection.fetchData("Meta_Subscriber_SelectiveCallForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved selective call forwarding settings");
  display("Subscribed to selective call forwarding: ", data.Subscribed);
  display("Selective call forwarding enabled: ", data.Enabled);

  // Fetch the selective call forwarding numbers list.
  connection.fetchData("Meta_Subscriber_SelectiveCallForwarding_NumbersList", numberSuccess, numberError);
}

function fetchError(connection, error) {
  display("Failed to retrieve delayed call forwarding settings: ");
  errorHandler(connection, error);                          
}

function numberSuccess(connection, dataType, data) {
  display("Retrieved selective call forwarding numbers list");
  if (typeof(data.Number) != "undefined") {
    // Display all the numbers in the selective call forwarding numbers list
    for (var i = 0; i < data.Number.length; i++) {
      display(data.Number[i]);
    }
  } else {
    display("No numbers");
  }
}

function numberError(connection, error) {
  display("Failed to retrieve selective forwarding numbers list");
  errorHander(connection, error);
}
