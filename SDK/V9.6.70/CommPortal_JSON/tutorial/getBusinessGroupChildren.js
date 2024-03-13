/*
* Fetch the business group children information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_BusinessGroup_ChildrenList_Subscriber", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved business group information");
  display("There are ", data.Subscriber.length, " subscribers in this group:");
  // Display the numbers and types of subscribers in this group.
  for (var i = 0; i < data.Subscriber.length; i++) {
    display(data.Subscriber[i].DirectoryNumber, " Type: ", data.Subscriber[i].SubscriberType);
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve business group information: ");
  errorHandler(connection, error);
}



