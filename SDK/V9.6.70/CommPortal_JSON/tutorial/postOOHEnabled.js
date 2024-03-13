/*
* Switch the out of hours greeting on or off.
*/

var input = prompt("Switch out of hours greeting ON or OFF?[ON/OFF]", "");

if (input != "" && input != null) {
  // Fetch the data in a format that we can save back to the server.
  connection.fetchRawData("Meta_Subscriber_MetaSphere_Greetings", fetchSuccess, fetchError);
} else {
  display("No user input");
}

function fetchSuccess(connection, dataType, data) {
  display("Retrieved greetings information");
  display("Current OOH status: Enabled = ", data.OutOfHoursEnabled._);
  data.OutOfHoursEnabled = { "_" : (input.toUpperCase() ==" ON") };

  // Save the data to the server.
  connection.saveData(dataType, data, saveSuccess, saveError);
}

function fetchError(connection, error) {
  display("Failed to retrieve greetings information: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned OOH ", input);
}
function saveError(connection, error) {
  display("Error saving data: ");
  errorHandler(connection, error);
}


