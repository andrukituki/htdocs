/*
* Fetch mandatory account code settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MandatoryAccountCodes", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved mandatory account codes");
  if (data.Subscribed) {
    display("Account codes required for these call types:");
    display(getCallTypes(data));
  } else {
    display("Not subscribed to mandatory account codes");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve mandatory account codes: ");
  errorHandler(connection, error);                          
}

// Extracts the call types with mandatory account codes from the data.
function getCallTypes(data) {
  var callTypes = [];
  for (var i in data.USCallTypes.Value) {
    if (data.USCallTypes.Value[i]) {
      callTypes.push(i);
    }
  }
  if (callTypes.length == 0) {
    callTypes = "None";
  }
  return callTypes;
}


                                                                                         
