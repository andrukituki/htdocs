/*
 * Fetch PBX direct inward calling settings.
 */

// Fetch the subscriber number asynchronously, and then request the PBX direct
// inward calling settings.
connection.fetchSubscriberNumber(
  function(connection, number)
  {
    // Fetch the data from the server in an easily accessible format.
    var data = "Meta_PBX_DirectInwardCalling?pbx=" + number;
    connection.fetchData(data, fetchSuccess, fetchError);
  }
);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved PBX direct inward calling settings");

  for (i in data.DirectInwardCalling)
  {
    dicData = data.DirectInwardCalling[i];

    if (i > 0)
    {
      display("");
    }

    display("Data for range ", (parseInt(i, 10) + 1));
    display("  Size of the range: ", dicData.RangeSize);
    display("  First number in the range: ", dicData.FirstDirectoryNumber);
    display("  Last number in the range: ", dicData.LastDirectoryNumber);
    display("  Code of the first number: ", dicData.FirstCode);
    display("  Code of the last number: ", dicData.LastCode);
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve PBX direct inward calling settings: ");
  errorHandler(connection, error);
}
