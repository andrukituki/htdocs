/*
* Fetch a list of all the faxes, then mark the first one as viewed.
*/

// Call the SDK function to fetch the list of faxes.
connection.fetchFaxes(fetchSuccess, fetchError);

var id;

function fetchSuccess(connection, faxes) {
  display("Fetched faxes");
  if (faxes.length > 0) {
    var fax = faxes[0];
    id = fax.Id;

    // Call the SDK function to mark this fax as viewed.
    connection.markFaxesAsViewed(id, saveSuccess, saveError);
  } else {
    display("There are no faxes");
  }
}

function fetchError(connection, error) {
  display("Error fetching faxes: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Marked fax ", id, " as viewed");
}

function saveError(connection, error) {
  display("Error marking fax ", id, " as viewed: ");
  errorHandler(connection, error);
}

