/*
* Fetch a list of all the voicemails, then delete the first one.
*/

// Call the SDK function to fetch the list of voicemails.
connection.fetchVoicemails(fetchSuccess, fetchError);

var id;

function fetchSuccess(connection, voicemails) {
  display("Fetched voicemails");
  if (voicemails.length > 0) {
    var voicemail = voicemails[0];
    id = voicemail.Id;

    // Call the SDK function to delete this voicemail.
    connection.deleteVoicemails(id, saveSuccess, saveError);
  } else {
    display("There are no voicemails");
  }
}

function fetchError(connection, error) {
  display("Error fetching voicemails: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Deleted voicemail ", id);
}

function saveError(connection, error) {
  display("Error deleting voicemail ", id, ": ");
  errorHandler(connection, error);
}

