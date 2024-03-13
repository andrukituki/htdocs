/*
* Fetch the list of all fax messages.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchFaxes(fetchSuccess, fetchError);

function fetchSuccess(connection, faxes) {
  display("Fetched faxes. There are ", faxes.length, " faxes.");
  display("Faxes are from: ");
  for (var i = 0; i < faxes.length; i++) {
    displau(faxes[i].From);
  }
}

function fetchError(connection, error) {
  display("Error fetching faxes: ");
  errorHandler(connection, error);
}

