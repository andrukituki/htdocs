/*
* Fetch the mailbox quota information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_QuotaInformation", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mailbox quota information");
  display("Mailbox is ", getStatus(data));
  display("Space used in mailbox: ", data.TotalUsage, "KB (", (data.TotalUsage/1024), "MB)");
}

function fetchError(connection, error) {
  display("Failed to retrieve mailbox quota information: ");
  errorHandler(connection, error);
}

// Check for mailbox full alerts.
function getStatus(data) {
  if (data.IsFull) {
    return "full";
  } else if (data.IsFullOrNearlyFull) {
    return "nearly full";
  } else {
    return "not even nearly full";
  }
}


