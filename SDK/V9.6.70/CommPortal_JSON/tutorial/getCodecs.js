/*
* Fetch the list of supported codecs
*/

// Call the SDK function to fetch the list of supported codecs
connection.fetchCodecs(fetchSuccess, fetchError);

function fetchSuccess(connection, codecs) {
  // Display the name and extension of each supported codec
  for (var i = 0; i < codecs.length; i++)
  {
    var codec = codecs[i];    
    display(codec.CodecName + ": " + codec.CodecExtension);
  }
}

function fetchError(connection, error) {
  display("Failed to retrieve codecs: ");
  errorHandler(connection, error);
}


