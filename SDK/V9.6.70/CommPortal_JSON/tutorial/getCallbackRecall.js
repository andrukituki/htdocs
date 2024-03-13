/*
* Fetch the automatic recall and automatic callback settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_AutomaticCallback", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved automatic callback information");
  display("Subscriber to automatic callback: ", (data.Subscribed));

  // Fetch the automatic recall settings.
  connection.fetchData("Meta_Subscriber_AutomaticRecall", recallSuccess, recallError);
}

function fetchError(connection, error) {
  display("Failed to retrieve automatic callback information: ");
  errorHandler(connection, error);                          
}

function recallSuccess(connection, dataType, data) {
  display("Retrieved automatic recall information")
  display("Subscribed to automatic recall: ", (data.Subscribed));
}                                                               

function recallError(connection, error) {
  display("Failed to retrieve automatic recall information:");
  errorHandler(connection, error);
}

                                                                                         
