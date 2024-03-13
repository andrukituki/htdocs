/*
* Fetch the selective call rejection settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_SelectiveCallRejection", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved selective call rejection settings");
  display("Selective call rejection subscribed: ", data.Subscribed);
  display("Selective call rejection enabled: ", data.Enabled);

  // Fetch the selective call rejection numbers list.
  connection.fetchData("Meta_Subscriber_SelectiveCallRejection_NumbersList", numberSuccess, numberError);
}

function fetchError(connection, error) {
  display("Failed to retrieve selective call rejection settings: ");
  errorHandler(connection, error);                          
}
                                                                                         
function numberSuccess(connection, dataType, data) {
  display("Retrieved selective rejection numbers:");
  if (typeof(data.Number) != "undefined") {
    // Display all the numbers in the selective call rejection list
    for (var i = 0; i < data.Number.length; i++) {
      display(data.Number[i]);
    }
  } else {
    display("No numbers");
  }
}

function numberError(connection, error) {
  display("Failed to retrieve selective rejection numbers");
  erroHandler(connection, error);
}
