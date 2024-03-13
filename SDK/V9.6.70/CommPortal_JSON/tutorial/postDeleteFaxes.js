/*
* Fetch a list of all the faxes, then delete the first one..
*/

// Call the SDK function to fetch the list of faxes.
connection.fetchFaxes(fetchSuccess, fetchError);

var id;

function fetchSuccess(connection, faxes) {
  display("Fetched faxes");
  if (faxes.length > 0) {
    var fax = faxes[0];
    id = fax.Id;

    // Call the SDK function to delete this fax.
    connection.deleteFaxes(id, deleteSuccess, deleteError);
  } else {
    display("There are no faxes");
  }
}

function fetchError(connection, error) {
  display("Error fetching faxes: ");
  errorHandler(connection, error);
}

function deleteSuccess(connection) {
  display("Deleted fax ", id);
}

function deleteError(connection, error) {
  display("Error deleting fax ", id, ": ");
  errorHandler(connection, error);
}

