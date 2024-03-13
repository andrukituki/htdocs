/*
* Fetch a list of all the voicemails, then mark the first one as unheard.
*/

// Call the SDK function to fetch voicemails
connection.fetchVoicemails(fetchSuccess, fetchError);

var id;

function fetchSuccess(connection, voicemails) {
  display("Fetched voicemails");
  if (voicemails.length > 0) {
    var voicemail = voicemails[0];
    id = voicemail.Id;

    // Call the SDK function to mark this voicemail as head
    connection.markVoicemailsAsUnheard(id, markSuccess, markError);
  } else {
    display("There are no voicemails");
  }
}

function fetchError(connection, error) {
  display("Error fetching voicemails: ");
  errorHandler(connection, error);
}

function markSuccess(connection) {
  display("Marked voicemail ", id, " as unheard");
}

function markError(connection, error) {
  display("Error marking voicemail ", id, " as unheard: ");
  errorHandler(connection, error);
}
