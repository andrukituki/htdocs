/*
 * Fetch PBX DID Number unavailable call forwarding settings.
 */

// Fetch the subscriber number asynchronously, and then request the PBX DID
// Number unavailable call forwarding settings.
connection.fetchSubscriberNumber(
  function(connection, number)
  {
    // Fetch the data from the server in an easily accessible format.
    var data = "Meta_PBXDIDNumber_UnavailableCallForwarding?pbxdid=" + number;
    connection.fetchData(data, fetchSuccess, fetchError);
  }
);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved PBX DID unavailable call forwarding settings");
  display("Is the service enabled? ", data.Enabled);
  display("Directory number to forward to: ", data.Number);
}

function fetchError(connection, error) {
  display("Failed to retrieve PBX DID unavailable call forwarding settings: ");
  errorHandler(connection, error);
}
