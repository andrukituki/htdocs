/*
 * Fetch PBX DID Number base information settings.
 */

// Fetch the subscriber number asynchronously, and then request the PBX DID
// Number base information.
connection.fetchSubscriberNumber(
  function(connection, number)
  {
    // Fetch the data from the server in an easily accessible format.
    var data = "Meta_PBXDIDNumber_BaseInformation?pbxdid=" + number;
    connection.fetchData(data, fetchSuccess, fetchError);
  }
);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved PBX DID base information settings");
  display("Name of the CFS that contains this DID: ", data.NetworkElementName);
  display("Directory number of the DID line: ", data.DirectoryNumber);
  display("Directory number of the main PBX line: ", data.PBXDirectoryNumber);
}

function fetchError(connection, error) {
  display("Failed to retrieve PBX DID base information settings: ");
  errorHandler(connection, error);
}
