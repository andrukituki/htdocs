/*
* Fetch the list of greetings and settings for greetings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_Greetings", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved greetings information");
  display("Default greeting: ", data.DefaultGreetingType);
  display("Out of hours greeting enabled: ", data.OutOfHoursEnabled);
  display("Busy greeting enabled: ", data.BusyEnabled);
  display("There are ", data.GreetingsList.length, " possible greetings");
}

function fetchError(connection, error) {
  display("Failed to retrieve greetings information: ");
  errorHandler(connection, error);
}


