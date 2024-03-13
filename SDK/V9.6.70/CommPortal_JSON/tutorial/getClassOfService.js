/*
* Fetch the class of service information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_ClassOfService", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved class of service information");
  display("Fax enabled: ", data.FaxEnabled);
  display("Click to dial allowed for this subscriber: ", data.ClickToDialAllowed)
  display("Voicemail allowed: ", data.MessagingAllowed);
  display("Allowed to use call logs: ", data.CallLogEnabled);
  display("Subscribed mashups: ");
  display(data.SubscribedMashups);
}

function fetchError(connection, error) {
  display("Failed to retrieve class of service information: ");
  errorHandler(connection, error);
}


