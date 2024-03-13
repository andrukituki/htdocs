/*
* Fetch the list of missed calls.
*/

// Call the SDK function to fetch the list of missed calls
connection.fetchMissedCalls(fetchSuccess, fetchError);

function fetchSuccess(connection, missedCalls) {
  display("Fetched Missed Calls List. There are ", missedCalls.length, " missed calls.");
  for (var i = 0; i < missedCalls.length; i++) {
    display(missedCalls[i].DirectoryNumber);
  }
}

function fetchError(connection, error) {
  display("Error fetching missed calls: ");
  errorHandler(connection, error);
}

