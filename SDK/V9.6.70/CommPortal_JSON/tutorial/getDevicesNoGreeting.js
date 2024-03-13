/*
* Fetch the MetaSphere devices information.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_DevicesNoGreetingInfo", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved devices information");
  display("There are ", data.length, " devices:");

  // For each device, display a summary.
  for (var i = 0; i < data.length; i++) {
  
    if (typeof(data[i].AcfSettings) != "undefined") {
      // This device has acf settings.
      display(data[i].Number, " Fmfm Enabled = ", data[i].AcfSettings.FmfmEnabled, ". Fmfm Targets: ");
  
      // Display all the targets for each device.
      for (var i = 0; i < data[i].AcfSettings.FmfmTargets.length; i++) {
        display("Outdial number: ", data[i].AcfSettings.FmfmTargets[i].Number);
      }
    } else {
      display(data[i].Number, " has no Acf Settings.");
    }
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve devices information: ");
  errorHandler(connection, error);
}


