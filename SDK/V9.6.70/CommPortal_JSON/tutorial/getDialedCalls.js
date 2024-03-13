/*
* Fetch the list of dialed calls.
*/

// Call the SDK function to fetch the list of dialed calls.
connection.fetchDialedCalls(fetchSuccess, fetchError);

function fetchSuccess(connection, dialedCalls) {
  display("Fetched Dialed Calls List. There are ", dialedCalls.length, " dialed calls.");
  for (var i = 0; i < dialedCalls.length; i++) {
    display(dialedCalls[i].DirectoryNumber);
  }
}

function fetchError(connection, error) {
  display("Error fetching dialed calls: ");
  errorHandler(connection, error);
}

