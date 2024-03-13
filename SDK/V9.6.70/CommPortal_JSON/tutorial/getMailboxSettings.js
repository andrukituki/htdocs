/*
* Fetch the mailbox settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_MailboxSettings", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mailbox settings");
  display("Skip PIN login status: ", data.SkipPinStatus);
  display("Fast login status: ", data.FastLoginStatus);
  display("Play voicemail on login: ", data.AutoPlayVoicemailStatus);
}

function fetchError(connection, error) {
  display("Failed to retrieve mailbox settings: ");
  errorHandler(connection, error);
}


