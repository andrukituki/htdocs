/*
* Fetch the base subscriber information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_BaseInformation", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved base information");
  display("MetaSwitch name: ", data.MetaSwitchName);
  display("Business group name: ", data.BusinessGroupName);
  display("Subscriber type: ", data.SubscriberType);
  display("Local call carrier: ", data.IntraLATACarrier);
  display("Long distance carrier: ", data.LongDistanceCarrier);
  display("International carrier: ", data.InternationalCarrier);
}

function fetchError(connection, error) {
  display("Failed to retrieve base information: ");
  errorHandler(connection, error);
}


