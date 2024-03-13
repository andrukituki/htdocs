/*
* Fetch a list of all the voicemails, then mark the first one as heard.
*/

// Call the SDK function to fetch voicemails.
connection.fetchVoicemails(fetchSuccess, fetchError);

var id;

function fetchSuccess(connection, voicemails) {
  display("Fetched voicemails");
  if (voicemails.length > 0) {
    var voicemail = voicemails[0];
    id = voicemail.Id;

    // Call the SDK function to mark voicemail with this id as heard.
    connection.markVoicemailsAsHeard(id, saveSuccess, saveError);
  } else {
    display("There are no voicemails.")
  }
}

function fetchError(connection, error) {
  display("Error fetching voicemails: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Marked voicemail ", id, " as heard");
}

function saveError(connection, error) {
  display("Error marking voicemail ", id, " as heard:");
  errorHandler(connection, error);
}
