/*
* Fetch the list of voicemails.
*/

// Call the SDK function to fetch the list of voicemails
// Final parameter is optional - request voicemails encoded in g711u
connection.fetchVoicemails(fetchSuccess, fetchError, "g711u");

function fetchSuccess(connection, voicemails) {
  display("Fetched voicemails. There are ", voicemails.length, " voicemails.");
}

function fetchError(connection, error) {
  display("Error fetching voicemails: ");
  errorHandler(connection, error);
}

