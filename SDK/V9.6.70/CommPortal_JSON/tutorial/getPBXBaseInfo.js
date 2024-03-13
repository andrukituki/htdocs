/*
 * Fetch PBX base information settings.
 */

// Fetch the subscriber number asynchronously, and then request the PBX base
// information.
connection.fetchSubscriberNumber(
  function(connection, number)
  {
    // Fetch the data from the server in an easily accessible format.
    var data = "Meta_PBX_BaseInformation?pbx=" + number;
    connection.fetchData(data, fetchSuccess, fetchError);
  }
);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved PBX base information settings");
  display("Name of the CFS that contains this PBX: ", data.NetworkElementName);
  display("Directory number of the main PBX line: ", data.DirectoryNumber);
}

function fetchError(connection, error) {
  display("Failed to retrieve PBX base information settings: ");
  errorHandler(connection, error);
}
