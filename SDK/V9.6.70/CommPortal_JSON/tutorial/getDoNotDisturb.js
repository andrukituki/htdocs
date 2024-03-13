/*
* Fetch the do not disturb and selective call acceptance settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_DoNotDisturb", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved do not disturb settings");
  display("Do not disturb subscribed: ", data.Subscribed);
  display("Do not disturb enabled: ", data.Enabled);
  display("Service Level: ", data.ServiceLevel);
  if (data.ServiceLevel == "Selective Call Acceptance") {
    // Fetch the selective call acceptance number list.
    connection.fetchData("Meta_Subscriber_DoNotDisturb_SCANumbersList", numberSuccess, numberError)
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve do not disturb settings: ");
  errorHandler(connection, error);                          
}

function numberSuccess(connection, dataType, data) {
  display("Sucessfully retrieved selective call acceptance numbers list:");
  // Display all the numbers in the list.
  if (typeof(data.Number) != "undefined") {
    for (var i = 0; i < data.Number.length; i++) {
      display(data.Number[i]);
    }                         
  } else {
    display("No numbers")
  }
}

function numberError(connection, error) {
  display("Failed to retrieve selective call acceptance numbers list");
  errorHandler(error);
}

                                                                                         
