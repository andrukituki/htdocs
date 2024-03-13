/*
* Bar international calls.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_CallBarring", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved call barring settings");
  if (data.Subscribed.Value._) {
    data.CurrentSubscriberBarredCallTypes.UseDefault._ = false
    data.CurrentSubscriberBarredCallTypes.Value.International._ = true;

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("Not subscribed to call barring");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve automatic callback information: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Successfully barred international calls");
}

function saveError(connection, error) {
  display("Error barring international calls");
  errorHandler(error);
}


                                                                                         
