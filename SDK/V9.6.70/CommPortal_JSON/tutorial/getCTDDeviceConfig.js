/*
* Fetch the click to dial settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_SubscriberDevice_MetaSphere_CTDDeviceConfig", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved click to dial settings");
  display("Remote phone number: ", data.RemotePhoneNumber);
  display("Auto Answer Enabled: ", data.UseAutoAnswer);
  if (data.UseOwnPhone) {
    display("Use the device as the originating phone");
  } else {
    display("Use a remote phone number as the originating phone");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve click to dial settings: ");
  errorHandler(connection, error);
}


