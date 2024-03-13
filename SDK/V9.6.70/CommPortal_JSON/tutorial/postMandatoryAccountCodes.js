/*
* Turn on mandatory account codes for international calls.
*/

// Fetch the data in a format that can be saved back to the server.
connection.fetchRawData("Meta_Subscriber_MandatoryAccountCodes", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mandatory account codes");
  if (data.Subscribed.Value._) {
    data.USCallTypes.UseDefault._ = false;
    data.USCallTypes.Value.International = { "_" : true };

    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("Not subscribed to mandatory account codes");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve mandatory account codes: ");
  errorHandler(connection, error);                          
}

function saveSuccess(connection) {
  display("Sucessfully turned on mandatory account codes for international calls");
}

function saveError(connection, error) {
  display("Failed to turn on mandatory account codes for international calls");
}

                                                                                         
