/*
* Turn the busy greeting on or off.
*/

var input = prompt("Switch busy greeting ON or OFF?[ON/OFF]", "");

if (input != "" && input != null) {
  // Fetch the data in a format that can be saved back to the server.
  connection.fetchRawData("Meta_Subscriber_MetaSphere_Greetings", fetchSuccess, fetchError);
} else {
  display("No user input");
}

function fetchSuccess(connection, dataType, data) {
  display("Retrieved greetings information");
  display("Current busy greeting status: Enabled=", data.BusyEnabled._);
  data.OutOfHoursEnabled = { "_" : (input.toUpperCase() == "ON") };

  // Save the data to the server
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Failed to retrieve greetings information: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned busy greeting ", input);
}
function saveError(connection, error) {
  display("Error saving data: ");
  errorHandler(connection, error);
}


