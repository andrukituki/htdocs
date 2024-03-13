/*
* Fetch customer information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_CustomerInformation", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved customer information");
  display("Customer's name is: ", data.CustInfo6);
}

function fetchError(connection, error) {
  display("Failed to retrieve customer information: ");
  errorHandler(connection, error);
}


