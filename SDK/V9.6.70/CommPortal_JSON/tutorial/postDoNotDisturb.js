/*
* Add a number to the selective call acceptance numbers list.
*/

// Fetch the data in a format that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_DoNotDisturb_SCANumbersList", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved do not disturb settings");
  var newNumber = prompt("Enter new selective call acceptance number: ", "");
  if (newNumber != "" && newNumber != null) {
    if (typeof(data.Number) != "undefined") {
      data.Number.push( { "_" : newNumber} );
    } else {
      data.Number = [{ "_" : newNumber}];
    }
    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }

}

function fetchError(connection, error) {
  display("Failed to retrieve do not disturb settings: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Sucessfully saved new selective call acceptance number");
}

function saveError(connection, error) {
  display("Failed to save new selective call acceptance number");
  errorHandler(connection, error);
}

                                                                                         
