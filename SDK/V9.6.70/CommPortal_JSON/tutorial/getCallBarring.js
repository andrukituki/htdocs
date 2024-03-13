/*
* Fetch the call barring settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_CallBarring", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved call barring settings");
  if (data.Subscribed) {
    // Display the operator barred call types.
    display("Types barred by Operator:");
    display(getBarredTypes(data, "Operator"));
    // Display the subscriber barred call types.
    display("Types barred by Subscriber:");
    display(getBarredTypes(data, "Subscriber"));
  } else {
    display("Not subscribed to call barring");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve automatic callback information: ");
  errorHandler(connection, error);                          
}

// Gets the barred call types for the operator or subscriber
function getBarredTypes(data, barrer) {
  var barredTypes = [];
  var typeList = (barrer == "Operator" ? data.CurrentOperatorBarredCallTypes.Value : 
                                          data.CurrentSubscriberBarredCallTypes.Value);
  for (var i in typeList) {
    if (typeList[i]) {
      barredTypes.push(i);
    }
  }
  if (barredTypes.length == 0) {
    barredTypes = "There are no call types barred by the " + barrer;
  }
  return barredTypes;
}


                                                                                         
