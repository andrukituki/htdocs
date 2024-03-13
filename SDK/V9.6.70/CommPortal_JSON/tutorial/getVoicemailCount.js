/*
* Fetches a count of the voicemails.
*/

// Call the SDK function to fetch the list of voicemails.
connection.fetchVoicemailCount(countSuccess, countError);

function countSuccess(connection, total, unheard) {
  display("There are ", total, " voicemails, ", unheard, " of them are unheard");
}

function countError(connection, error) {
  display("Error fetching voicemail count: ");
  errorHandler(connection, error);
}

