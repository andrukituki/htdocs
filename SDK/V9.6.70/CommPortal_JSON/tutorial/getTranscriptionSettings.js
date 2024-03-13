/*
* Fetch the transcription settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchTranscriptionEnabled(fetchSuccess, fetchError);

function fetchSuccess(connection, transcriptionEnabled) {
  display("Retrieved transcription enabled");
  display("Speech-to-text transcriptions enabled: ", transcriptionEnabled);
}

function fetchError(connection, error) {
  display("Failed to retrieve transcription settings: ");
  errorHandler(connection, error);
}


