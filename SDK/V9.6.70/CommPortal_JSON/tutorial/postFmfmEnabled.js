/*
* Turn "Find Me Follow Me" on or off for first device.
*/

// Fetch the data in a form that we can save back to the server.
connection.fetchRawData("Meta_Subscriber_MetaSphere_DevicesNoGreetingInfo", fetchSuccess, fetchError);

var input;

function fetchSuccess(connection, dataType, data) {
  display("Retrieved devices information");
  display("Find Me Follow Me currently enabled: ", data[0].AcfSettings.FmfmEnabled._);
  input = prompt("Turn 'Find Me Follow Me' ON or OFF?[ON/OFF]", "");
  if (input != "" && input != null) {
    var subscriberNumber = data[0].Number._;

    // Build the Find Me Follow Me settings.
    data = [];

    // The settings for this schedule. 
    var entry =
    {
      Days : [{ "_" : 1 }, { "_" : 3 }, { "_" : 5 }],
      StartTime : { "_" : "0800"},
      EndTime : { "_" : "1700"},
      ScheduleType: { "_" : "WeeklySched" }
    } 
    // A set of schedules.
    var schedule =
    {
      Name : { "_" : "2" },
      Id : { "_" : "2" },
      Entries : [entry]
    }
    // Settings for the target.
    var target = 
    {
      Number : { "_" : "0123456789"},
      RingDuration : { "_" : 36000 },
      Schedule : schedule
    }
    var targets = [target];
    // Find Me Follow Me settings.
    var settings =
    {
      FmfmTargets : targets,
      FmfmRequireCallerName : { "_" : true },
      FmfmMode : { "_" : "Simultaneous" },
      FmfmEnabled : { "_" : (input == "ON" ? true : false) }
    }
    // Add this information to the data to save to the server.
    data[0]=
    {
      AcfSettings : settings,
      Number : { "_" : subscriberNumber }
    }
    // Save the data to the server.
    connection.saveData(dataType, data, saveSuccess, saveError);
  } else {
    display("No user input");
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve devices information: ");
  errorHandler(connection, error);
}

function saveSuccess(connection) {
  display("Successfully turned Find Me Follow Me ", input);
}

function saveError(connection, error) {
  display("Error turning Find Me Follow Me ", input, ": ");
  errorHandler(connection, error);
}

