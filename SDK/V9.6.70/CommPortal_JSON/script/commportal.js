/**
 * CommPortal JavaScript API v8.1
 * http://www.metaswitch.com/
 * http://innovators.metaswitch.com/
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 */

/**
 * @version 9.5.40
 * @fileoverview
 *
 * <p>
 * The CommPortal JavaScript API provides simplified access to the data made
 * available via the CommPortal JSON interface.
 * </p>
 *
 * <p>
 * Including this file will define just one object in the JavaScript global
 * scope, the {@link CommPortal} object.
 * </p>
 *
 * <p>
 * All interaction with the API is through either the global
 * {@link CommPortal} object, or via other objects returned via the various
 * method calls on that object.
 * </p>
 *
 * <p>
 * The other objects used within the interface are all simple JavaScript
 * objects (they are not "instanceof" anything other than Object), though for
 * documentation purposes they are listed here as if they are in lowercased
 * pseudo classes named thus:
 * </p>
 * <ul>
 *   <li>{@link address}</li>
 *   <li>{@link call}</li>
 *   <li>{@link callstate}</li>
 *   <li>{@link contact}</li>
 *   <li>{@link error}</li>
 *   <li>{@link fax}</li>
 *   <li>{@link voicemail}</li>
 *   <li>{@link greetings}</li>
 *   <li>{@link greeting}</li>
 * </ul>
 *
 * <p>
 * Similarly a pseudo class of {@link callbacks} is used as a documentation
 * device to allow the method signatures of the various callbacks used within
 * the API to be documented.
 * </p>
 *
 * <p>
 * For the sake of convenience, including this file will also define the JSON
 * namespace if that is not already defined, which it does by including the
 * following external file
 * </p>
 * <ul>
 *   <li>json2.js</li>
 * </ul>
 *
 * See <a href="http://innovators.metaswitch.com">The MetaSwitch Innovators Community</a>
 * for more details.
 */

(function()
{
  // Static variables
  var sUniqueId = 0;
  var sRedirectFile = "empty.txt";
  var sLoginPath = "/login.html";

  //CodeForTesting1 -- do not delete this line, it is used for automated testing

  // The CommPortal server interface version we pass on login.  This is the
  // latest version supported by this script.  It's not used in V7.1, since
  // we always specify the version explicitly on each request, but we
  // send it anyway in case it is useful to the server in future versions.
  var LOGIN_VERSION = "9.5.40";

  // The default Client Version value used in logs to the Service Assurance
  // Server. The Client Version should be overwritten by users of the
  // CommPortal SDK by calling setClientVersion(version)
  var DEFAULT_CLIENT_VERSION = "9.5.40 (SDK)";

  // Find where we were loaded from, since we need to be able to find other
  // files in the same directory.
  // Note that since we are being executed as a result of being included
  // then we will normally simply be the last script in the array...
  var scripts = document.getElementsByTagName("script");
  for (var i = scripts.length - 1; i >= 0; i--)
  {
    var script = scripts[i];

    var src = script.src;
    if (src.match(/(^|\/)commportal.js(\?.*)?$/))
    {
      var pathSplit = src.lastIndexOf("/");

      // The sLibPath includes any trailing "/" that may be necessary
      var sLibPath = pathSplit > -1 ? src.substring(0, pathSplit + 1) : "";

      // Commands are anything after the first "?" if present
      if (src.indexOf("?") > -1)
      {
        var sCommands = src.substring(src.indexOf("?") + 1);
      }
      break;
    }
  }

  var sPageProtocolHost = document.location.protocol + "//" +
                          document.location.host;
  var sPagePath = sPageProtocolHost +
                  document.location.pathname.substring(0, document.location.pathname.lastIndexOf("/") + 1);

  /**
   * Convert a URL into an absolute URL.  Relative URLs are assumed to be
   * relative to the current path.  Absolute URLs are unchanged.
   *
   * @private
   * @param {String} url the URL to make absolute
   * @return an absolute URL
   */
  function toAbsoluteUrl(url)
  {
    // Check if the URL is missing or already absolute.
    if ((url) &&
        (!url.match(/^https?:/i)))
    {
      // The URL is relative.  However, it may specify a full path from the
      // root.  Check this.
      if (url.substring(0, 1) == "/")
      {
        // The relative URL starts with a /, so it just needs to be prefixed
        // with the protocol and host of the page.
        url = sPageProtocolHost + url;
      }
      else
      {
        // The relative URL doesn't start with a /, so it needs to be prefixed
        // with the full path to the page.
        url = sPagePath + url;
      }
    }
    // Resolve any .. path components in the URL by seeing if there are any
    // remaining, stripping them out (along with the preceding path components)
    // and then spinning round and trying again.
    while (1)
    {
      // The following regular expression parses the URL into three sections:
      // 1) from the beginning up to and including the / before the path
      //    component before the ..
      // 2) the path component before the .., its /, the .. and the following /
      // 3) the rest of the URL
      //
      // For example, http://example.com/abcdef/../example.html would be parsed
      // as follows:
      // 1) http://example.com/
      // 2) abcdef/../
      // 3) example.html
      //
      // The regular expression works as follows.
      // /.../i - defines a case-insensitive regular expression
      // ^...$  - the regular expression must match the whole URL
      // (https?:\/\/(?:[^/]+\/)+?)
      //        - the first capturing group
      // https? - "http" followed by an optional "s"
      // :\/\/  - "://"
      // (?:[^/]+\/)
      //        - a non-capturing group that matches a single path component
      // [^\/]+\/
      //        - one or more non-/ characters followed by a /
      // +?     - allows for one or more of the previous element with a
      //          non-greedy match (i.e. match as few as possible), required
      //          because otherwise we match the last .. in the URL, not the
      //          first
      // ([^\/]+\/\.\.\/)
      //        - the second capturing group, matches one or more non-/
      //          characters followed by "/../"
      // (.*)   - the third capturing group, matching anything left in the URL
      var match =
                /^(https?:\/\/(?:[^\/]+\/)+?)([^\/]+\/\.\.\/)(.*)$/i.exec(url);
      if (match == null)
      {
        break;
      }
      url = match[1] + match[3];
    }
    return url;
  }

  // We need a valid absolute URL on the same server as the current page to
  // redirect to.  If we were loaded from the same server as the page, try the
  // file in that directory.  If not, just try the file in the same directory
  // as the page was loaded.  See {@link CommPortal#setRedirectUrl} for more.
  sLibPath = toAbsoluteUrl(sLibPath);
  if ((sLibPath) &&
      (sLibPath.substring(0, sPageProtocolHost.length) == sPageProtocolHost))
  {
    var sRedirectUrl = sLibPath + sRedirectFile;
  }
  else
  {
    var sRedirectUrl = sPagePath + sRedirectFile;
  }

  processCommands();

  /** @private */
  function processCommands()
  {
    // If we do not have a body yet, then try again later
    if (!document.body)
    {
      // We delay processing the commands until the body exists
      setTimeout(processCommands, 100);
    }
    else
    {
      // Now work through any commands we may have been given
      if (sCommands)
      {
        var commands = sCommands.split(",");

        for (var i = 0; i < commands.length; i++)
        {
          var command = commands[i];

          // No commands are actually defined yet.
        }
      }
    }
  }

  /**
   * The Main constructor object.
   * <p>
   * The object it returns embodies the full
   * CommPortal library's abilities, but will typically be thought of as an
   * object which simply implements one of the sub-interfaces, such as a
   * connection, or a call.
   * </p>
   *
   * <p>
   * The server URL can take the form "domain/cust" or a more specific path
   * "domain/cust/login.html" if the first option loads something other than
   * the plain login page.
   *
   * <p>
   * Possible flags are:
   * <ul>
   *   <li>{@link #OPTIONS_USE_GET}</li>
   * </ul>
   *
   * <p>The flags parameter is new in version 7.1</p>
   *
   * @class
   * See <a href="http://innovators.metaswitch.com">Innovators</a> for more details.
   *
   * <p>
   * This is the namespace for the CommPortal JavaScript API.
   *
   * <p>
   * There is a general convention followed in naming methods in this API.
   * </p>
   * <ul>
   * <li>Methods named <b>fetchXXX()</b> pass their values asynchrously to the supplied callback</li>
   * <li>Methods named <b>getXXX()</b> or <b>convertXXX()</b> return their values immediately, as the return value</li>
   * </ul>
   *
   * <p>
   * Other action verbs, such as <b>add</b>, <b>modify</b>, <b>delete</b>,
   * <b>save</b> and <b>mark</b> also act asynchronously.
   * </p>
   *
   * <p>
   * Where a method can reasonably be expected to act on either a single
   * item, or a set of items, the parameter will accept either a single
   * item id (as a string), or an Array of such items.
   * </p>
   *
   * @param {String} server [optional] server to connect to
   * @param {Integer} flags [optional] flags to control optional behaviour
   */
  function CommPortal(server, flags)
  {
    // If the first parameter is a string, then it is the server, otherwise
    // we shuffle all the parameters up since it has been omitted
    if (typeof server != "string")
    {
      flags = server;
      server = undefined;
    }

    /**
     * The CommPortal server interface version we use.  This is chosen during
     * login as a result of version negotiation.  All requests we make of the
     * CommPortal Server use this version.
     * @private
     */
    this.interfaceVersion = null;

    /**
     * The CommPortal client version we are using.  This defaults to the
     * version of this script (specified by DEFAULT_CLIENT_VERSION). This can
     * be overwritten by a call to setClientVersion.
     * {See {@link commportal#setClientVersion}}
     * @private
     */
    this.clientVersion = DEFAULT_CLIENT_VERSION;

    /**
     * This private variable stores a callback function which is called in the
     * fetchData method if it is set, and retrieves the requested url.
     * {See {@link callbacks#getRequestCallback}}
     * @private
     */
    this.getRequestCallback = null;

    /**
     * This private variable stores a callback function which is called in the
     * fetchData method if it is set, and retrieves the fetched data.
     * {See {@link callbacks#getResponseCallback}}
     * @private
     */
    this.getResponseCallback = null;

    /**
     * This private variable stores a callback function which is called in the
     * saveData method if it is set, and retrieves the update request form.
     * {See {@link callbacks#updateRequestCallback}}
     * @private
     */
    this.updateRequestCallback = null;

    /**
     * This private variable stores a callback function which is called in the
     * saveData method if it is set, and retrieves the update response redirect url.
     * {See {@link callbacks#updateResponseCallback}}
     * @private
     */
    this.updateResponseCallback = null;

    if (flags)
    {
      /** @private */
      this.useGet = !!(flags & CommPortal.OPTIONS_USE_GET);
    }

    // Variables

    // Default the server if it is not given
    // Server validation is now done in login, which is the first call that
    // can return a failure indication.
    /** @private */
    this.server = server || sPagePath;
    /** @private */
    this.serverLogin = this.server;
    // Remove the path to the login page from the server path
    this.server = this.server.replace(sLoginPath, "");
    /** @private */
    this.originalServer = server;

    this.invalidateCache();

    // Giving each connection a unique id helps for debugging
    /** @private */
    this.id = sUniqueId++;

    /**
     * These private variables store the objects responsible for polling the
     * server to retrieve any event updates.  One is used to poll for updates
     * to telephone calls made by this SDK.  The other is for everything else.
     * A separate one is required for making telephone calls because a
     * different URL is used.  Note that when a telephone call is being made,
     * the polling for all other event types is paused to avoid any issues with
     * there being too many connections to the same server.
     * @private
     */
    this.commPortalEvent = new CommPortalEvent();
    this.callEvent = new CommPortalEvent();

    /**
     * This private variable stores the current call state.
     * @private
     */
    this.callState = CommPortal.CALLSTATE_INITIAL;

    //CodeForTesting2 -- do not delete this line, it is used for automated testing
  }

  // Make the CommPortal object available in the Global scope
  if ("CommPortal" in window)
  {
    alert("Something has already defined the CommPortal object");
  }
  else
  {
    window.CommPortal = CommPortal;
  }

  // If the JSON namespace does not exist, then include it
  if (!("JSON" in window) || !JSON.stringify)
  {
    document.write("<script src='" + sLibPath + "json2.js'></script>");
  }

  // Options flags that can be passed to the constructor.
  // These should be added together if you need to set multiple options.
  // (They are all powers of two, so that they do not clash).

  /**
   * Options: Use GET request (rather than POST) for all URLs.
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.OPTIONS_USE_GET = 1;

  /**
   * Error: Unknown error - no more details available.
   * @type integer
   * @final
   */
  CommPortal.ERROR_UNKNOWN              = 0;
  /**
   * Error: Login window could not pop up.
   * @type integer
   * @final
   */
  CommPortal.ERROR_LOGIN_WINDOW_BLOCKED = 1;
  /**
   * Error: Login window was closed without logging in.
   * @type integer
   * @final
   */
  CommPortal.ERROR_LOGIN_WINDOW_CLOSED  = 2;
  /**
   * Error: Logging in was unsuccessful.
   * @type integer
   * @final
   */
  CommPortal.ERROR_LOGIN_ERROR          = 3;
  /**
   * Error: Operation cannot proceed since you are not logged in.
   * @type integer
   * @final
   */
  CommPortal.ERROR_NOT_LOGGED_IN        = 4;
  /**
   * Error: Cannot have an existing UID when creating a new contact.
   * @type integer
   * @final
   */
  CommPortal.ERROR_SUPPLIED_UID         = 5;
  /**
   * Error: Must include a UID when modifying an existing contact.
   * @type integer
   * @final
   */
  CommPortal.ERROR_MISSING_UID          = 6;
  /**
   * Error: The subscriber does not keep any call lists.
   * @type integer
   * @final
   */
  CommPortal.ERROR_NO_CALLLISTS         = 7;
  /**
   * Error: Making a call gave an error
   * @type integer
   * @final
   */
  CommPortal.ERROR_MAKE_CALL_ERROR      = 8;
  /**
   * Error: Only allowed to have one call in progress.
   * @type integer
   * @final
   */
  CommPortal.ERROR_CALL_IN_PROGRESS     = 9;
  /**
   * Error: There is no current call.
   * @type integer
   * @final
   */
  CommPortal.ERROR_NO_CURRENT_CALL      = 10;
  /**
   * Error: Error reported fetching data from server
   * @type integer
   * @final
   */
  CommPortal.ERROR_DATA_FETCH_ERROR     = 11;
  /**
   * Error: Error reported saving data to server
   * @type integer
   * @final
   */
  CommPortal.ERROR_DATA_SAVE_ERROR      = 12;
  /**
   * Error: Cannot save cleaned data back to the server - it must be in raw format
   * @type integer
   * @final
   */
  CommPortal.ERROR_NOT_RAW_DATA         = 13;
  /**
   * Error: Session state provided on a reconnect is no longer valid
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_SESSION_STATE_INVALID = 14;
  /**
   * Error: Token was generated with a newer SDK version
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_LOGIN_TOKEN_TOO_NEW  = 15;
  /**
   * Error: Server string did not parse
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_SERVER_INVALID_FORMAT = 16;
  /**
   * Error: Action data not recognized by the server
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_ACTION_INVALID_DATA  = 17;
  /**
   * Error: No corresponding call handler currently set
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_NO_CORRESPONDING_HANDLER = 18;
  /**
   * Error: Events are incompatible
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_INCOMPATIBLE_EVENTS  = 19;
  /**
   * Error: Subscription not allowed
   *
   * <p>New in version 7.1</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_SUBSCRIPTION_NOT_ALLOWED  = 20;
  /**
   * Error: Called line is busy.
   *
   * <p>New in version 7.3</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_LINE_BUSY  = 21;
  /**
   * Error: Number to call is invalid
   *
   * <p>New in version 7.3</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_INVALID_NUMBER  = 22;
  /**
   * Error: No answer from called line.
   *
   * <p>New in version 7.3</p>
   *
   * @type integer
   * @final
   */
  CommPortal.ERROR_CALL_NOT_ANSWERED  = 23;

  var commPortalErrors = [];
  commPortalErrors[CommPortal.ERROR_UNKNOWN                 ] = "Unknown error";

  commPortalErrors[CommPortal.ERROR_LOGIN_WINDOW_BLOCKED    ] = "Login window blocked";
  commPortalErrors[CommPortal.ERROR_LOGIN_WINDOW_CLOSED     ] = "Login window closed";
  commPortalErrors[CommPortal.ERROR_LOGIN_ERROR             ] = "Login error";
  commPortalErrors[CommPortal.ERROR_NOT_LOGGED_IN           ] = "Not logged in";

  commPortalErrors[CommPortal.ERROR_SUPPLIED_UID            ] = "Must not include a UID when adding a contact";
  commPortalErrors[CommPortal.ERROR_MISSING_UID             ] = "Need a UID specified when modifying a contact";

  commPortalErrors[CommPortal.ERROR_NO_CALLLISTS            ] = "Subscriber does not keep call lists";

  commPortalErrors[CommPortal.ERROR_MAKE_CALL_ERROR         ] = "Error when making phone call";
  commPortalErrors[CommPortal.ERROR_CALL_IN_PROGRESS        ] = "Another call is already in progress";
  commPortalErrors[CommPortal.ERROR_NO_CURRENT_CALL         ] = "There is no current call";
  commPortalErrors[CommPortal.ERROR_DATA_FETCH_ERROR        ] = "Error fetching data from server";
  commPortalErrors[CommPortal.ERROR_DATA_SAVE_ERROR         ] = "Error saving data to server";
  commPortalErrors[CommPortal.ERROR_NOT_RAW_DATA            ] = "Data to be saved must be in raw wire format";
  commPortalErrors[CommPortal.ERROR_SESSION_STATE_INVALID   ] = "Session state for reconnection was invalid";
  commPortalErrors[CommPortal.ERROR_LOGIN_TOKEN_TOO_NEW     ] = "Token is too new for this version of CommPortal SDK";
  commPortalErrors[CommPortal.ERROR_SERVER_INVALID_FORMAT   ] = "Unable to parse the server string";

  commPortalErrors[CommPortal.ERROR_ACTION_INVALID_DATA     ] = "Action failed";

  commPortalErrors[CommPortal.ERROR_NO_CORRESPONDING_HANDLER] = "No corresponding call handler found";
  commPortalErrors[CommPortal.ERROR_INCOMPATIBLE_EVENTS     ] = "Incompatible combination of events";

  commPortalErrors[CommPortal.ERROR_SUBSCRIPTION_NOT_ALLOWED] = "Subscription not allowed";

  commPortalErrors[CommPortal.ERROR_LINE_BUSY               ] = "The called line is busy";
  commPortalErrors[CommPortal.ERROR_INVALID_NUMBER          ] = "The called number is invalid";
  commPortalErrors[CommPortal.ERROR_CALL_NOT_ANSWERED       ] = "The called line did not answer";

  /**
   * The object type that gets passed back to an error callback.
   *
   * @private
   */
  function CommPortalError(id, extras)
  {
    // We always save the id
    this.id = id;

    // Look up a text version of the error based on its id
    this.message = commPortalErrors[id in commPortalErrors ? id : CommPortal.ERROR_UNKNOWN];

    // There may be additional information provided with the error
    if (extras)
    {
      for (var i in extras)
      {
        // We only take items that have been defined locally - we don't want
        // to copy any fields that may have been added to the global
        // object prototype.
        if (extras.hasOwnProperty(i))
        {
          this[i] = extras[i];
        }
      }
    }
  }

  /**
   * Return an (English) text description of the error.
   *
   * @private
   */
  CommPortalError.prototype.toString = function()
  {
    return this.message;
  };

  /**
   * Queue up an error response to the error callback
   */
  /** @private */
  function queueError(callback, connection, errorId, extras)
  {
    if (callback)
    {
      queueCallback(callback, connection, new CommPortalError(errorId, extras));
    }
  }

  /**
   * Queue up a response to a callback
   */
  /** @private */
  function queueCallback(callback, connection)
  {
    if (callback)
    {
      var args = [];

      // Copy over any values to pass to the callback.
      // The connection object is always the first value.
      for (var i = 1; i < arguments.length; i++)
      {
        args.push(arguments[i]);
      }

      // We use a zero length timeout, to ensure this call is always
      // decoupled from the caller stack frame (ie is always asynchronous)
      setTimeout(function()
      {
        callback.apply(null, args);
      }, 0);
    }
  }

  /**
   * CORE API: Gets the redirect URL.
   *
   * <p>
   * See {@link CommPortal#setRedirectUrl} for details on its purpose and
   * valid values.
   * </p>
   *
   * @return {String} the redirect URL
   */
  CommPortal.getRedirectUrl = function()
  {
    return sRedirectUrl;
  };

  /**
   * CORE API: Sets the redirect URL.
   *
   * <p>
   * The redirect URL must point to a small file on the same domain as the
   * page.  It is used to work around "same-origin policy" restrictions.  If
   * requests to the URL returns an error (e.g. 404 File not Found), some
   * browsers may not notify the CommPortal SDK that requests have been
   * redirected and it will fail to operate correctly.
   * </p>
   *
   * <p>
   * The redirect URL defaults to empty.txt in the same directory as the SDK
   * itself, as long as the SDK is hosted on the same domain as the page.  If
   * the SDK is not hosted on the same domain, it defaults to empty.txt in the
   * same directory as the page.
   * </p>
   *
   * @param {String} redirectUrl the redirect URL
   */
  CommPortal.setRedirectUrl = function(redirectUrl)
  {
    sRedirectUrl = toAbsoluteUrl(redirectUrl);
  };

  /**
   * Setter for getRequestCallback. {See {@link callbacks#getRequestCallback}}
   * @private
   * @param {function} callBack callback receiving requested url
   */
  CommPortal.prototype.setGetRequestCallback = function(callBack)
  {
    this.getRequestCallback = callBack;
  };

  /**
   * Setter for getResponseCallback. {See {@link callbacks#getResponseCallback}}
   * @private
   * @param {function} callBack callback receiving fetched data
   */
  CommPortal.prototype.setGetResponseCallback = function(callBack)
  {
    this.getResponseCallback = callBack;
  };

  /**
   * Setter for updateRequestCallback. {See {@link callbacks#updateRequestCallback}}
   * @private
   * @param {function} callBack callback receiving update form
   */
  CommPortal.prototype.setUpdateRequestCallback = function(callBack)
  {
    this.updateRequestCallback = callBack;
  };

  /**
   * Setter for updateResponseCallback. {See {@link callbacks#updateResponseCallback}}
   * @private
   * @param {function} callBack callback receiving url
   */
  CommPortal.prototype.setUpdateResponseCallback = function(callBack)
  {
    this.updateResponseCallback = callBack;
  };

  /**
   * Setter for clientVersion.
   * @param {version} Client version to use.
   */
  CommPortal.prototype.setClientVersion = function(version)
  {
    this.clientVersion = version;
  };

  /**
   * CORE API: Logs in to CommPortal, thereby establishing a current session.
   *
   * <p>
   * If no authenication details are given, pops up a new frame to do the login.
   * Thus should always be called as a result of user action (click)
   * to prevent this being stopped by a popup blocker.
   * </p>
   *
   * <p>
   * As an alternative, authentication details can be provided in the the form
   * of one string, which is treated as a persistent token, or as two strings
   * representing subscriber number and password (insecure as this is).
   * In these cases, login does not cause a popup to appear, and no such
   * restriction on only calling this as a result of user action is necessary.
   * </p>
   *
   * <p>
   * The optional appId tells the CommPortal server the name of the
   * application, so that it can both log it and prioritize requests better
   * when under load (if the server in use has prioritization configured).
   * Application ids are limited to 32 characters in length, from the set of
   * capital and lower case letters, digits, and the underscore character.
   * The prefix "MS_" is reserved.
   * </p>
   *
   * <p>The ability to pass an application id, and to log in using number and
   * password are new in version 7.1</p>
   *
   * @see #logout
   *
   * @param {String} appId [optional] application id for this application
   * @param {function} successCallback callback called when successfully logged in (See {@link callbacks#loginCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   * @param {String} tokenOrNumber [optional] persistent token to perform a
   *   login without the user needing to reenter their credentials,
   *   or if followed by another optional parameter, the phone number of a
   *   subscriber being authenticated via phone number and password parameters
   * @param {String} password [optional] password of subscriber
   */
  CommPortal.prototype.login = function(appId,
                                        successCallback,
                                        failureCallback,
                                        tokenOrNumber,
                                        password)
  {
    // Only login to a valid server url.
    if (parseServerUrl(this.server))
    {
      var windowName = createUniqueId("login");

      if (typeof appId != "string")
      {
        // The first parameter is not a string, so we assume that the optional
        // appId has not been provided, and juggle all the other parameters
        password = tokenOrNumber;
        tokenOrNumber = failureCallback;
        failureCallback = successCallback;
        successCallback = appId;
        appId = undefined;
      }

      // We check which of the optional parameters were given, to work out which
      // behaviour is the required one
      if (password)
      {
        var number = tokenOrNumber;
        var token = undefined;
      }
      else if (tokenOrNumber)
      {
        token = tokenOrNumber;
      }

      // The server to log in to.
      // this.server will be updated later if login succeeds with a redirect
      var loginServer = this.serverLogin;

      // We need to record whether we logged in using a token or not, since we
      // must not request a token when we already logged in using a token.
      this.usedToken = !!token;

      if (tokenOrNumber)
      {
        // We have authentication details, so try to log in using those

        loginServer = this.server;

        // Create a hidden iframe
        var loginWindow = createiFrame(windowName);

        // Compute the basic context info specifying the client version
        var contextInfoVal = getSASBasicContextInfo(this);

        // Some fields are common to both types of authenticated login
        var fields =
        {
            version : LOGIN_VERSION,
            redirectTo : sRedirectUrl,
            errorRedirectTo : sRedirectUrl,
            ContextInfo : contextInfoVal
        };

        if (appId)
        {
          fields.ApplicationID = appId;
        }

        // Add the method specific fields
        if (token)
        {
          // Produce the bare token (PAT) needed to log in
          if (token.match(/^!CPSDK,/))
          {
            // Version 1 or later token
            var tokenparts = token.split(/,/);

            if (tokenparts[1] == 1)
            {
              // Version 1 token: !CPSDK,1,server,PAT
              loginServer = patchServerUrl(this.server, tokenparts[2]);
              token = tokenparts[3];
            }
            else
            {
              // Unsupported token version - fail login.
              if (failureCallback)
              {
                // Call the error callback asynchronously
                queueError(failureCallback,
                           this,
                           CommPortal.ERROR_LOGIN_TOKEN_TOO_NEW);
              }

              // We hit an error, so need to make sure we don't actually try
              // and log in
              var skipLogin = true;
            }
          }

          fields.Encrypted = token;
        }
        else
        {
          fields.DirectoryNumber = number;
          fields.Password = password;
        }

        if (!skipLogin)
        {
          if (this.useGet)
          {
            // We are using a GET request to simulate the preferred form submission
            // so we need all the fields in the url
            var urlFields = encodeFieldsForURL(fields);

            var previousURL = simulateSubmit(loginWindow,
                                             this.server + "/login?" + urlFields);
          }
          else
          {
            // Create a form whose response will be sent to the iframe
            var loginForm = createForm("CommPortal_passwordLoginSenderForm",
                                       loginWindow,
                                       loginServer + "/login",
                                       fields);

            // Submit the form we created
            loginForm.submit();
          }
        }
      }
      else
      {
        // No token means do the redirected login.
        var windowURL = loginServer +
                        "?redirectTo=" + encodeURIComponent(sRedirectUrl) +
                        "&errorRedirectTo=" + encodeURIComponent(sRedirectUrl) +
                        "&version=" + LOGIN_VERSION;

        if (appId)
        {
          windowURL += "&ApplicationID=" + appId;
        }

        var loginWindow =
          window.open(windowURL,
                      windowName,
                      "width=340, height=347, scrollbars=no, resizable=yes, toolbar=no, location=yes, status=no");
        var previousURL = windowURL;
      }

      if (!loginWindow)
      {
        // We failed to get a popup window, which happens when a popup blocker
        // prevents the window opening.
        if (failureCallback)
        {
          // Call the error callback asynchronously
          queueError(failureCallback, this, CommPortal.ERROR_LOGIN_WINDOW_BLOCK);
        }
        else
        {
          alert("Login appears to have been blocked by a popup blocker");
        }
      }
      else
      {
        // Start looking for the response
        pollForResponse(this,
                        loginWindow,
                        successCallback,
                        failureCallback,
                        handleLoginURL,
                        previousURL);
      }
    }
    else
    {
      // The server name given did not parse
      queueError(failureCallback,
                 this,
                 CommPortal.ERROR_SERVER_INVALID_FORMAT);
    }

    /**
     * Callback when the login response is available
     */
    function handleLoginURL(url, connection, loginWindow, successCallback, failureCallback)
    {
      // Check if the URL has changed to the redirect URL that we expect.
      if (url.substring(0, sRedirectUrl.length) == sRedirectUrl)
      {
        if (loginWindow.close)
        {
          // This is a real window (not an iFrame), so we close it
          loginWindow.close();
        }
        else
        {
          // Remove the iframe and any login form from the DOM
          removeElement(loginWindow);
          removeElement(loginForm);
        }

        if (connection.usedToken)
        {
          // Save the token that was used to login
          connection.token = token;
        }

        // Have a look at the URL parameters
        var queryParams = parseQueryStringParams(url);

        if (queryParams.session)
        {
          // We got a session ID, login succeeded
          connection.sessionId = queryParams.session;

          if (queryParams.redirectDomain)
          {
            // We got redirected.  Update our records with the new server.
            connection.server = patchServerUrl(connection.server,
                                               queryParams.redirectDomain);
          }

          if (queryParams.latestVersion)
          {
            // The server is reporting its latest supported version to us.
            connection.interfaceVersion = queryParams.latestVersion;
          }
          else
          {
            // The server isn't reporting its latest supported version.  It must
            // be V7.0 or earlier.  We only support V7.0 and later, so we assume
            // V7.0.
            connection.interfaceVersion = "7.0";
          }

          if (successCallback)
          {
            // Callbacks to be called after we finish retrieving the required
            // data
            function callback()
            {
              connection.callEvent.fetchRequiredData(connection, callback2);
            }
            function callback2()
            {
              successCallback(connection, connection.sessionId);
            }

            // Before we call the success callback we fetch some required data
            // for the CommPortal events
            connection.commPortalEvent.fetchRequiredData(connection, callback);
          }
        }
        else
        {
          // Something went wrong - we may have some more details supplied via
          // an error parameter
          var error = queryParams.error;
          var extras = error ? { response : error } : undefined;

          queueError(failureCallback,
                     connection,
                     CommPortal.ERROR_LOGIN_ERROR,
                     extras);
        }
      }
      else
      {
        // The URL changed, but not to the value we were looking for.  Poll
        // again.
        pollForResponse(connection,
                        loginWindow,
                        successCallback,
                        failureCallback,
                        handleLoginURL,
                        url);

      }
    }
  };

  /**
   * Parse a server URL into its components:
   *
   * <p><table>
   * <tr><td>https:// <td>here.example.org  <td>/default/
   * <tr><td>{scheme} <td>{server}          <td>{brand}
   * </table>
   *
   * Note that the server part does not have the slashes in it - but the scheme
   * end with a slash, and the brand begins with one.
   *
   * @param {String} url The URL to parse
   * @return four-element array [url, scheme, server, brand], or null if url
   *   does not parse into the three components
   * @type String[]
   *
   * @private
   */
  function parseServerUrl(url)
  {
    // We allow for both http and https
    return url.match(/^(https?\:\/\/)([^\/]+)(.*)$/);
  }

  //CodeForTesting3 -- do not delete this line, it is used for automated testing

  /**
   * Redirect a server URL to a different server.
   *
   * @param {String} url the old server URL
   * @param {String} server the new server
   * @return the new server URL
   * @type String
   *
   * @private
   */
  function patchServerUrl(url, server)
  {
    // We replace the <server> in, e.g., https://<server>/default/,
    // while preserving the same scheme and path.
    var urlparts = parseServerUrl(url);
    return urlparts[1] + server + urlparts[3];
  }

  /**
   * CORE API: Reconnect to an existing session
   * <p>
   * Useful if the client can save the session state locally over a page
   * refresh, since reconnecting to the open session takes less server
   * resources than logging in again.
   * </p>
   *
   * <p>New in version 7.1</p>
   *
   * @see #getSessionState
   *
   * @param {String} sessionState
   *   session state that was previously valid
   * @param {function} successCallback
   *   callback called if session is valid (See {@link callbacks#successCallback})
   * @param {function} failureCallback
   *   callback called if session is invalid (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.reconnect = function(sessionState, successCallback, failureCallback)
  {
    // Ensure we start from a clean cache state
    this.invalidateCache();

    if (sessionState)
    {
      if (sessionState.indexOf("|") != -1)
      {
        // We have a valid sessionState.
        var parts = sessionState.split("|");

        if (parts.length == 3 && parseServerUrl(this.originalServer))
        {
          // The session state holds three pieces of information:
          // - the server (this is presumably a federated server setup)
          // - the session id
          // - the interface version.
          //
          // Impersonate the given server, session and interface version.
          this.server = patchServerUrl(this.originalServer, parts[0]);
          this.sessionId = parts[1];
          this.interfaceVersion = parts[2];
        }
        else if (parts.length == 2)
        {
          // The session state holds two pieces of information:
          // - the session id
          // - the interface version.
          //
          // Impersonate the session and interface version.
          this.sessionId = parts[0];
          this.interfaceVersion = parts[1];
        }
        else
        {
          // The session state was not in the format we expected, or does not
          // make sense with the server passed to the constructor, so flag to
          // return an error
          sessionState = null;
        }
      }
      else
      {

        // The session state was not in the format we expected (it should have
        // had at least one '|' in it), so flag to return an error.
        sessionState = null;
      }

      if (sessionState)
      {
        // We call on to fetchSubscriberNumber, which will validate whether the
        // sessionId we are impersonating is indeed valid
        this.fetchSubscriberNumber(reconnected, reconnectFailed);
      }
    }

    if (!sessionState)
    {
      // Call the error callback asynchronously
      queueError(failureCallback,
                 this,
                 CommPortal.ERROR_SESSION_STATE_INVALID);
    }

    function reconnected(connection)
    {
      successCallback(connection);
    }

    function reconnectFailed(connection)
    {
      // Undo the impersonation
      this.server = this.originalServer;
      if (this.sessionId)
      {
        delete this.sessionId;
      }

      // Call the error callback asynchronously
      queueError(failureCallback,
                 this,
                 CommPortal.ERROR_SESSION_STATE_INVALID);
    }
  };

  /**
   * CORE API: Synchronous call to return the current session id string.
   * <p>
   * The session id may be helpful if you are trying to manually form
   * urls outside of the SDK, but otherwise is of little use.
   * </p>
   *
   * @return the session id string, or undefined if this object does not
   *   represent a valid, logged in session
   * @type String
   *
   * @see #login
   * @see #getSessionState
   */
  CommPortal.prototype.getSessionId = function()
  {
    return this.sessionId;
  };

  /**
   * CORE API: Synchronous call to return the current session state string.
   * <p>
   * The session state may be passed back in to {@link #reconnect}.
   * </p>
   *
   * <p>New in version 7.1</p>
   *
   * @return the session state string, or undefined if this object does not
   *   represent a valid, logged in session
   * @type String
   *
   * @see #reconnect
   * @see #getSessionId
   */
  CommPortal.prototype.getSessionState = function()
  {
    // The sessionState is undefined unless we have a session ID.
    if (this.sessionId)
    {
      // The session state is at least the session ID and the interface
      // version, but may also have the server name prepended below.
      var sessionState = this.sessionId + "|" + this.interfaceVersion;

      var server = (parseServerUrl(this.server))[2];
      var originalServer = (parseServerUrl(this.originalServer))[2];

      if (server != originalServer)
      {
        // The session state also needs to have the server prepended
        sessionState = server + "|" + sessionState;
      }
    }

    return sessionState;
  };

  /**
   * CORE API: Synchronous call to return the current call id string.
   *
   * @return the call id string, or undefined if there is no call currently
   *   in progress on this connection
   * @type String
   *
   * @see #makeCall
   */
  CommPortal.prototype.getCallId = function()
  {
    return this.callId;
  };

  /**
   * CORE API: Logs out of the current session.
   *
   * @see #login
   */
  CommPortal.prototype.logout = function()
  {
    // Logout can be achived simply by doing a get to the logout servlet
    var img = new Image();
    img.src = this.server + "/session" + this.sessionId + "/logout";

    // We are logged out, so delete the session id
    delete this.sessionId;

    // Discard any data we may have cached
    this.invalidateCache();
  };

  /**
   * Gets access to the authentication token for the current logged in
   * session.
   * <p>
   * The token is passed asynchronously to the provided callback when
   * it is ready.
   * </p>
   *
   * @param {function} successCallback callback called when token arrives (See {@link callbacks#tokenCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchToken = function(successCallback, failureCallback)
  {
    var connection = this;

    if (this.sessionId)
    {
      if (this.token)
      {
        // We already have a token, so return that.  The token could have
        // come from a previous call to fetchToken, or it could have come from
        // the fact that we logged in using a token, in which case we cannot
        // get another token - so must return the one we originally used.
        returnToken();
      }
      else
      {
        // Create a uniquely named callback function for this request.
        var callbackName = createUniqueId("callback");
        var callback = "CommPortal." + callbackName;

        // Register the uniquely-named callback
        CommPortal[callbackName] = function(structure)
        {
          try
          {
            // Save the token
            connection.token = structure.Encrypted;

            returnToken();
          }
          finally
          {
            // Remove the now unneeded script tag.
            removeElement(scriptTag);

            // This callback is now finished with, get rid of it.
            delete CommPortal[callbackName];
          }
        };

        // Calculate the CommPortal token URL
        var url = this.server +
                  "/session" +
                  this.sessionId +
                  "/line/token?Password=*" +
                  "&callback=" + callback +
                  "&version=" + this.interfaceVersion;

        // Add a new script tag with that URL.  Once loaded, it will kick off our
        // callback.
        var scriptTag = appendScriptTag(url);
      }
    }
    else
    {
      // Queue a callback, so that it is never called synchronously
      queueError(failureCallback,
                 connection,
                 CommPortal.ERROR_NOT_LOGGED_IN);
    }

    function returnToken()
    {
      // Calculate the token to return.  As well as the PAT itself, this also
      // contains the server that issued it (since we may have been redirected
      // on login).
      //
      // Token formats:
      //
      // Version 0:   PAT
      // Version 1:   !CPSDK,1,server,PAT
      //
      var fullToken = "!CPSDK,1," +
                      parseServerUrl(connection.server)[2] + "," +
                      connection.token;

      queueCallback(successCallback,
                    connection,
                    fullToken);
    }
  };

  /**
   * CORE API: Fetches data from the CommPortal server via the JSON interface.
   *
   * <p>
   * The data passed to the successCallback is a flattened and cleaned up
   * version of the data received over the wire.
   * </p>
   * <p>
   * Where multiple pieces of data are being fetched, one callback is called
   * for each piece of data.
   * </p>
   *
   * @param {String/String[]} dataTypes which data should be fetched.
   *   Can consist of a single value, or an array of such values.
   * @param {function} successCallback callback called for each item of data
   *   successfully fetched (See {@link callbacks#fetchCallback})
   * @param {function} failureCallback callback called for each error that occurs
   *
   * @see #fetchRawData
   */
  CommPortal.prototype.fetchData = function(dataTypes,
                                            successCallback,
                                            failureCallback)
  {
    return innerFetchData(this,
                          dataTypes,
                          successCallback,
                          failureCallback);
  };
  /**
   * CORE API: Fetches raw data from the CommPortal server via the JSON interface.
   *
   * <p>
   * The data passed to the successCallback is exactly as received over the
   * wire, and as such is suitable for passing back in to the saveData method.
   * </p>
   * <p>
   * Where multiple pieces of data are being fetched, one callback is called
   * for each piece of data.
   * </p>
   *
   * @param {String/String[]} dataTypes which data should be fetched.
   *   Can consist of a single value, or an array of such values.
   * @param {function} successCallback callback called for each item of data
   *   successfully fetched (See {@link callbacks#fetchCallback})
   * @param {function} failureCallback callback called for each error that occurs
   *
   * @see #fetchData
   * @see #saveData
   */
  CommPortal.prototype.fetchRawData = function(dataTypes,
                                               successCallback,
                                               failureCallback)
  {
    return innerFetchData(this,
                          dataTypes,
                          true,
                          successCallback,
                          failureCallback);
  };

  /** @private */
  function innerFetchData(connection,
                          dataTypes,
                          returnRaw,
                          successCallback,
                          failureCallback)
  {
    // Process the parameters
    if (typeof returnRaw == "function")
    {
      // The optional returnRaw parameter was omitted, so shuffle the other
      // parameters around
      failureCallback = successCallback;
      successCallback = returnRaw;
      returnRaw = false;
    }

    if (connection.sessionId)
    {
      // Create a uniquely named callback function for this request.
      var callbackName = createUniqueId("callback");
      var callback = "CommPortal." + callbackName;

      if (typeof dataTypes != "object")
      {
        dataTypes = [dataTypes];
      }

      // Register the uniquely-named callback. The parameters are part of the
      // CommPortal JSON interface.
      CommPortal[callbackName] = function(objectIdentity,
                                          dataType,
                                          getData,
                                          getErrors,
                                          updateData,
                                          updateErrors)
      {
        // Call the getReponseCallback function if it is set.
        if (connection.getResponseCallback)
        {
          connection.getResponseCallback(callbackName,
                                         objectIdentity,
                                         dataType,
                                         getData,
                                         getErrors,
                                         updateData,
                                         updateErrors);
        }

        // Wrap the callbacks in a try block, to protect against badly written
        // callback functions.
        try
        {
          if (!getErrors)
          {
            if (successCallback)
            {
              if (returnRaw)
              {
                // The callback requires raw data
                var data = getData;
              }
              else
              {
                // Convert the RAW JSON style data into a flatter version
                var data = flattenDataObject(getData, dataType);

                // There may be some other special casing we need to do
                data = handleSpecialCases(dataType, data);

                // Mark this as processed data, so that we can recognise it if
                // it is passed back into saveData
                data._cleaned = true;
              }

              queueCallback(successCallback,
                            connection,
                            dataType,
                            data,
                            objectIdentity);
            }
          }
          else
          {
            // TODO Check error type and mark connection as failed if sessionExpired, etc.
            queueError(failureCallback,
                       connection,
                       CommPortal.ERROR_DATA_FETCH_ERROR,
                       { dataType : dataType,
                         errors : getErrors,
                         objectIdentity : objectIdentity });
          }
        }
        finally
        {
          if (--CommPortal[callbackName].count === 0)
          {
            // Remove the now unneeded script tags.
            for (var i = 0; i < scriptTags.length; i++)
            {
              removeElement(scriptTags[i]);
            }

            // This callback is now finished with, get rid of it.
            delete CommPortal[callbackName];
          }
        }
      };
      CommPortal[callbackName].count = dataTypes.length;

      // If too many data objects are requested at once, the URL will be too
      // long.  Thus we split large requests up into multiple requests.
      var scriptTags = [];
      var remainingTypes = dataTypes.join(",");
      var splitSize = 1000;
      while (remainingTypes)
      {
        if (remainingTypes.length > splitSize)
        {
          var comma = remainingTypes.indexOf(",", splitSize);
          if (comma == -1)
          {
            comma = remainingTypes.lastIndexOf(",", splitSize);
          }

          var dataType = remainingTypes.substring(0, comma);
          remainingTypes = remainingTypes.substring(comma + 1);
        }
        else
        {
          dataType = remainingTypes;
          remainingTypes = "";
        }

        // Calculate the CommPortal data URL
        var url = connection.server +
                  "/session" +
                  connection.sessionId +
                  "/data?data=" + encodeURIComponent(dataType) +
                  "&callback=" + callback +
                  "&version=" + connection.interfaceVersion;

        // Add a new script tag with that URL.  Once loaded, it will kick off our
        // callback.
        //
        // TODO Start a timeout timer to detect connection failure.
        scriptTags.push(appendScriptTag(url));

        // Call the getRequestCallback function if it is set.
        if (connection.getRequestCallback)
        {
          connection.getRequestCallback(url);
        }
      }
    }
    else
    {
      queueError(failureCallback,
                 connection,
                 CommPortal.ERROR_NOT_LOGGED_IN);
    }
  }

  /**
   * Gets the logged in subscriber's phone number.
   * <p>
   * The number is passed asynchronously to the provided callback when ready.
   * </p>
   *
   * @param {function} successCallback callback called to pass the number (See {@link callbacks#numberCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchSubscriberNumber = function(successCallback,
                                                        failureCallback)
  {
    var connection = this;

    if (this.cache.session)
    {
      returnCachedData();
    }
    else
    {
      // We must query the session data
      this.fetchData("Session",
                     processSession,
                     failureCallback);
    }

    function processSession(connection, dataType, getData, objectIdentity)
    {
      // Cache the data
      connection.cache.session = getData;

      // Fix up the COS and DN in the commPortalEvent, so that tracking of call
      // events (in addEventList and getEventCallback) works correctly for 
      // reconnects.
      connection.callEvent.classOfService = connection.cache.cos;
      if (connection.cache.session.ManagedSubscribers[0])
      {
        connection.callEvent.defaultLine =
          connection.cache.session.ManagedSubscribers[0].DirectoryNumber;
      }

      // Make the callback now
      returnCachedData();
    }

    function returnCachedData()
    {
      queueCallback(successCallback,
                    connection,
                    connection.cache.session.ManagedSubscribers[0].DirectoryNumber);
    }
  };

  /**
   * Gets the logged in subscriber's name.
   * <p>
   * The name is passed asynchronously to the provided callback when ready.
   * </p>
   *
   * <p>New in version 7.1</p>
   *
   * @param {function} successCallback callback called to pass the number (See {@link callbacks#nameCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchSubscriberName = function(successCallback,
                                                      failureCallback)
  {
    var connection = this;

    if (this.cache.subscriberName)
    {
      // We already have the subscriber name cached. Just return the name
      // from the cache.
      returnCachedName();
    }
    else if (compareVersion(this.interfaceVersion, "7.3") >= 0)
    {
      // Our version is at least 7.3.
      // Get the name from the Msrb_Subscriber_BaseInformation indication
      // added in V7.3. This should return data for both EAS-only subscribers
      // and subscribers on both a CFS and EAS.
      this.fetchData("Msrb_Subscriber_BaseInformation",
                     processSubscriberName,
                     failureCallback);
    }
    else
    {
      // Get the name from
      // Meta_Subscriber_MetaSphere_SubscriberSettings. This should return data
      // for both EAS-only subscribers and subscribers on both a CFS and EAS.
      this.fetchData("Meta_Subscriber_MetaSphere_SubscriberSettings",
                     processSubscriberName,
                     failureCallback);
    }

    function processSubscriberName(connection, dataType, getData, objectIdentity)
    {
      // Cache the name.
      if (dataType == "Meta_Subscriber_MetaSphere_SubscriberSettings")
      {
        connection.cache.subscriberName = getData.DisplayName;
      }
      else
      {
        // We got the Msrb_Subscriber_BaseInformation data.
        connection.cache.subscriberName = getData.SubscriberName;
      }

      // Return the cached name on our success callback.
      returnCachedName();
    }

    function returnCachedName()
    {
      queueCallback(successCallback,
                    connection,
                    connection.cache.subscriberName);
    }
  };

  /**
   * Gets the count of voicemails.
   * <p>
   * The count is passed asynchronously to the provided callback when ready.
   * </p>
   *
   * @param {function} successCallback callback called to pass the counts (See {@link callbacks#countsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #fetchVoicemails
   */
  CommPortal.prototype.fetchVoicemailCount = function(successCallback,
                                                      failureCallback)
  {
    // Although the protocol contains a messages count, that includes messages
    // other than voicemails.  Thus to get a true count, we need to call the
    // method that really fetches the full list, and then count the items in
    // that.  We trust that caching will make this as efficient as it can.
    this.fetchVoicemails(countVoicemails,
                         failureCallback);

    function countVoicemails(connection, voicemails)
    {
      var total = voicemails.length;
      var unheard = total;

      for (var i = 0; i < total; i++)
      {
        if (voicemails[i].Read)
        {
          unheard--;
        }
      }

      successCallback(connection, total, unheard);
    }
  };

  /**
   * Gets access to the CommPortal voicemails, in an easily accessed simple
   * array format.
   * <p>
   * The voicemails are passed asynchronously to the provided callback when
   * they are ready.
   * </p>
   *
   * @param {function} successCallback callback called to pass the voicemails (See {@link callbacks#voicemailsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   * @param {String/String[]} (optional) a single codec or an array of codecs which the caller can handle. If not provided this will be assume to be g711u.
   *
   * @see #fetchVoicemailCount
   */
  CommPortal.prototype.fetchVoicemails = function(successCallback,
                                                  failureCallback,
                                                  codecs)
  {
    var connection = this;
    
    if (this.cache.voicemails)
    {
      returnCachedData();
    }
    else
    {
      // We must query the voicemails
      this.fetchData("Meta_Subscriber_MetaSphere_VoicemailMessages",
                     processVoicemails,
                     failureCallback);
    }

    function processVoicemails(connection, dataType, getData, objectIdentity)
    {
      // We return a simple array, of simple objects
      var voicemails = [];

      for (var i = 0; i < getData.length; i++)
      {
        var message = getData[i];

        if (message.ReportType || message.SystemMessageType)
        {
          // It's a system message or a report message - ignore it
        }
        else
        {
          voicemails.push(message);
        }
      }

      connection.cache.voicemails = voicemails;

      returnCachedData();
    }

    function returnCachedData()
    {
      for (var i = 0; i < connection.cache.voicemails.length; i++)
      {        
        var message = connection.cache.voicemails[i];

        // Add a field that gives the URL to get the audio file from
        message.AudioFile =
          connection.server + "/session" + connection.sessionId + "/line/" +
          "voicemail.fetch?id=" + message.Id +
          "&version=" + connection.interfaceVersion + 
          "&codec=" + codecReqStr(codecs);
      }
    
      queueCallback(successCallback,
                    connection,
                    connection.cache.voicemails);
    }
  };
  
  /** @private */
  function codecReqStr(codecs)
  {
    var codecStr = "";

    if (typeof codecs !== 'undefined')
    {
      // We may have been passed a single codec - treat it as if we were given a
      // single entry array
      if (typeof codecs != "object")
      {
        codecs = [codecs];
      }

      for (var i = 0; i < codecs.length; i++)
      {
        codecStr += codecs[i];
        if (i < (codecs.length - 1))
        {
          codecStr += ";";
        }
      }
    }
    
    return codecStr;
  }

  /**
   * Gets the count of faxes.
   * <p>
   * The count is passed asynchronously to the provided callback when ready.
   * </p>
   *
   * @param {function} successCallback callback called to pass the counts (See {@link callbacks#countsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #fetchFaxes
   */
  CommPortal.prototype.fetchFaxCount = function(successCallback,
                                                failureCallback)
  {
    // Although the protocol contains a messages count, that includes messages
    // other than faxes.  Thus to get a true count, we need to call the
    // method that really fetches the full list, and then count the items in
    // that.  We trust that caching will make this as efficient as it can.
    this.fetchFaxes(countFaxes,
                    failureCallback);

    function countFaxes(connection, faxes)
    {
      var total = faxes.length;
      var unheard = total;

      for (var i = 0; i < total; i++)
      {
        if (faxes[i].Read)
        {
          unheard--;
        }
      }

      successCallback(connection, total, unheard);
    }
  };

  /**
   * Gets access to the CommPortal faxes, in an easily accessed simple
   * array format.
   * <p>
   * The faxes are passed asynchronously to the provided callback when
   * they are ready.
   * </p>
   *
   * @param {function} successCallback callback called to pass the faxes (See {@link callbacks#faxesCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #fetchFaxCount
   */
  CommPortal.prototype.fetchFaxes = function(successCallback,
                                             failureCallback)
  {
    var connection = this;

    if (this.cache.faxes)
    {
      returnCachedData();
    }
    else
    {
      // We must query the faxes
      this.fetchData("Meta_Subscriber_MetaSphere_FaxMessages",
                     processFaxes,
                     failureCallback);
    }

    function processFaxes(connection, dataType, getData, objectIdentity)
    {
      // We return a simple array, of simple objects
      var faxes = [];

      for (var i = 0; i < getData.length; i++)
      {
        var message = getData[i];

        if (message.ReportType || message.SystemMessageType)
        {
          // It's a system message or a report message - ignore it
        }
        else
        {
          faxes.push(message);

          // Add a field that gives the URL to get the TIFF file from
          message.ImageFile =
            connection.server + "/session" + connection.sessionId + "/line/" +
            "fax.tif?id=" + message.Id +
            "&version=" + connection.interfaceVersion;
          // Add a field that gives the URL to get the PDF file from
          message.ImageFilePDF = message.ImageFile.replace("fax.tif?id=",
                                                           "fax.pdf?id=");
        }
      }

      connection.cache.faxes = faxes;

      returnCachedData();
    }

    function returnCachedData()
    {
      queueCallback(successCallback,
                    connection,
                    connection.cache.faxes);
    }
  };


  /**
   * Converts a string format date, as contained in a
   * {@link voicemail}, {@link fax}, or {@link call}
   * entry into a JavaScript Date object.
   *
   * @param {String} dateString date as a string
   *
   * @return the date as a JavaScript Date object
   * @type Date
   */
  CommPortal.prototype.convertDate = function(dateString)
  {
    // Split the string into individual fields
    var fields = dateString.split(/[. :]/);

    // There are a number of formats we need to handle
    //  YYYY.MM.DD hh.mm.ss   - (all numeric) used by voicemails
    //  DD mth YY hh:mm:ss    - where mth is alphabetic, used by call lists
    if (isNaN(parseInt(fields[1], 10)))
    {
      // The month is non numeric
      var day = parseInt(fields[0], 10);
      var year = 2000 + parseInt(fields[2], 10);

      // The month names are always in English
      var month = "JanFebMarAprMayJunJulAugSepOctNovDec".indexOf(fields[1]) / 3;
    }
    else
    {
      // We assume everything is numeric
      var year = parseInt(fields[0], 10);
      var month = parseInt(fields[1], 10) - 1;
      var day = parseInt(fields[2], 10);
    }

    var date = new Date(year,
                        month,
                        day,
                        parseInt(fields[3], 10),
                        parseInt(fields[4], 10),
                        parseInt(fields[5], 10));
    return date;
  };

  /**
   * Marks one or a number of voicemails as heard.
   *
   * @param {String/String[]} voicemailIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #markVoicemailsAsUnheard
   * @see #confirmVoicemailsHeard
   */
  CommPortal.prototype.markVoicemailsAsHeard = function(voicemailIds,
                                                        successCallback,
                                                        failureCallback)
  {
    handleMessageAction(this,
                        voicemailIds,
                        successCallback,
                        failureCallback,
                        "MessagesToMarkAsRead");
  };

  /**
   * Marks one or a number of voicemails as unheard.
   *
   * @param {String/String[]} voicemailIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #markVoicemailsAsHeard
   */
  CommPortal.prototype.markVoicemailsAsUnheard = function(voicemailIds,
                                                          successCallback,
                                                          failureCallback)
  {
    handleMessageAction(this,
                        voicemailIds,
                        successCallback,
                        failureCallback,
                        "MessagesToMarkAsUnread");
  };

  /**
   * Deletes one or a number of voicemails.
   *
   * @param {String/String[]} voicemailIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.deleteVoicemails = function(voicemailIds,
                                                   successCallback,
                                                   failureCallback)
  {
    handleMessageAction(this,
                        voicemailIds,
                        successCallback,
                        failureCallback,
                        "MessagesToDelete");
  };

  /**
   * A synchronous call (no callbacks) that indicates that the passed voicemails
   * have (probably) been heard, without making an explicit call to the
   * server to set this state.  Allows the API to keep its state in sync.
   *
   * <p>
   * Should be called whenever the {@link voicemail#AudioFile} field has been
   * used to actually fetch the referenced file.
   * </p>
   *
   * @param {String/String[]} voicemailIds single id as string, or array of ids
   *
   * @see #markVoicemailsAsHeard
   */
  CommPortal.prototype.confirmVoicemailsHeard = function(voicemailIds)
  {
    // We don't do anything clever with the passed ids - we simply invalidate
    // the voicemail related part of the cache.
    delete this.cache.voicemails;
  };

  /**
   * Uploads a voicemail audio file to the CommPortal server.
   *
   * <p>New in version 7.3</p>
   *
   * @param {string} formId The ID of the HTML <form> element containing the
   *                        file input to be submitted to the server.
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.uploadVoicemailAudio = function(formId,
                                                       successCallback,
                                                       failureCallback)
  {
    uploadAudioFile(this, "voicemail.wav", "newvoicemail",
                    formId, successCallback, failureCallback);
  };

  /**
   * Send a voicemail using the previously uploaded audio file.
   * See {@link commportal#uploadVoicemailAudio} for more details.
   *
   * <p>New in version 7.3</p>
   *
   * @param {string} destNumber The number to send the voicemail to.
   * @param {boolean} isUrgent Whether the voicemail should be marked urgent.
   * @param {boolean} isPrivate Whether the voicemail should be marked private.
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.sendVoicemail = function(destNumber,
                                                isUrgent,
                                                isPrivate,
                                                successCallback,
                                                failureCallback)
  {
    // First we clear the cache of fetched voicemails
    delete this.cache.voicemails;

    // Construct a new voicemail JSON object
    var newVoicemail =
    {
      "DestinationNumbers": [
        {"_" : destNumber}
      ],
      "Urgent": {"_" : isUrgent},
      "Private": {"_" : isPrivate},
      "MessageType": {"_" : "VOICEMAIL"},
      "Action": {"_" : "CREATE"},
      "AudioFileName": {"_" : this.cache["newvoicemail"]}
    };

    // Send the update to the server
    this.saveData("Meta_Subscriber_MetaSphere_MessageToSend",
                   newVoicemail,
                   successCallback,
                   failureCallback);
  };

  /** @private */
  function handleMessageAction(connection,
                               messageIds,
                               successCallback,
                               failureCallback,
                               dataType)
  {
    // We may have been passed a single id - treat it as if we were given a
    // single entry array
    if (typeof messageIds != "object")
    {
      messageIds = [messageIds];
    }

    // Work through the array of messages, forming them in to the wire
    // protocol array structure
    var data = [];
    for (var i = 0; i < messageIds.length; i++)
    {
      // We make sure the ids are treated as strings, even if they somehow got
      // changed to be numbers by appending them to an empty string
      data.push({"_" : "" + messageIds[i]});
    }

    // We are updating the messages, so invalidate any cached data for them
    if (dataType.match("Fax"))
    {
      // We are acting on the fax messages
      delete connection.cache.faxes;
    }
    else
    {
      // We are acting on the voicemails
      delete connection.cache.voicemails;
    }

    // Send the command to the server
    connection.saveData(dataType,
                        data,
                        successCallback,
                        failureCallback,
                        undefined);
  }

  /**
   * Marks one or a number of faxes as viewed.
   *
   * @param {String/String[]} faxIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #markFaxesAsUnviewed
   * @see #confirmFaxesViewed
   */
  CommPortal.prototype.markFaxesAsViewed = function(faxIds,
                                                    successCallback,
                                                    failureCallback)
  {
    handleMessageAction(this,
                        faxIds,
                        successCallback,
                        failureCallback,
                        "Meta_Subscriber_MetaSphere_FaxMessagesToMarkAsRead");
  };

  /**
   * Marks one or a number of faxes as unviewed.
   *
   * @param {String/String[]} faxIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #markFaxesAsViewed
   */
  CommPortal.prototype.markFaxesAsUnviewed = function(faxIds,
                                                      successCallback,
                                                      failureCallback)
  {
    handleMessageAction(this,
                        faxIds,
                        successCallback,
                        failureCallback,
                        "Meta_Subscriber_MetaSphere_FaxMessagesToMarkAsUnread");
  };

  /**
   * Deletes one or a number of faxes.
   *
   * @param {String/String[]} faxIds single id as string, or array of ids
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.deleteFaxes = function(faxIds,
                                              successCallback,
                                              failureCallback)
  {
    handleMessageAction(this,
                        faxIds,
                        successCallback,
                        failureCallback,
                        "Meta_Subscriber_MetaSphere_FaxMessagesToDelete");
  };

  /**
   * A synchronous call (no callbacks) that indicates that the passed faxes
   * have (probably) been viewed, without making an explicit call to the
   * server to set this state.  Allows the API to keep its state in sync.
   *
   * <p>
   * Should be called whenever the {@link fax#ImageFile} or
   * {@link fax#ImageFilePDF} field has been used to actually fetch the
   * referenced file.
   * </p>
   *
   * @param {String/String[]} faxIds single id as string, or array of ids
   *
   * @see #markFaxesAsViewed
   */
  CommPortal.prototype.confirmFaxesViewed = function(faxIds)
  {
    // We don't do anything clever with the passed ids - we simply invalidate
    // the fax related part of the cache.
    delete this.cache.faxes;
  };

  /**
   * Gets the list of codecs supported by this EAS.
   * <p>
   * The details are passed asynchronously to the provided callback when ready.
   * </p>
   *
   * <p>New in version 8.1</p>
   *
   * @param {function} successCallback callback called to pass the codec details (See {@link callbacks#greetingsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchCodecs = function(successCallback,
                                              failureCallback)
  {
    var connection = this;
        
    this.fetchData("Meta_Global_MetaSphere_Configuration",
                   processCodecs,
                   failureCallback);    

    function processCodecs(connection, dataType, getData, objectIdentity)
    {
      var codecInfo = [];
      
      if (getData.CodecInfo)
      {
        codecInfo = getData.CodecInfo;
      }
      else
      {
        var g711uCodec = {};
        g711uCodec.CodecName = "g711u";
        g711uCodec.CodecExtension = "wav";
        g711uCodec.CodecContentType = "audio/wav";
        codecInfo.push(g711uCodec);
      }
      
      queueCallback(successCallback,
                    connection,
                    codecInfo);
    }
  };
  
  /**
   * Gets details about the subscriber's greetings.
   * <p>
   * The details are passed asynchronously to the provided callback when ready.
   * </p>
   *
   * <p>New in version 7.3</p>
   *
   * @param {function} successCallback callback called to pass the greetings details (See {@link callbacks#greetingsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   * @param {String/String[]} (optional) a single codec or an array of codecs which the caller can handle. If not provided this will be assume to be g711u.
   */
  CommPortal.prototype.fetchGreetings = function(successCallback,
                                                 failureCallback,
                                                 codecs)
  {
    var connection = this;

    // We need to fetch both the Greetings data and the CoS for this function

    if (this.cache.greetings)
    {
      returnCachedData();
    }
    else if (this.cache.cos)
    {
      this.fetchData("Meta_Subscriber_MetaSphere_Greetings",
                     processGreetings,
                     failureCallback);
    }
    else
    {
      this.fetchData("Meta_Subscriber_MetaSphere_ClassOfService",
                     processClassOfService,
                     failureCallback);

    }

    // Cache the CoS data
    function processClassOfService(connection, dataType, getData, objectIdentity)
    {
      connection.cache.cos = getData;
      connection.fetchData("Meta_Subscriber_MetaSphere_Greetings",
                           processGreetings,
                           failureCallback);
    }

    // Process and cache the Greetings data
    function processGreetings(connection, dataType, getData, objectIdentity)
    {
      var greetings = {};
      greetings.defaultGreetingType = getData.DefaultGreetingType;

      // We filter the returned GreetingsList against the list of
      // greetings allowed by the CoS
      var filteredGreetingsList = [];
      var availGreetings = connection.cache.cos.AvailableGreetings.split(",");

      for (var i = 0; i < getData.GreetingsList.length; i++)
      {
        var greeting = getData.GreetingsList[i];
        var greetingIsAvail = false;

        // Check if current greeting is available
        for (var j = 0; j < availGreetings.length; j++)
        {
          if (availGreetings[j] == greeting.GreetingType)
          {
            greetingIsAvail = true;
            break;
          }
        }

        // Only return the greeting if the CoS says it is available
        if (greetingIsAvail)
        {
          var greetingCopy =
          {
            availableForDefault: greeting.AvailableForDefault,
            greetingType: greeting.GreetingType,
            isRecorded: greeting.IsRecorded,
            recordable: greeting.Recordable
          };

          filteredGreetingsList.push(greetingCopy);
        }
      }

      greetings.greetingsList = filteredGreetingsList;
      connection.cache.greetings = greetings;
      returnCachedData();
    }

    function returnCachedData()
    {
      for (var i = 0; i < connection.cache.greetings.greetingsList.length; i++)
      {        
        var greeting = connection.cache.greetings.greetingsList[i];
        
        // Add a field that gives the URL to get the audio file from
        if (greeting.isRecorded)
        {
          greeting.audioFile =
              connection.server + "/session" + connection.sessionId + "/line/" +
              "greeting.fetch?type=" + greeting.greetingType +
              "&version=" + connection.interfaceVersion + 
              "&codec=" + codecReqStr(codecs);
        }
      }
    
      queueCallback(successCallback,
                    connection,
                    connection.cache.greetings);
    }
  };

  /**
   * Uploads a greetings audio file to the CommPortal server.
   *
   * <p>New in version 7.3</p>
   *
   * @param {string} formId The ID of the HTML <form> element containing the
   *                        file input to be submitted to the server.
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.uploadGreetingsAudio = function(formId,
                                                       successCallback,
                                                       failureCallback)
  {
    uploadAudioFile(this, "greeting.wav", "newgreeting",
                    formId, successCallback, failureCallback);
  };

  /**
   * Update the audio for a greeting type using a previously uploaded audio
   * file. See {@link commportal#uploadGreetingsAudio} for more details.
   *
   * <p>New in version 7.3</p>
   *
   * @param {string} type The type of the greeting to update.
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.updateGreetingAudio = function(type,
                                                      successCallback,
                                                      failureCallback)
  {
    // Ensure we have greetings data cached
    var connection = this;
    if (!this.cache.greetings)
    {
      this.fetchGreetings(doUpdate, failureCallback);
    }
    else
    {
      doUpdate();
    }

    function doUpdate()
    {
      // Extract the current default then delete the cache
      var defType = connection.cache.greetings.defaultGreetingType;
      delete connection.cache.greetings;

      // Construct a greeting audio update JSON object
      var newGreeting =
      {
        "GreetingsList":
        [
          {
            "GreetingType": {"_" : type},
            "GreetingFilename": {"_" : connection.cache["newgreeting"]}
          }
        ],
        "DefaultGreetingType": {"_" : defType}
      };

      // Send the update to the server
      connection.saveData("Meta_Subscriber_MetaSphere_Greetings",
                          newGreeting,
                          successCallback,
                          failureCallback);
    }
  };

  /**
   * Update which greeting type is currently the default. Some greeting types
   * cannot be set directly as the default and must be enabled through other
   * fields on the Meta_Subscriber_MetaSphere_Greetings indication. This
   * can be done using the {@link commportal#saveData} function.
   *
   * <p>New in version 7.3</p>
   *
   * @param {function} successCallback callback called when successful (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
   CommPortal.prototype.updateDefaultGreeting = function(type,
                                                         successCallback,
                                                         failureCallback)
  {
    // Ensure we have greetings data cached
    var connection = this;
    if (!this.cache.greetings)
    {
      this.fetchGreetings(doUpdate, failureCallback);
    }
    else
    {
      doUpdate();
    }

    function doUpdate()
    {
      // Extract the current default then delete the cache
      var defType = connection.cache.greetings.defaultGreetingType;
      delete connection.cache.greetings;

      // Construct a greeting default type update JSON object
      var greetingUpdate =
      {
        "GreetingsList":
        [
          {
            "GreetingType": {"_" : defType},
            "AvailableForDefault": {"_" : true}
          }
        ],
        "DefaultGreetingType": {"_" : type}
      };

      // Send the update to the server
      connection.saveData("Meta_Subscriber_MetaSphere_Greetings",
                          greetingUpdate,
                          successCallback,
                          failureCallback);
    }
  };

  /** @private */
  function uploadAudioFile(connection, audioURL, audioSaveName,
                           formId, successCallback, failureCallback)
  {
    if (connection.sessionId)
    {
      // Lookup the supplied form ID
      var formElement = document.getElementById(formId);      
      var formUploadFileExt = ".wav";
      
      var formUploadFilename = formElement.uploadFile.value;
      if (formUploadFilename.lastIndexOf('.') > -1)
      { 
        formUploadFileExt = formUploadFilename.substring(formUploadFilename.lastIndexOf('.'));
      }
      var uploadFilename = audioSaveName + formUploadFileExt;

      // Create a hidden iFrame to hold the response
      var iFrameId = createUniqueId("upload");
      var iFrame = createiFrame(iFrameId);

      var fields =
      {
        version : connection.interfaceVersion,
        redirectTo : sRedirectUrl + "?success=true",
        errorRedirectTo : sRedirectUrl + "?error=updateFailed",
        filename : uploadFilename
      };
      
      connection.cache[audioSaveName] = uploadFilename;

      // We need to have the fields defined so far as part of the URL
      var urlFields = encodeFieldsForURL(fields);

      var uploadURL = connection.server + "/session" +
                      connection.sessionId + "/line/" +
                      audioURL + "?" + urlFields;
      formElement.action = uploadURL;
      formElement.target = iFrameId;
      formElement.submit();

      // Start polling for the response
      pollForResponse(connection,
                      iFrame,
                      successCallback,
                      failureCallback,
                      handleUploadAudioResponse);
    }
    else
    {
      queueError(failureCallback, connection, CommPortal.ERROR_NOT_LOGGED_IN);
    }

    /**
     * Callback when the upload audio response is available
     */
    function handleUploadAudioResponse(url,
                                       connection,
                                       iFrame,
                                       successCallback,
                                       failureCallback)
    {
      // We have finished with the iFrame and form so can remove them from the DOM
      removeElement(iFrame);

      var queryParams = parseQueryStringParams(url);

      if (queryParams.success)
      {
        queueCallback(successCallback,
                      connection);
      }
      else
      {
        var extras = { error : "uploadFailed" };

        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_DATA_SAVE_ERROR,
                   extras);
      }
    }
  };

  /**
   * Checks whether voicemail transcriptions are allowed for this subscriber.
   *
   * <p>New in version 7.3</p>
   *
   * @param {function} successCallback callback called when successful (See {@link callbacks#transcriptsEnabledCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchTranscriptionEnabled = function(successCallback,
                                                            failureCallback)
  {
    if (this.cache.transcriptions) 
    {
      // Return the already cached settings.
      returnCachedData();
    }
    else 
    {
      this.fetchData("SubscriberTranscriptionSettings",
                     processTranscriptionSettings,
                     failureCallback);
    }

    // Cache the transcription settings.
    function processTranscriptionSettings(connection, 
                                          dataType,
                                          getData, 
                                          objectIdentity)
    {
      connection.cache.transcriptions = getData.Enabled;
      returnCachedData();
    }

    function returnCachedData()
    {
      queueCallback(successCallback,
                    connection,
                    connection.cache.transcriptions);
    }
  };

  /**
   * CORE API: Updates data on the CommPortal server via the JSON interface.
   *
   * @param {String} dataType service indication type as used by JSON interface
   * @param {Object} data contents of JSON service indication
   * @param {function} successCallback callback called if success
   * (See {@link callbacks#successCallback}
   * @param {function} failureCallback callback called if error
   * (See {@link callbacks#failureCallback}
   * @param {Object} objectIdentity [optional] can be used to specify a
   *                          context other than the current subscriber
   * @param {String} logText [optional] can be used to specify a
   *                        summary of the update being performed which will be
   *                        logged to your Service Assurance Server
   *                        (if this is present and enabled).
   *                        WARNING.  Supplying a logText parameter to this
   *                        function places additional load on the Service
   *                        Assurance Server and should only be called if
   *                        you are sure that your Service Assurance Server
   *                        has sufficient capacity.
   *
   * @see #fetchRawData
   */
  CommPortal.prototype.saveData = function(dataType,
                                           data,
                                           successCallback,
                                           failureCallback,
                                           objectIdentity,
                                           logText)
  {
    // Process the parameters
    if (typeof objectIdentity == "string")
    {
      // The optional objectIdentity parameter was omitted, so shuffle the
      // parameters around
      logText = objectIdentity;
      objectIdentity = undefined;
    }
    // Default the logText if it has not been specified
    if (logText === undefined)
    {
      logText = "";
    }

    if (this.sessionId)
    {
      if (data._cleaned)
      {
        // We can only save raw data to the server, not cleaned data
        queueError(failureCallback, this, CommPortal.ERROR_NOT_RAW_DATA);
      }
      else
      {
        var oidString = objectIdentityToString(objectIdentity);
        if (oidString)
        {
          if (dataType.indexOf("?") > -1)
          {
            // We already have a query string, so append to it
            dataType += "&" + oidString;
          }
          else
          {
            // Add this as a query string
            dataType += "?" + oidString;
          }
        }

        // Save off our this pointer for use in the callback.
        var connection = this;

        // Create a hidden iFrame to hold the request
        var iFrameId = createUniqueId("save");

        var iFrame = createiFrame(iFrameId);

        // Compute the basic context info specifying the client version
        var contextInfoVal = getSASContextInfo(connection, false, logText, "");

        var fields =
        {
          version : this.interfaceVersion,
          redirectTo : sRedirectUrl + "?success=true",
          errorRedirectTo : sRedirectUrl + "?error=updateFailed",
          ContextInfo : contextInfoVal
        };

        // We need to have the fields defined so far as part of the URL
        var urlFields = encodeFieldsForURL(fields);

        // We have taken all the existing fields and put them in the URL, so
        // reset back to no form fields
        fields = {};

        // Include the data in the form
        fields[dataType] = JSON.stringify(data);

        // There is part of the url that is common for both post and get
        var baseURL = this.server + "/session" + this.sessionId + "/line/";

        if (this.useGet)
        {
          // Get places ALL the fields in the URL, so add the other ones now
          urlFields += "&" + encodeFieldsForURL(fields);

          var previousURL = simulateSubmit(iFrame,
                                           baseURL + "post?" + urlFields);
        }
        else
        {
          // Create a form whose response will be sent to the iframe
          var updateForm = createForm("CommPortal_updateSenderForm",
                                      iFrame,
                                      baseURL + "data?" + urlFields,
                                      fields);

          // Set the correct encoding for a data update
          updateForm.encoding = "multipart/form-data";

          // Call the updateRequestCallback function if it is set
          if (connection.updateRequestCallback)
          {
            connection.updateRequestCallback(updateForm);
          }

          // Submit the form we created
          updateForm.submit();
        }

        // Start polling for the response
        pollForResponse(this,
                        iFrame,
                        successCallback,
                        failureCallback,
                        handleDataSavedResponse,
                        previousURL);
      }
    }
    else
    {
      queueError(failureCallback, this, CommPortal.ERROR_NOT_LOGGED_IN);
    }

    /**
     * Callback when the save data response is available
     */
    function handleDataSavedResponse(url,
                                     connection,
                                     iFrame,
                                     successCallback,
                                     failureCallback)
    {

      // Call the updateResponseCallback function if it is set
      if (connection.updateResponseCallback)
      {
        connection.updateResponseCallback(url);
      }

      // We have finished with the iFrame and form so can remove them from the DOM
      removeElement(iFrame);
      removeElement(updateForm);

      var queryParams = parseQueryStringParams(url);

      if (queryParams.success)
      {
        queueCallback(successCallback,
                      connection);
      }
      else
      {
        // It was some form of error - if one was passed in the url we
        // use that, else just report unknown.
        var error = queryParams.error;
        var extras = { dataType : dataType };
        if (error) {
          extras.error = error;
        }

        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_DATA_SAVE_ERROR,
                   extras);
      }
    }
  };

  /**
   * Gets access to the CommPortal contacts, in an easily accessed simple
   * array format.
   * <p>
   * The contacts are passed asynchronously to the provided callback when
   * they are ready.
   * </p>
   *
   * @param {function} successCallback callback that is passed the contacts
   * @param {function} successCallback callback that is passed the contacts (See {@link callbacks#contactsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchContacts = function(successCallback,
                                                failureCallback)
  {
    var connection = this;

    if (this.cache.contacts)
    {
      returnCachedData();
    }
    else
    {
      // We must query the contacts, our callback handles raw data directly
      this.fetchRawData("Meta_Subscriber_UC9000_Contacts",
                        processContacts,
                        failureCallback);
    }

    /** @private */
    function processContacts(connection, dataType, getData, objectIdentity)
    {
      // We return a simple array, of simple objects
      var contacts = [];

      for (var i = 0; i < getData.vCard.length; i++)
      {
        var vCard = getData.vCard[i];

        var contact = {};

        /** @private */
        function setIfPresent(object, objectField, srcObject, evalText)
        {
          try
          {
            var value = eval("srcObject." + evalText + "._");

            object[objectField] = value;
          }
          catch (e)
          {
          }
        }

        /** @private */
        function setIfArrayPresent(contactField,
                                   vCardArrayField,
                                   vCardObjectField,
                                   omitType)
        {
          // To make life simpler, we always have an array of these things
          // present, but it may well be empty.
          contact[contactField] = [];
          if (!omitType)
          {
            contact[contactField + "Type"] = [];
          }

          try
          {
            eval("var array = vCard" + "." + vCardArrayField + ";");

            if (array)
            {
              for (var i = 0; i < array.length; i++)
              {
                if (typeof vCardObjectField == "string")
                {
                  // We were passed a field name to access the value
                  try
                  {
                    var value = eval("array[" + i + "]." + vCardObjectField);

                    contact[contactField][i] = value;
                  }
                  catch (e)
                  {
                  }
                }
                else
                {
                  // We were passed a function that processes a complex object
                  var value = vCardObjectField(array[i]);

                  contact[contactField][i] = value;
                }

                if (!omitType)
                {
                  // See if we have any info to put in the Type field
                  var types = ["HOME", "WORK", "CELL", "FAX"];
                  var type = "";
                  for (var t = 0; t < types.length; t++)
                  {
                    try
                    {
                      var value = eval("array[" + i + "]._" + types[t]);

                      if (value)
                      {
                        type = types[t].toLowerCase();
                        break;
                      }
                    }
                    catch (e)
                    {
                    }
                  }
                  contact[contactField + "Type"][i] = type;
                }
              }
            }
          }
          catch (e)
          {
          }
        }

        var fields =
        [
          "givenName",      "N.GIVEN",
          "familyName",     "N.FAMILY",
          "nickname",       "GROUP[0].NICKNAME[0]",
          "organization",   "GROUP[0].ORG[0].ORGNAME",
          "jobTitle",       "GROUP[0].TITLE[0]",
          "sms",            "GROUP[0].SMS[0].ADDRESS",
          "uid",            "GROUP[0].UID[0]"
        ];

        for (var field = 0; field < fields.length; field += 2)
        {
          setIfPresent(contact, fields[field], vCard, fields[field + 1]);
        }

        setIfArrayPresent("phone", "GROUP[0].TEL",   "NUMBER._");
        setIfArrayPresent("email", "GROUP[0].EMAIL", "USERID._", true);

        setIfArrayPresent("address", "GROUP[0].ADR", function(each)
        {
          var parts = [];
          var address = {};

          var fields =
          [
            "street",     "STREET",
            "locality",   "LOCALITY",
            "region",     "REGION",
            "postalcode", "PCODE",
            "country",    "CTRY"
          ];

          for (var field = 0; field < fields.length; field += 2)
          {
            setIfPresent(address, fields[field], each, fields[field + 1]);

            if (address[fields[field]])
            {
              parts.push(address[fields[field]]);
            }
          }

          if (parts.length)
          {
            address.displayAddress = parts.join(", ");
          }

          return address;
        });

        // Now we produce some calculated convenience display properties
        var parts = [];
        if (contact.givenName)
        {
          parts.push(contact.givenName);
        }
        if (contact.familyName)
        {
          parts.push(contact.familyName);
        }
        if (parts.length)
        {
          // This is just a convenience property - it assumes western style
          // names where a full name is written "firstname lastname"
          contact.displayName = parts.join(" ");
        }

        // Add this to the array of contacts
        contacts.push(contact);
      }

      connection.cache.contacts = contacts;

      returnCachedData();
    }

    /** @private */
    function returnCachedData()
    {
      queueCallback(successCallback,
                    connection,
                    connection.cache.contacts);
    }
  };

  /**
   * Adds a contact.
   *
   * @param {contact} contact contact object to be added
   * @param {function} successCallback callback that is called on success (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #modifyContact
   * @see #deleteContacts
   */
  CommPortal.prototype.addContact = function(contact,
                                             successCallback,
                                             failureCallback)
  {
    // It's an error when adding to supply the uid,
    // or processed fields that do not agree with their raw components
    if (contact.uid)
    {
      queueError(failureCallback, this, CommPortal.ERROR_SUPPLIED_UID);
    }
    else
    {
      innerHandleContact(this,
                         contact,
                         successCallback,
                         failureCallback);
    }
  };


  /**
   * Modifies a contact
   *
   * @param {contact} contact contact object to be modified
   * @param {function} successCallback callback that is called on success (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #addContact
   * @see #deleteContacts
   */
  CommPortal.prototype.modifyContact = function(contact,
                                                successCallback,
                                                failureCallback)
  {
    // It's an error when modifying not to have a uid.
    if (!contact.uid)
    {
      queueError(failureCallback, this, CommPortal.ERROR_MISSING_UID);
    }
    else
    {
      innerHandleContact(this,
                         contact,
                         successCallback,
                         failureCallback);
    }
  };

  /**
   * Deletes one or a number of contacts.
   *
   * @param {String/String[]} uids the id(s) of the contacts to delete
   * @param {function} successCallback callback that is called on success (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #addContact
   * @see #modifyContact
   */
  CommPortal.prototype.deleteContacts = function(uids,
                                                 successCallback,
                                                 failureCallback)
  {
    // We are deleting some contact - so the cache will be invalid
    delete this.cache.contacts;

    if (typeof uids != "object")
    {
      // We are just deleting a single uid, so turn it in to a single entry array
      uids = [uids];
    }

    var dataType = "Meta_Subscriber_UC9000_Contacts?vCard.GROUP.UID=*";

    var data =
    {
      "_Action": "delete",
      "vCard" : []
    };

    for (var i = 0; i < uids.length; i++)
    {
      data.vCard.push(
        {
          GROUP : [ { UID : [ {"_" : uids[i] } ] } ]
        });
    }

    this.saveData(dataType,
                  data,
                  successCallback,
                  failureCallback);
  };

  /** @private */
  function innerHandleContact(connection,
                              contact,
                              successCallback,
                              failureCallback)
  {
    // Extract and validate the fields in the passed object
    var given = contact.givenName || "";
    var family = contact.familyName || "";
    var job = contact.jobTitle || "";
    var organization = contact.organization || "";
    var sms = contact.sms || "";
    var nickname = contact.nickname || "";
    var uid = contact.uid || "";

    // We are changing some contact in some way - so the cache will be invalid
    delete connection.cache.contacts;

    var dataType = "Meta_Subscriber_UC9000_Contacts?vCard.GROUP.UID=" + uid;

    var data =
    {
      "vCard":[{"N":{"GIVEN":{"_" : given},
                     "FAMILY":{"_" : family}},
                "GROUP":[{"TEL":[],
                          "NICKNAME":[{"_" : nickname}],
                          "TITLE":[{"_" : job}],
                          "ORG":[{"ORGNAME":{"_" : organization}}],
                          "EMAIL":[],
                          "SMS":[{"ADDRESS":{"_" : sms}}],
                          "ADR":[]}]}]
    };

    if (uid)
    {
      // As well as including the UID in the dataType, we need it in the object
      data.vCard[0].GROUP[0].UID = [{"_" : uid}];
    }

    var email = contact.email || [];
    for (var i = 0; i < email.length; i++)
    {
      data.vCard[0].GROUP[0].EMAIL.push({USERID : {"_" : email[i]}});
    }

    var phone = contact.phone || [];
    for (var i = 0; i < phone.length; i++)
    {
      try
      {
        var type = contact.phoneType[i];
      }
      catch (e)
      {
      }

      data.vCard[0].GROUP[0].TEL.push({NUMBER : {"_" : phone[i]},
                                       _HOME : type == "home",
                                       _WORK : type == "work",
                                       _CELL : type == "cell",
                                       _FAX :  type == "fax"});
    }

    var address = contact.address || [];
    for (var i = 0; i < address.length; i++)
    {
      try
      {
        var type = contact.addressType[i];
      }
      catch (e)
      {
      }

      var adr =
      {
        _HOME : type == "home",
        _WORK : type == "work"
      };

      if (address[i].street)
      {
        adr.STREET = {"_" : address[i].street};
      }
      if (address[i].locality)
      {
        adr.LOCALITY = {"_" : address[i].locality};
      }
      if (address[i].region)
      {
        adr.REGION = {"_" : address[i].region};
      }
      if (address[i].pcode)
      {
        adr.PCODE = {"_" : address[i].pcode};
      }
      if (address[i].ctry)
      {
        adr.CTRY = {"_" : address[i].ctry};
      }

      data.vCard[0].GROUP[0].ADR.push(adr);
    }

    connection.saveData(dataType,
                        data,
                        successCallback,
                        failureCallback);
  }

  /**
   * Gets access to the list of dialed calls, in an easily accessed simple
   * array format.
   * <p>
   * The array of calls is passed asynchronously to the provided callback when
   * it is ready.
   * </p>
   *
   * @param {function} successCallback callback that is passed the calls (See {@link callbacks#callsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchDialedCalls = function(successCallback,
                                                   failureCallback)
  {
    commonFetchCallList(this,
                        successCallback,
                        failureCallback,
                        "DialedCalls");
  };

  /**
   * Gets access to the list of answered calls, in an easily accessed simple
   * array format.
   * <p>
   * The array of calls is passed asynchronously to the provided callback when
   * it is ready.
   * </p>
   *
   * @param {function} successCallback callback that is passed the calls (See {@link callbacks#callsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchAnsweredCalls = function(successCallback,
                                                     failureCallback)
  {
    commonFetchCallList(this,
                        successCallback,
                        failureCallback,
                        "AnsweredCalls");
  };

  /**
   * Gets access to the list of missed calls, in an easily accessed simple
   * array format.
   * <p>
   * The array of calls is passed asynchronously to the provided callback when
   * it is ready.
   * </p>
   *
   * @param {function} successCallback callback that is passed the calls (See {@link callbacks#callsCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.fetchMissedCalls = function(successCallback,
                                                   failureCallback)
  {
    commonFetchCallList(this,
                        successCallback,
                        failureCallback,
                        "MissedCalls");
  };

  /** @private */
  function commonFetchCallList(connection,
                               successCallback,
                               failureCallback,
                               type)
  {
    var requiredData =
    {
      mswCallLists  : "Meta_Subscriber_CallLists",
      sdpCallLists  : "Meta_Subscriber_MetaSphere_CallList",
      cos           : "Meta_Subscriber_MetaSphere_ClassOfService",
      caps          : "Meta_Subscriber_MetaSphere_SubscriberCapabilities",
      session       : "Session"
    };

    commonFetchRequiredData(requiredData, connection, returnCachedData);

    function returnCachedData()
    {
      var isMetaSwitchSubscriber =
        connection.cache.session.ManagedSubscribers[0].Devices[0].IsMetaSwitchSubscriber;

      // Determine which of the call lists should apply
      if (type == "DialedCalls")
      {
        var callList = isMetaSwitchSubscriber ? connection.cache.mswCallLists :
                                                null;
      }
      else
      {
        var icmEnabled = connection.cache.caps.LogicBasedRedirect &&
                         connection.cache.cos.IcmAllowed;

        if (isMetaSwitchSubscriber)
        {
          var callList = icmEnabled ? connection.cache.sdpCallLists :
                                      connection.cache.mswCallLists;
        }
        else
        {
          var callList = icmEnabled ? connection.cache.sdpCallLists :
                                      null;
        }
      }

      if (callList)
      {
        queueCallback(successCallback,
                      connection,
                      callList[type].Call);
      }
      else
      {
        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_NO_CALLLISTS);
      }
    }
  };

  /** @private */
  function commonFetchRequiredData(requiredData,
                                   connection,
                                   returnCachedData)
  {
    function gotRequiredData()
    {
      var gotAll = true;
      for (var i in requiredData)
      {
        if (requiredData.hasOwnProperty(i))
        {
          gotAll = gotAll && !!connection.cache[i];
        }
      }
      return gotAll;
    }

    if (gotRequiredData())
    {
      returnCachedData();
    }
    else
    {
      // Query all the data
      var dataTypes = [];

      for (var i in requiredData)
      {
        if (requiredData.hasOwnProperty(i) && !connection.cache[i])
        {
          dataTypes.push(requiredData[i]);
        }
      }

      connection.fetchData(dataTypes,
                           processData,
                           errorOnData);
    }

    function processData(connection, dataType, getData, objectIdentity)
    {
      for (var i in requiredData)
      {
        if (requiredData.hasOwnProperty(i) && requiredData[i] == dataType)
        {
          connection.cache[i] = getData;
          break;
        }
      }

      if (gotRequiredData())
      {
        returnCachedData();
      }
    }

    function errorOnData(connection, error)
    {
      // We simply cache any error we got
      for (var i in requiredData)
      {
        if (requiredData.hasOwnProperty(i) && requiredData[i] == error.dataType)
        {
          connection.cache[i] = error.getErrors;
          break;
        }
      }
    }
  }

  /**
   * Invalidates the cache so that subsequent data fetches get the latest data
   * direct from the server.
   */
  CommPortal.prototype.invalidateCache = function()
  {
    this.cache = {};
  };

  /**
   * Indicates state: Command to start call has yet to be issued
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_INITIAL         = 0;
  /**
   * Indicates state: Command to start call has been issued
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_CALLING         = 1;
  /**
   * Indicates state: The phone that forms the first leg of the call is ringing
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_FIRST_RINGING   = 2;
  /**
   * Indicates state: The first phone has been answered
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_FIRST_ANSWERED  = 3;
  /**
   * Indicates state: The phone that forms the second leg of the call is
   * ringing
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_SECOND_RINGING  = 4;
  /**
   * Indicates state: The second phone has been answered
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_SECOND_ANSWERED = 5;
  /**
   * Indicates state: The command to cancel the call has been issued.
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_CLEARING        = 6;
  /**
   * Indicates state: The call has ended.
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_CLEARED         = 7;
  /**
   * Indicates state: The call has failed.
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_FAILED          = 8;
  /**
   * Indicates state: The call still exists, but CommPortal has stepped out of
   * the call control path and so no further interaction with the call is
   * possible via this SDK.
   * @type integer
   * @final
   */
  CommPortal.CALLSTATE_FINAL           = 9;

  var callingStates = [];
  callingStates[CommPortal.CALLSTATE_INITIAL        ] = "Initial state";
  callingStates[CommPortal.CALLSTATE_CALLING        ] = "Calling";
  callingStates[CommPortal.CALLSTATE_FIRST_RINGING  ] = "First phone ringing";
  callingStates[CommPortal.CALLSTATE_FIRST_ANSWERED ] = "First phone answered";
  callingStates[CommPortal.CALLSTATE_SECOND_RINGING ] = "Second phone ringing";
  callingStates[CommPortal.CALLSTATE_SECOND_ANSWERED] = "Second phone answered";
  callingStates[CommPortal.CALLSTATE_CLEARING       ] = "Canceling call";
  callingStates[CommPortal.CALLSTATE_CLEARED        ] = "Call ended";
  callingStates[CommPortal.CALLSTATE_FAILED         ] = "Call failed";
  callingStates[CommPortal.CALLSTATE_FINAL          ] = "Final state";

  /**
   * Indicates state: The subscriber's phone is ringing
   *
   * <p>New in version 7.1</p>
   * @type string
   * @final
   */
  CommPortal.INCOMINGCALLSTATE_RINGING               = "ringing";
  /**
   * Indicates state: The subscriber's phone is not ringing
   *
   * <p>New in version 7.1</p>
   * @type string
   * @final
   */
  CommPortal.INCOMINGCALLSTATE_NOT_RINGING           = "not-ringing";
  /**
   * Indicates type: The incoming call type normal
   *
   * <p>New in version 7.1</p>
   * @type string
   * @final
   */
  CommPortal.INCOMINGCALLTYPE_NORMAL                 = "normal";
  /**
   * Indicates type: The incoming call type live message screening
   *
   * <p>New in version 7.1</p>
   * @type string
   * @final
   */
  CommPortal.INCOMINGCALLTYPE_LIVE_MESSAGE_SCREENING = "live-message-screening";

  /**
   * Makes a phone call.
   *
   * @param {String} numberTo phone number to call
   * @param {String} numberFrom [optional] phone number to call from
   * @param {function} progressCallback called multiple times with progress state (See {@link callbacks#progressCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #cancelCall
   */
  CommPortal.prototype.makeCall = function(numberTo, numberFrom, progressCallback, failureCallback)
  {
    var connection = this;

    // Process the parameters
    if (typeof numberFrom == "function")
    {
      // The optional numberFrom parameter was omitted, so shuffle the other
      // parameters around
      failureCallback = progressCallback;
      progressCallback = numberFrom;
      numberFrom = undefined;
    }

    if (!numberFrom)
    {
      // No from number was provided, so use the subscriber's own number
      connection.fetchSubscriberNumber(function(connection, number)
      {
        numberFrom = number;
        innerMakeCall(connection, numberTo, numberFrom, progressCallback, failureCallback);
      },
      failureCallback);
    }
    else
    {
      // We already have all the numbers we need, so make the call now
      innerMakeCall(connection, numberTo, numberFrom, progressCallback, failureCallback);
    }

    // Define a function to actually make the call.  The strategy here is to
    //
    // -  fire off an action to CommPortal Server to make the call.
    // -  assuming the action was accepted successfully, use a dedicated
    //    instance of the CommPortalEvent class to listen for events occurring
    //    on that call.
    function innerMakeCall(connection, numberTo, numberFrom, progressCallback, failureCallback)
    {
      if (connection.getCallId() != null)
      {
        // We can't have more than one call in progress at a time
        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_CALL_IN_PROGRESS,
                   { callId : connection.getCallId() });
      }
      else
      {
        // Reset the call state and store the callbacks.
        connection.setCallState(CommPortal.CALLSTATE_INITIAL);
        connection.callProgressCallback = progressCallback;
        connection.callFailureCallback = failureCallback;

        // Define a function for dealing with events that happen to the call
        // being made.  This is essentially a mapping from CommPortal Server
        // events to CommPortal.CALLSTATE_*.
        function eventCallback(connection, number, eventType, eventData, eventSubType)
        {
          if (eventSubType == "ServiceInitiatedEvent")
          {
            connection.setCallState(CommPortal.CALLSTATE_FIRST_RINGING);
          }
          else if (eventSubType == "OriginatedEvent")
          {
            connection.setCallState(CommPortal.CALLSTATE_FIRST_ANSWERED);
          }
          else if (eventSubType == "DeliveredEvent")
          {
            connection.setCallState(CommPortal.CALLSTATE_SECOND_RINGING);
          }
          else if (eventSubType == "EstablishedEvent")
          {
            // The called party has answered, so the call is now connected.
            connection.setCallState(CommPortal.CALLSTATE_SECOND_ANSWERED);
          }
          else if (eventSubType == "ConnectionClearedEvent")
          {
            if (eventData.cause._ == "pathReplacement")
            {
              // EAS has stepped out of the call path but the call still
              // exists.
              connection.setCallState(CommPortal.CALLSTATE_FINAL);
            }
            else
            {
              connection.setCallState(CommPortal.CALLSTATE_CLEARED);
            }
          }
          else if (eventSubType == "FailedEvent")
          {
            if (connection.callState >= CommPortal.CALLSTATE_FIRST_ANSWERED)
            {
              // An error after the first leg is answered will be communicated
              // via audio to the first phone, so just treat the call as ended.
              connection.setCallState(CommPortal.CALLSTATE_CLEARED);
            }
            else
            {
              var error = CommPortal.ERROR_UNKNOWN;

              // Try to work out why the call failed.
              if (eventData.cause._ == "busy")
              {
                error = CommPortal.ERROR_LINE_BUSY;
              }
              else if (eventData.cause._ == "callNotAnswered")
              {
                error = CommPortal.ERROR_CALL_NOT_ANSWERED;
              }

              connection.setCallState(CommPortal.CALLSTATE_FAILED, error);
            }
          }
        }

        // Define a function to be called when the action has been successfully
        // accepted by CommPortal Server.
        function actionCallback(connection, actionData)
        {
          // First check to see if this is a response to a MakeCall action
          // request.
          if (actionData.objectType)
          {
            if (actionData.objectType == "MakeCallResponse")
            {
              // The response is for the expected action.  Store the callID and
              // deviceID from the response so that the call can be canceled
              // later.
              if (actionData.callingDevice != null)
              {
                connection.callId   = actionData.callingDevice.callID;
                connection.deviceId = actionData.callingDevice.deviceID;
              }

              // Now use the dedicated instance of CommPortalEvent to listen
              // for events on this call.
              connection.setCallState(CommPortal.CALLSTATE_CALLING);
              connection.callEvent.subscribeToEvents(["Connection"],
                                                     eventCallback);
            }
            else
            {
              connection.setCallState(CommPortal.CALLSTATE_FAILED,
                                      CommPortal.ERROR_UNKNOWN);
            }
          }
        }

        // Define a function to be called when the action has failed - this
        // means we can send a better error than "action failed" back to the
        // caller.
        function actionFailedCallback(connection, error)
        {
          var actionData = error.actionData;
          error = CommPortal.ERROR_UNKNOWN;

          if ((actionData != null) &&
              (actionData.objectType == "ErrorValue") &&
              (actionData.operation == "invalidCalledNumber"))
          {
            error = CommPortal.ERROR_INVALID_NUMBER;
          }

          connection.setCallState(CommPortal.CALLSTATE_FAILED, error);
        }

        // Pause polling for events.  This is because some browsers only
        // support a maximum of 2 simultaneous connections to a single server,
        // and we don't want this to interfere with making this telephone call.
        connection.commPortalEvent.pausePolling();

        // Fire off the action to make the call.
        connection.performAction({
                                   objectType : "MakeCall",
                                   callingDevice : numberFrom,
                                   calledDirectoryNumber : numberTo,
                                   autoOriginate : "prompt",
                                   callCharacteristics : { assistCall : false }
                                 },
                                 actionCallback,
                                 actionFailedCallback);
      }
    }
  };

  /**
   * Cancels making a phone call.
   *
   * <p>
   * Can be used to cancel the last call started via {@link makeCall()}
   * provided that the call has not already ended nor reached the
   * CommPortal.CALLSTATE_FINAL call state.
   * </p>
   *
   * <p>
   * Note that the progressCallback of
   * the corresponding {@link #makeCall} will be called to tell it that the
   * call has been canceled.
   * </p>
   *
   * @param {function} successCallback callback that is called on success (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   *
   * @see #makeCall
   */
  CommPortal.prototype.cancelCall = function(successCallback,
                                             failureCallback)
  {
    var callId = this.getCallId();

    if ((callId != null) &&
        (this.callState < CommPortal.CALLSTATE_CLEARED))
    {
      // Record the fact that we are canceling the call.
      this.setCallState(CommPortal.CALLSTATE_CLEARING);

      // Define a function to be called when the action has been successfully
      // accepted by CommPortal Server.  The event-listening interface should
      // report a final state anyway, but do it here as well to be defensive.
      function actionCallback(connection, actionData)
      {
        // First check to see if the response is for an EndCall action request.
        if (actionData.objectType == "EndCallResponse")
        {
          connection.setCallState(CommPortal.CALLSTATE_CLEARED);
        }
        else
        {
          connection.setCallState(CommPortal.CALLSTATE_FAILED,
                                  CommPortal.ERROR_UNKNOWN);
        }
      }

      // Fire off the action to cancel the call.
      this.performAction({
                           objectType : "EndCall",
                           connectionToBeCleared : { callID : callId,
                                                     deviceID : this.deviceId }
                         },
                         actionCallback,
                         failureCallback);

      // Signal that the cancel request was accepted.
      queueCallback(successCallback, connection);
    }
    else
    {
      queueError(failureCallback,
                 this,
                 CommPortal.ERROR_NO_CURRENT_CALL);
    }
  };

  /**
   * Sets the new state and does associated housekeeping, including calling the
   * appropriate callback.
   *
   * @param {CommPortal.CALLSTATE_*} newState the new call state.
   * @param {CommPortalCall.Error} error (optional) error that occurred.
   * @private
   */
  CommPortal.prototype.setCallState = function(newState, error)
  {
    var oldState = this.callState;
    this.callState = newState;
    var callId = this.getCallId();

    // Ignore this request unless it denotes a change in state.
    if (oldState != newState)
    {
      // If this is an error state, call the failure callback.
      if ((error != null) && (this.callFailureCallback != null))
      {
        queueError(this.callFailureCallback, this, error);
      }
      else if ((this.callProgressCallback != null) &&
               (newState > CommPortal.CALLSTATE_INITIAL))
      {
        // Report the current state to the progress callback.  Don't bother
        // reporting the initial call state event as this is of no interest to
        // the caller.
        var stateObject =
        {
          state : newState,
          message : callingStates[newState],
          toString : function() {return this.message;}
        };
        queueCallback(this.callProgressCallback, this, callId, stateObject);
      }

      // A state higher than CLEARING indicates that there is no further
      // processing for this call, so tidy up.
      if (newState > CommPortal.CALLSTATE_CLEARING)
      {
        this.callEvent.unsubscribeFromEvents(["Connection"]);
        this.commPortalEvent.restartPolling();
        delete this.callId;
        delete this.deviceId;
        delete this.callFailureCallback;
        delete this.callProgressCallback;
      }
    }
  };

  /**
   * CORE API: Subscribe to events
   *
   * <p>
   * Sets up a subscription for events that will be passed to the given
   * callback.
   *
   * <p>
   * Can be called multiple times with different events specified, to
   * allow different events to be routed to different callbacks, or with
   * multiple events at once to pass them all to the same callback.
   * A subsequent call with a different callback will replace any existing
   * callback for that event.
   *
   * <p>
   * Possible event strings are
   * <ul>
   *   <li>IncomingCallManager - changes to ICM configuration</li>
   *   <li>DoNotDisturb - changes to DND configuration</li>
   *   <li>FindMeFollowMe - changes to MetaSwitch FMFM configuration</li>
   *   <li>UnconditionalCallForwarding - changes to UCF configuration</li>
   *   <li>VoicemailCount - changes to the numbers of read and unread voicemails</li>
   *   <li>Contacts - changes to the contact list</li>
   *   <li>ClickToDialConfig - changes to the click-to-dial configuration</li>
   *   <li>IncomingCall - provides details of incoming calls</li>
   * </ul>
   *
   * <p>
   * The optional target line number may be specified when requesting events on
   * other than the logged in subscriber's line.  (Only certain types of
   * subscriber are able to access other lines in this way).
   *
   * <p>
   * The optional timeout parameter specifies a timeout which may be used to
   * tweak the number of requests that go out over the wire.  If multiple
   * calls use different timeout settings then the last value passed to any of
   * the calls is the one used.
   *
   * <p>
   * You should be aware that some events can't be listened for simultaneously,
   * for instance, you can't do both of listen for incoming call events AND
   * listen on multiple lines at once.  In the case when incompatible events
   * are requested, an error will be returned.
   *
   * <p>New in version 7.1</p>
   *
   * @see #unsubscribeFromEvents
   *
   * @param {String/String[]} events which events should be subscribed to
   * @param {function} eventCallback callback called for each event (See {@link callbacks#eventCallback})
   * @param {String} target [optional] line number to request events on
   * @param {Integer} timeout [optional] maximum timeout to use in milliseconds
   *
   * @return undefined on success, or an error value such as if asked to start
   *   listening for events that are incompatible or if asked to listen to
   *   an event that the subscriber is not allowed to listen for
   * @type error
   */
  CommPortal.prototype.subscribeToEvents = function(events,
                                                    eventCallback,
                                                    target,
                                                    timeout)
  {
    return this.commPortalEvent.subscribeToEvents(events,
                                                  eventCallback,
                                                  target,
                                                  timeout);
  };

  /**
   * CORE API: Unsubscribe from events
   *
   * <p>
   * Clears a subscription for specified events.
   *
   * <p>
   * Can be called multiple times with different events specified, or
   * with multiple events at once.  Only the events given are unsubscribed
   * from, and the groupings used when unsubscribing do not need to match those
   * used when subscribing.
   *
   * <p>
   * The optional line number may be specified when unsubscribing from events
   * on other than the logged in subscriber's line.  (Only certain types of
   * subscriber are able to access other lines in this way).
   *
   * <p>New in version 7.1</p>
   *
   * @see #subscribeToEvents
   *
   * @param {String/String[]} events which events should be unsubscribed from
   * @param {String} target [optional] phone number of line that event subscription relates to
   *
   * @return undefined on success, or an error value if there was no
   *   corresponding subscription request found
   * @type error
   */
  CommPortal.prototype.unsubscribeFromEvents = function(events,
                                                        target)
  {
    return this.commPortalEvent.unsubscribeFromEvents(events, target);
  };

  /**
   * Set the incoming call handler, so that it is notified when an incoming
   * call occurs.
   *
   * <p>
   * The optional target phone number may be specified when needing to handle
   * incoming calls on other than the logged in subscriber's phone line.
   * (Only certain types of subscriber are able to access other phone lines in
   * this way).
   *
   * <p>
   * You cannot set call handlers for multiple phone lines at once, nor mix
   * this method with the core API method {@link #subscribeToEvents}
   * when that would also involve multiple target phone lines.  In such cases
   * a synchronous error is returned.
   *
   * <p>New in version 7.1</p>
   *
   * @see #clearIncomingCallHandler
   * @see #subscribeToEvents
   *
   * @param {function} incomingCallback callback called for each incoming call (See {@link callbacks#incomingCallback})
   * @param {String} target [optional] phone number to monitor for incoming calls
   *
   * @return undefined on success, or an error value if adding this call
   *   handler is incompatible with other event monitoring already in place, or
   *   if the subsriber account is not configured to allow incoming call
   *   notifications
   * @type error
   */
  CommPortal.prototype.setIncomingCallHandler = function(incomingCallback,
                                                         target)
  {
    function eventCallback(connection, number, eventType, eventData)
    {
      incomingCallback(connection, number, eventData);
    }

    return this.subscribeToEvents(["IncomingCall"],
                                  eventCallback,
                                  target);
  };

  /**
   * Clear the incoming call handler
   *
   * <p>
   * The optional target line number may be specified to match that provided
   * to setIncomingCallHandler.
   *
   * <p>New in version 7.1</p>
   *
   * @see #setIncomingCallHandler
   *
   * @param {String} target [optional] line number
   *
   * @return undefined on success, or an error value such as if there is no
   *   corresponding call handler currently set
   * @type error
   */
  CommPortal.prototype.clearIncomingCallHandler = function(target)
  {
    return this.unsubscribeFromEvents(["IncomingCall"], target);
  };

  /**
   * CORE API: Perform the server action as described by the data parameter.
   *
   * <p>New in version 7.1</p>
   *
   * @param {Object} data contents of service indication describing the action
   * @param {function} actionCallback callback called when successfully
   *        performed the action (See {@link callbacks#actionCallback})
   * @param {function} failureCallback callback called if an error occurs  (See
   *        {@link callbacks#failureCallback})
   */
  CommPortal.prototype.performAction = function(data,
                                                actionCallback,
                                                failureCallback)
  {
    if (this.sessionId)
    {
      // Save off our this pointer for use in the callback
      var connection = this;

      // Set the version and the data
      var fields =
      {
        version : this.interfaceVersion,
        request : JSON.stringify(data)
      };

      // Encode the fields to add to the url
      var urlFields = encodeFieldsForURL(fields);

      // Create the base URL
      var baseURL = this.server + "/session" + this.sessionId + "/line/";

      // Create a uniquely named callback function for this request
      var callbackName = createUniqueId("callback");
      var callback = "callback=CommPortal." + callbackName;

      var url = baseURL + "action.js?" + callback + "&" + urlFields;

      // Register the uniquely-named callback
      CommPortal[callbackName] = function(data)
      {
        // Call the getReponseCallback function if it is set
        if (connection.getResponseCallback)
        {
          connection.getResponseCallback(callbackName,
                                         null,
                                         "action.js",
                                         data,
                                         null,
                                         null,
                                         null);
        }

        try
        {
          handleActionResponse(data, actionCallback, failureCallback);
        }
        finally
        {
          // Remove the now unneeded script tag.
          removeElement(scriptTag);

          // This callback is now finished with, get rid of it
          delete CommPortal[callbackName];
        }
      };
      // Add a new script tag with that URL.  Once loaded, it will kick
      // off our callback.
      var scriptTag = appendScriptTag(url);

      // Call the getRequestCallback function if it is set
      if (connection.getRequestCallback)
      {
        connection.getRequestCallback(url);
      }
    }
    else
    {
      queueError(failureCallback, this, CommPortal.ERROR_NOT_LOGGED_IN);
    }

    // Callback to handle the action response
    function handleActionResponse(data,
                                  actionCallback,
                                  failureCallback)
    {
      if (data.objectType != "ErrorValue")
      {
        // We successfully performed the action
        queueCallback(actionCallback,
                      connection,
                      data);
      }
      else
      {
        // We failed to perform the action
        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_ACTION_INVALID_DATA,
                   { actionData : data });
      }
    }
  };

  /**
   * Redirect an incoming call to another phone number
   *
   * <p>New in version 7.1</p>
   *
   * @param {String} callId the id of the call to be redirected
   * @param {String} device the device number that is receiving the call
   * @param {String} newNumber the new phone number to redirect the call to
   * @param {function} successCallback callback called when the call is successfully redirected (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.redirectCall = function(callId,
                                               device,
                                               newNumber,
                                               successCallback,
                                               failureCallback)
  {
    // Create the data to submit to the server
    var data =
    {
      objectType : "DeflectCall",
      callToBeDiverted : {
        deviceID : device,
        callID : callId
      },
      newDestination : newNumber,
      reason : "callForward"
    };

    this.performAction(data,
                       successCallback,
                       failureCallback);
  };

  /**
   * Reject an incoming call.
   *
   * <p/>
   * The rejected call may be ended, or sent to email instead dependent on the
   * line's settings.
   *
   * <p>New in version 7.1</p>
   *
   * @param {String} callId the id of the call to be rejected
   * @param {String} device the device number that is receiving the call
   * @param {function} successCallback callback called when the call is successfully terminated (See {@link callbacks#successCallback})
   * @param {function} failureCallback callback called if an error occurs (See {@link callbacks#failureCallback})
   */
  CommPortal.prototype.rejectCall = function(callId,
                                             device,
                                             successCallback,
                                             failureCallback)
  {
    // Create the data to submit to the server
    var data =
    {
      objectType : "EndCall",
      connectionToBeCleared : {
        callID : callId,
        deviceID : device
      }
    };

    this.performAction(data,
                       successCallback,
                       failureCallback);
  };

  /**
   * Make a Service Assurance Server (SAS) log.
   *
   * <p>WARNING.  Calling this function places additional load on the Service
   * Assurance Server and should only be called if you are sure that your
   * Service Assurance Server has sufficient capacity.</p>
   *
   * <p>
   * The Service Assurance Server supports the ISO-8859-1 character set so
   * the summary and details text must only use these characters.
   *</p>
   * @param {String} summary the summary information. This can be up to 128
   *                 characters long. Any additional characters will be
   *                 discarded and replaced with "..."
   *                 It must not be blank or the log will be ignored.
   * @param {String} details the detailed description. This can be up to 512
   *                 characters long. Any additional characters will be
   *                 discarded and replaced with "...".
   * @see #makeSASErrorLog
   */
  CommPortal.prototype.makeSASLog = function(summary, details)
  {
    makeSASLogInternal(this, false, summary, details);
  };

  /**
   * Make a Service Assurance Server (SAS) error log.
   *
   * <p>WARNING.  Calling this function places additional load on the Service
   * Assurance Server and should only be called if you are sure that your
   * Service Assurance Server has sufficient capacity.</p>
   *
   *<p>
   * The Service Assurance Server supports the ISO-8859-1 character set so
   * the summary and details text must only use these characters.
   *</p>
   * @param {String} summary the summary information. This can be up to 128
   *                 characters long. Any additional characters will be
   *                 discarded and replaced with "..."
   *                 It must not be blank or the log will be ignored.
   * @param {String} details the detailed description. This can be up to 512
   *                 characters long. Any additional characters will be
   *                 discarded and replaced with "...". A stack trace is
   *                 automatically appended to the details.
   * @see #makeSASLog
   */
  CommPortal.prototype.makeSASErrorLog = function(summary, details)
  {
    details += "\nStack:\n" + getStack(CommPortal.prototype.makeSASErrorLog.caller);
    makeSASLogInternal(this, true, summary, details);
  };

  /** @private */
  function makeSASLogInternal(connection, isError, summary, details)
  {
    // Only make a log if we have a session and the trimmed summary is not
    // blank.
    if (connection.sessionId && (summary.replace(/^\s+|\s+$/g, '') != ""))
    {
      var image = new Image();
      image.src = connection.server +
                  "/session" +
                  connection.sessionId +
                  "/line/clientlog.gif?ContextInfo=" +
                  encodeURIComponent(getSASContextInfo(connection,
                                                       isError,
                                                       summary,
                                                       details));
    }
  };

  /** @private */
  function getSASBasicContextInfo(connection)
  {
    // Simply return a ContextInfo only containing the ClientVersion.
    return getSASContextInfo(connection, false, "", "");
  };

  /** @private */
  function getSASContextInfo(connection, isError, summary, details)
  {
    // Create a local function to truncate a message to a given length,
    // appending three dots (an ellipsis cannot be used until SAS supports
    // UTF-8).
    var truncate = function(message, length)
      {
        // Trim whitespace.
        message = message.replace(/^\s+|\s+$/g, '');
        if (message.length > length)
        {
          message = message.substr(0, length) + "...";
        }
        return message;
      };

    // Truncate the fields so that the SAS database does not fill up too quickly.
    var version = truncate(connection.clientVersion, 16);
    summary = truncate(summary, 128);
    details = truncate(details, 512);

    // Only include the error, summary and details if there are any, since
    // otherwise SAS will use sensible defaults.
    return("version=" + encodeURIComponent(version) +
           (isError ? "&error=1" : "") +
           ((summary.length > 0) ? ("&summary=" + encodeURIComponent(summary)): "") +
           ((details.length > 0) ? ("&details=" + encodeURIComponent(details)) : ""));
  };

  /** @private */
  function getStack(f)
  {
    var stack = "";
    try
    {
      if (Error && Error().stack)
      {
        // Mozilla stores the stack directly on Error objects.
        stack = Error().stack;

        // Strip off the Error details and this function (the first 2 lines).
        stack = /^.*\n.*\n([\s\S]*)$/.exec(stack)[1];

        // The format of the stack is https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Error/stack
        // i.e. one line for each function with this format:
        // <function name>([parameters])@<page URL>:<line number>
        // Strip off the parameters and most of the page URL, otherwise there
        // will not be enough space in the 512 bytes details limit for much of
        // the stack.
        // (\S*\() - match on 0 or more non-white space chars, ending in open bracket.
        //           i.e. the function name and opening bracket.
        // .* - ignore 0 or more characters, i.e. any function parameters.
        // (\)) - match on closing bracket.
        // \@ - ignore @.
        // .* - ignore 0 or more characters.
        // \/ - ignore last / of URL.
        // ([^?#]*) - match on 1 or more non-?# characters, i.e. the last part of the URL.
        // [?#]* - ignore 0 or more ? and #, an optional URL parameter start or hash.
        // .* - ignore 0 or more characters.
        // (:\d*) - match the colon followed by the line number.
        //
        // Execute with multiline regexp so that the regexp matches on each line.
        var functionsRegExp = /^(\S*\().*(\))\@.*\/([^?#]*)[?#]*.*(:\d*)$/gm;
        var matches;
        var cleanStack = "";
        while (matches = functionsRegExp.exec(stack))
        {
          // Prevent browsers like Firefox from getting stuck in an infinite loop
          if (matches.index == functionsRegExp.lastIndex)
          {
            functionsRegExp.lastIndex++;
          }
          // The match results are stored from index 1 (index 0 is the expression
          // that the regexp operated on).

          // Make anonynous functions more obvious.
          if (matches[1] == "(")
          {
            matches[1] = "anonymous(";
          }

          for (var ii = 1; ii < matches.length; ii++)
          {
            cleanStack += matches[ii];
          }
          cleanStack += "\n";
        }
        stack = cleanStack;
      }
      else if (f)
      {
        // Extract the function name from the function.
        var matches=/function (\w+)/.exec(String(f));
        var stack = matches ? matches[1] : "";

        // Get to the next depth in the stack.
        stack += "()\n" + getStack(f.caller);
      }
      // Otherwise we are at the end of stack so just return an empty string.
    }
    catch (localException)
    {
      stack = "Failed to get stack due to " + localException;
    }

    return(stack);
  };

  /**
   * Class responsible for polling the CommPortal server for events
   * @class
   * @private
   */
  function CommPortalEvent()
  {
    /**
     * This private variable stores the queue of events requests.
     * @private
     */
    this.queuedEventRequests = [];

    /**
     * This private variable stores the delay for the next poll.
     * @private
     */
    this.delay = 0;

    /**
     * This private variable stores the delay for the next poll if we got an
     * error.
     * @private
     */
    this.nextErrorDelay = 0;

    /**
     * This private variable stores the timeout for polling events.  Default
     * value set to 30000 milliseconds (30 seconds).
     * @private
     */
    this.timeout = 30000;

    /**
     * This private variable stores a list of events and their callbacks.
     * @private
     */
    this.eventObjectList = [];

    /**
     * This private variable stores the polling state.
     * @private
     */
    this.pollingState = CommPortalEvent.STOPPED;

    /**
     * This private variable stores the default subscriber line
     * @private
     */
    this.defaultLine = "";

    /**
     * This private variable stores the Class of Service data
     * @private
     */
    this.classOfService = {};

    /**
     * This private variable stores the mapping to convert the events name
     * that are exposed to the user to the events name used in the CommPortal
     * Server.
     * @private
     */
    this.userToCommPortalEvents =
    {
      IncomingCallManager :         "Meta_SubscriberDevice_MetaSphere_ICM",
      DoNotDisturb :                "Meta_Subscriber_DoNotDisturb",
      FindMeFollowMe :              "Meta_Subscriber_Find",
      UnconditionalCallForwarding : "Meta_Subscriber_UnconditionalCallForwarding",
      VoicemailCount :              "Meta_Subscriber_MetaSphere_VoicemailMessageCounts",
      Contacts :                    "Meta_Subscriber_UC9000_Contacts",
      ClickToDialConfig :           "Meta_SubscriberDevice_MetaSphere_CTDDeviceConfig",
      IncomingCall :                "IncomingCall",
      Connection :                  "Connection",
      // Return an inverted version of this object making the keys be the
      // values and the values be the keys (only when the values are strings)
      inverted : function()
      {
        var result = {};
        // For each key, invert it by the value
        for (var key in this)
        {
          if ((key in this) && typeof(this[key]) == "string")
          {
            // Make the key be the value and the value be the key
            result[this[key]] = key;
          }
          else
          {
            // We cannot add a value as a key if it isn't o type string, but
            // we want to preserve it as it is in the original object
            result[key] = this[key];
          }
        }
        return result;
      }
    };
  };

  /**
   * State: Polling is stopped.
   * @type integer
   * @final
   */
   /** @private */
  CommPortalEvent.STOPPED = 0;
  /**
   * State: Polling is started.
   * @type integer
   * @final
   */
   /** @private */
  CommPortalEvent.STARTED = 1;
  /**
   * State: Polling is paused.
   * @type integer
   * @final
   */
   /** @private */
  CommPortalEvent.PAUSED = 2;
  /**
   * State: Polling is paused but wasn't started when paused.
   * @type integer
   * @final
   */
   /** @private */
  CommPortalEvent.PAUSED_NOT_STARTED = 3;
  /**
   * Timeout to wait in addition to the default time out to identify
   * connection problem
   * @type integer
   * @final
   */
   /** @private */
  CommPortalEvent.STANDARD_SERVER_TIMEOUT = 5000;

  /**
   * Subscribe to events
   * @private
   *
   * @see CommPortal#subscribeToEvents
   */
  CommPortalEvent.prototype.subscribeToEvents = function(events,
                                                         eventCallback,
                                                         target,
                                                         timeout)
  {
    // Check if there are any invalid or incompatible events specified
    var error = this.checkEventError(events, target);
    if (!error)
    {
      // Convert the events name to the ones used by CommPortal
      var convertList = this.userToCommPortalEvents;
      events = this.convertEventName(events, convertList);

      // Add the events to the list
      this.addEventList(events, eventCallback, target, timeout);

      // Create the queue
      this.createQueue();

      if (this.pollNotStarted())
      {
        // Start the polling
        this.pollForUpdates();
      }
    }

    return error;
  };

  /**
   * Unsubscribe from events
   * @private
   *
   * @see CommPortal#unsubscribeFromEvents
   */
  CommPortalEvent.prototype.unsubscribeFromEvents = function(events,
                                                             target)
  {
    var error = this.removeEventList(events, target);
    this.createQueue();
    return error;
  };

  /**
   * Poll for updates
   * @private
   */
  CommPortalEvent.prototype.pollForUpdates = function()
  {
    if (this.pollingState == CommPortalEvent.STOPPED)
    {
      // Change the polling state to started
      this.pollingState = CommPortalEvent.STARTED;
    }
    else if (this.pollingState == CommPortalEvent.PAUSED_NOT_STARTED)
    {
      // The polling is paused and wasn't started, so we change the state
      // to indicate that it should start after paused state finishes
      this.pollingState = CommPortalEvent.PAUSED;
    }
    else if (!this.hasEvents())
    {
      // There aren't any events to poll, so stop polling
      this.pollingState = CommPortalEvent.STOPPED;
    }

    if (this.pollingState == CommPortalEvent.STARTED)
    {
      // Take the next request off the queue
      var eventObject = this.queuedEventRequests.shift();

      // and return it to the back of the queue again
      this.queuedEventRequests.push(eventObject);

      // Create a uniquely named callback function for this request.
      var callbackName = createUniqueId("callback");
      var callback = "CommPortal." + callbackName;

      this.actualCallbackName = callbackName;

      // Create the URL that we should send the request for events to on the
      // server
      var eventURL = this.createEventURL(eventObject, callback);

      var commportalEvent = this;

      // Register the uniquely-named callback
      CommPortal[callbackName] = function(data)
      {
        // Call the getReponseCallback function if it is set.
        if (connection.getResponseCallback)
        {
          connection.getResponseCallback(callbackName,
                                         null,
                                         "event.js",
                                         data,
                                         null,
                                         null,
                                         null);
        }

        // Handle the response
        commportalEvent.handleEventResponse(data);

        // Cleanup the callback and script tag
        commportalEvent.cleanupRequest(callbackName, scriptTag);

        // Clear the script timeout
        clearTimeout(handleTimeout);
      };

      // Append the script tag to poll for events
      var scriptTag = appendScriptTag(eventURL);

      this.actualScriptTag = scriptTag;

      // Will handle any connection problem that could have occurred
      function handleScriptRequestTimeout()
      {
        // We had a problem with the script request, cleanup the callback and
        // script tab
        commportalEvent.cleanupRequest(callbackName, scriptTag);

        if (commportalEvent.pollingState == CommPortalEvent.STARTED)
        {
          // Poll for updates
          commportalEvent.pollForUpdates();
        }
      }

      // Set a timeout to handle a connection problem in the request
      var handleTimeout =
            setTimeout(handleScriptRequestTimeout,
                       this.timeout + CommPortalEvent.STANDARD_SERVER_TIMEOUT);

      // Call the getRequestCallback function if it is set.
      if (connection.getRequestCallback)
      {
        connection.getRequestCallback(eventURL);
      }
    }
  };

  /**
   * Pause polling
   * @private
   */
  CommPortalEvent.prototype.pausePolling = function()
  {
    if (this.pollingState == CommPortalEvent.STARTED)
    {
      // The polling was already started, so we indicate that it is paused now
      // but should be started again.
      this.pollingState = CommPortalEvent.PAUSED;
      // Cancel and clear any outstanding request
      this.cleanupRequest(this.actualCallbackName, this.actualScriptTag);
    }
    else if (this.pollingState == CommPortalEvent.STOPPED)
    {
      // The polling was stopped, so we indicate that it is paused now but
      // wasn't started yet
      this.pollingState = CommPortalEvent.PAUSED_NOT_STARTED;
    }
  };

  /**
   * Restart polling if it was started before being paused
   * @private
   */
  CommPortalEvent.prototype.restartPolling = function()
  {
    if (this.pollingState == CommPortalEvent.PAUSED)
    {
      // The polling was paused and now it needs to be started again
      this.pollingState = CommPortalEvent.STARTED;
      this.pollForUpdates();
    }
    else if (this.pollingState == CommPortalEvent.PAUSED_NOT_STARTED)
    {
      // The polling was paused but there is no need to start it again
      this.pollingState = CommPortalEvent.STOPPED;
    }
  };

  /**
   * Return true if the poll has not started
   * @return true if poll not started
   * @type boolean
   * @private
   */
  CommPortalEvent.prototype.pollNotStarted = function()
  {
    var result = ((this.pollingState == CommPortalEvent.STOPPED) ||
                  (this.pollingState == CommPortalEvent.PAUSED_NOT_STARTED));

    return result;
  };

  /**
   * Handle the event response
   *
   * @param {Object} data contents of JSON service indication
   * @private
   */
  CommPortalEvent.prototype.handleEventResponse = function(data)
  {
    this.delay = 0;
 
    // We need to call the callback for any data that we have been told has
    // changed
    var eventList = data.events;
    if (eventList)
    {
      for (var i = 0; i < eventList.length; i++)
      {
        var event = eventList[i];
        var eventName = event.subscription;
        var target = event.objectIdentity.line;
        
        // Get the callback responsible for handling the event
        var callback = this.getEventCallback(eventName, target);
        if (callback)
        {
          // Convert the event name to the one used by the user
          var convertList = this.userToCommPortalEvents.inverted();
          eventName = (this.convertEventName(eventName, convertList))[0];
          queueCallback(callback,
                        connection,
                        target,
                        eventName,
                        event.data,
                        event.eventType);
        }
      }
    }

    var errorList = data.errors;
    if (errorList.length > 0)
    {
      // Since we are getting errors, we delay before our next poll
      // However, if we are getting noSuchObject, we should not ask again, per the API.
      this.delay = (type == "noSuchObject") ? -1 : this.getNextErrorDelay();
    }

    // A non-negative delay means we should poll again
    if (this.delay >= 0)
    {
      var commportalEvent = this;
      setTimeout(function()
      {
        commportalEvent.pollForUpdates();
      }, this.delay);
    }
  };

  /**
   * Return the delay that should be used if we got an error
   *
   * @return the delay in milliseconds
   * @type integer
   * @private
   */
  CommPortalEvent.prototype.getNextErrorDelay = function()
  {
    var delay = this.nextErrorDelay || 0;

    // Update the nextErrorDelay so we delay increasingly larger gaps
    // when errors are occuring continuously, up to a maximum of 30 secs
    this.nextErrorDelay = Math.min(30000, delay * 2 + 2000);

    return delay;
  };

  /**
   * Convert the event name in the event list acconding to the convert list
   *
   * The event name will be preserved if there isn't a corresponding one in
   * the convert list.
   *
   * @param {String/String[]} eventList events that we should convert
   * @param {Object} convertList a map with the events name
   *
   * @return the new event list with the event names converted
   * @type Array
   * @private
   */
  CommPortalEvent.prototype.convertEventName = function(eventList, convertList)
  {
    // Ensure we are manipulating an array
    eventList = toArray(eventList);

    var result = [];

    // For each event in the list, check if there is the corresponding one in
    // the convert list
    for (var i = 0; i < eventList.length; i++)
    {
      var event = eventList[i];
      if (event in convertList)
      {
        // Convert the event name and add it to the list
        result.push(convertList[event]);
      }
      else
      {
        // We also add the ones that were not found in the convert list
        result.push(event);
      }
    }

    return result;
  };

  /**
   * Fetch some required data for the events
   *
   * <p>
   * We are interested in the Class of Service and Subscriber number.
   * The Class of Service will be used to check if the user is allowed to
   * listen to an specific event.  The subscriber number is used to identify
   * events for the default subscriber number.
   *
   * @param callback the callback called after we finished retrieving the data
   *   from the server
   * @private
   */
  CommPortalEvent.prototype.fetchRequiredData = function(connection, callback)
  {
    var commPortalEvent = this;
    var requiredData =
    {
      cos     : "Meta_Subscriber_MetaSphere_ClassOfService",
      session : "Session"
    };

    commonFetchRequiredData(requiredData, connection, processData);

    function processData()
    {
      commPortalEvent.classOfService = connection.cache.cos;
      if (connection.cache.session.ManagedSubscribers[0])
      {
        commPortalEvent.defaultLine =
                connection.cache.session.ManagedSubscribers[0].DirectoryNumber;
      }
      // Call the callback
      callback();
    }
  };

  /**
   * Return true if we have events in the queue
   *
   * @return true if we have request queued
   * @type boolean
   * @private
   */
  CommPortalEvent.prototype.hasEvents = function()
  {
    return this.queuedEventRequests.length > 0;
  };

  /**
   * Check if we can listen to the events checking the Class of Service to
   * see if we are allowed and if there isn't any incompatible events to be
   * listen for
   *
   * @param {String/String[]} eventList events that we should check
   * @param {String} target line number to request events on
   * @return an error if the events are not allowed to be listened for or if
   *   there is an incompatible event to listen to or undefined if not
   * @type error
   * @private
   */
  CommPortalEvent.prototype.checkEventError = function(eventList, target)
  {
    // Check if we don't have any not allowed event to be listen
    var error = this.checkEventNotAllowed(eventList);

    if (!error)
    {
      // Check if we don't have any incompatible event to be listened for
      error = this.checkEventIncompatible(eventList, target);
    }

    return error;
  };

  /**
   * Check if the subscriber is allowed to listen to the events
   *
   * Note that the event name used here are the ones that are exposed to the
   * user and not the ones that are used by the CommPortal.
   *
   * @param {String/String[]} eventList the events that we should check
   * @return an error indicating that the subscriber is not allowed to listen
   *   to one or more events or undefined if the subscriber is allowed
   * @type error
   * @private
   */
  CommPortalEvent.prototype.checkEventNotAllowed = function(eventList)
  {
    eventList = toArray(eventList);
    var eventNotAllowed = [];
    for (var i in eventList)
    {
      if (eventList.hasOwnProperty(i))
      {
        // Get the related CoS service that we should check
        var cosService = getService(eventList[i]);
        if ((cosService) &&
            (this.classOfService[cosService.service] != cosService.value))
        {
          // We are not allowed to listen to the event
          eventNotAllowed.push(eventList[i]);
        }
      }
    }

    // Get the related event CoS service and value
    function getService(event)
    {
      var service = null;
      var value = null;
      switch (event)
      {
      case "IncomingCall":
        service = "IcpAllowed";
        value   = true;
        break;
      case "IncomingCallManager":
        service = "IcmAllowed";
        value   = true;
        break;
      }

      var cosService = null;
      if (service && value)
      {
        var cosService =
        {
          service : service,
          value   : value
        };
      }

      return cosService;
    }

    var result = undefined;
    if (eventNotAllowed.length > 0)
    {
      // Create the error indicating that there are one or more events that
      // the subscriber is not allowed to listen for
      result = new CommPortalError(CommPortal.ERROR_SUBSCRIPTION_NOT_ALLOWED,
                                   {events : eventNotAllowed});
    }

    return result;
  };

  /**
   * Return an error if the events are incompatible to the ones that are
   * already being listened for or undefined if the events are compatible.
   *
   * @param {String/String[]} eventList which events should be checked
   * @param {String} target line number to request events on
   *
   * @return an error if the events are incompatible or undefined if not
   * @type error
   * @private
   */
  CommPortalEvent.prototype.checkEventIncompatible = function(eventList,
                                                              target)
  {
    // Set the target if it isn't defined and ensure we are manipulating an
    // array
    target = this.normalizeTarget(target);
    eventList = toArray(eventList);

    var result = undefined;

    // We can only have problems when listen to IncomingCall
    var incompatibleEvent = "IncomingCall";

    // Check if the item is on the list
    function checkList(item, list)
    {
      var result = false;
      for (var i = 0; i < list.length; i++)
      {
        if (list[i] == item)
        {
          result = true;
          break;
        }
      }
      return result;
    }

    // Check if there is any incompatible event on event List
    var iCOnEventList = checkList(incompatibleEvent, eventList);

    // Check if there is any incompatible event on queued events
    var iCOnQueueEvent = false;
    var queueTargetList = [];
    for (var i = 0; i < this.queuedEventRequests.length; i++)
    {
      var queuedEvent = this.queuedEventRequests[i];
      // Check if there is an incompatible event on the queue
      iCOnQueueEvent = (iCOnQueueEvent ||
                        checkList(incompatibleEvent, queuedEvent.events));
      // Save all targets line
      queueTargetList.push(queuedEvent.target);
    }

    if ((iCOnEventList && queueTargetList.length > 1) ||
        ((iCOnEventList && queueTargetList.length == 1) &&
         (queueTargetList[0] != target)) ||
        (!iCOnEventList && iCOnQueueEvent && queueTargetList[0] != target))
    {
      // We return the incompatible event error for one of the following three
      // possibilities.
      // We have Incoming Call on eventList and we are listening to more than
      // one target line.
      // We have Incoming Call on eventList and the target line that we are
      // listening to events on is different from the new target
      // We are listening for Incoming Call and the new target is different
      // from the target for the Incoming Call
      result = new CommPortalError(CommPortal.ERROR_INCOMPATIBLE_EVENTS);
    }

    return result;
  };

  /**
   * Set the maximum timeout used on the request for events
   *
   * @param {Integer} timeout [optional] maximum timeout to use in milliseconds
   * @private
   */
  CommPortalEvent.prototype.setRequestTimeout = function(timeout)
  {
    if (timeout && timeout >= 0)
    {
      this.timeout = timeout;
    }
  };

  /**
   * Set the target to the subscriber line number if it is undefined
   *
   * @param {String} target [optional] line number
   * @return the subscriber line if the target was undefined or the target
   *   unmodified if not
   * @type String
   * @private
   */
  CommPortalEvent.prototype.normalizeTarget = function(target)
  {
    return target ? target : this.defaultLine;
  };

  /**
   * Set the target to an empty string if the target is the subscriber line
   * number
   *
   * @param {String} target line number
   * @return the target line as an empty string if the target is equal to the
   *   subscriber line or the target unmodified if not
   * @type String
   * @private
   */
   CommPortalEvent.prototype.unnormalizeTarget = function(target)
   {
     return target == this.defaultLine ? "" : target;
   };

  /**
   * Get the index of the event in the eventObjectList
   *
   * @param {String} event the event name
   * @param {String} target line number
   * @return the event index or -1 if not found
   * @type integer
   * @private
   */
  CommPortalEvent.prototype.getEventIndex = function(event, target)
  {
    var result = -1;

    // Iterate through the event object list to find its position
    for (var index = 0; index < this.eventObjectList.length; index++)
    {
      var eventObject = this.eventObjectList[index];
      if (eventObject.event == event && eventObject.target == target)
      {
        // We found the event, so stop looking for it
        result = index;
        break;
      }
    }

    return result;
  };

  /**
   * Get the callback associated with the event
   *
   * @param {String} event the event name
   * @param {String} target line number
   * @return the event callback
   * @type Function
   * @private
   */
  CommPortalEvent.prototype.getEventCallback = function(event, target)
  {
    var callback = null;
    // Get the index of the event in our list
    var index = this.getEventIndex(event, target);
    if (index >= 0 && index < this.eventObjectList.length)
    {
      // Get the callback of the event
      var callback = this.eventObjectList[index].callback;
    }

    return callback;
  };

  /**
   * Remove the events from the eventObjectList
   *
   * @param {String/String[]} eventList which events we should remove
   * @param {String} target [optional] line number
   *
   * @return undefined on success or an error if one of the events on the
   *   eventList was not found
   * @type error
   * @private
   */
  CommPortalEvent.prototype.removeEventList = function(eventList, target)
  {
    // Ensure that we are manipulating an array
    eventList = toArray(eventList);

    var eventsNotFound = [];

    // Normalize the target to a valid value as it can be undefined
    target = this.normalizeTarget(target);

    // Iterate through the event list and remove all of them
    for (var i = 0; i < eventList.length; i++)
    {
      var event = eventList[i];

      // Get the index of the event on our list
      var index = this.getEventIndex(event, target);

      if (index > -1)
      {
        // Remove the event from the list
        this.eventObjectList.splice(index, 1);
      }
      else
      {
        // Add the event to the not found list
        eventsNotFound.push(event);
      }
    }

    var result = undefined;
    if (eventsNotFound.length > 0)
    {
      // We didn't find one or more events to remove
      result = new CommPortalError(CommPortal.ERROR_NO_CORRESPONDING_HANDLER,
                                   {events : eventsNotFound});
    }

    return result;
  };

  /**
   * Add the events and their callback to the eventObjectList.
   *
   * @param {String/String[]} eventList which events we should subscribe to
   * @param {function} eventCallback callback called for each event
   * @param {String} target [optional] line number
   * @param {Integer} timeout [optional] maximum timeout to use
   * @private
   */
  CommPortalEvent.prototype.addEventList = function(eventList,
                                                    eventCallback,
                                                    target,
                                                    timeout)
  {
    // Ensure that we are manipulating an array
    eventList = toArray(eventList);

    // Normalize the target to a valid value as it can be undefined
    target = this.normalizeTarget(target);

    // Set the new timeout, if valid
    this.setRequestTimeout(timeout);

    // Add all events to our events list
    for (var i = 0; i < eventList.length; i++)
    {
      var event = eventList[i];

      // Remove any existent event with the same name
      this.removeEventList(event, target);

      // Create the new event and add it to our list
      var newEventObject =
      {
        event : event,
        callback : eventCallback,
        target : target
      };
      this.eventObjectList.push(newEventObject);
    }
  };

  /**
   * Create the queue to poll the server for events merging events for the same
   * target
   *
   * @private
   */
  CommPortalEvent.prototype.createQueue = function()
  {
    this.queuedEventRequests = [];

    // Group the events by target
    var targets = {};
    for (var i = 0; i < this.eventObjectList.length; i++)
    {
      var eventObject = this.eventObjectList[i];

      if (targets[eventObject.target])
      {
        // Target already exists, just add the event to the list
        targets[eventObject.target].push(eventObject.event);
      }
      else
      {
        // New target found, create an instance of the target
        targets[eventObject.target] = [eventObject.event];
      }
    }

    // Queue all events for the same target
    for (var key in targets)
    {
      if (targets.hasOwnProperty(key))
      {
        var eventQueued =
        {
          target: key,
          events: targets[key]
        };

        this.queuedEventRequests.push(eventQueued);
      }
    }
  };

  /**
   * Create the URL that will be used to poll the server for events
   *
   * @param {Object} eventQueued event object from the queue
   * @param {function} callback the callback that will be called by the data
   *   returned from the server request
   * @return the URL to use in the resquest
   * @type String
   *
   * @private
   */
  CommPortalEvent.prototype.createEventURL = function(eventQueued, callback)
  {
    // We have some events we are interested in.
    var eventURL = "events?events=" + eventQueued.events.join(",");

    // Timeout is OPTIONAL but server currently requires it
    eventURL += "&timeout=" + this.timeout;

    // We must always give the version number
    eventURL += "&version=" + connection.interfaceVersion;

    // We must always give a callback
    eventURL += "&callback=" + callback;

    // Set the call if this is a connection subscription and there is a call
    // ID.
    var callId = connection.getCallId();

    if ((eventQueued.events[0] == "Connection") && (callId != null))
    {
      eventURL = "call" + callId + "/" + eventURL;
    }

    // Set the target line
    var target = this.unnormalizeTarget(eventQueued.target);
    eventURL = "line" + target + "/" + eventURL;

    // Create the session URL
    var sessionURL = connection.server + "/session" + connection.sessionId;
    eventURL = sessionURL + "/" + eventURL;

    return eventURL;
  };

  /**
   * Cleanup any outstanding request removing the callback and the tag script
   *
   * @param {String} callbackName name of the callback that we should clean
   * @param {Object} scriptTag the html script tag to be removed
   * @private
   */
  CommPortalEvent.prototype.cleanupRequest = function(callbackName, scriptTag)
  {
    var timeoutHandle;
    function cleanupFunction()
    {
      try
      {
        // Get rid of this function
        delete CommPortal[callbackName];
      }
      catch (exception)
      {
        // Fails in IE 6, just null out instead.
        CommPortal[callbackName] = undefined;
      }

      clearTimeout(timeoutHandle);
    }
    CommPortal[callbackName] = cleanupFunction;

    // Schedule a timer to clear it up completely in a minute's time.  If the
    // request did take longer than this then all that happens is we get a
    // benign JS error.
    timeoutHandle = setTimeout(cleanupFunction, 60 * 1000);

    // Try to force the browser to cancel any outstanding request for the
    // script file by removing it from the DOM.  We also tried changing the
    // src to null, but this fails in IE, which requests "null" from the
    // server.
    if (scriptTag)
    {
      // Remove the src attribute, which stops any outstanding browser request
      scriptTag.removeAttribute("src");

      // The element hasn't been cleaned up yet
      var parent = scriptTag.parentNode;
      if (parent)
      {
        parent.removeChild(scriptTag);
      }
    }
  };

  // Private methods
  // Unfortunately due to a bug in jsDoc, we need to explictly mark these all
  // as private to avoid them showing up as GLOBAL functions (which they most
  // definitely are not)

  /**
   * Takes a URL and parses out the query string, returning it as an object
   * mapping keys to values.
   */
  /** @private */
  function parseQueryStringParams(url)
  {
    var hashIdx = url.indexOf("#");
    var params = {};

    if (hashIdx != -1)
    {
      url = url.substring(0, hashIdx);
    }
    var qsIdx = url.indexOf("?");
    if (qsIdx != -1)
    {
      // Got a query string, extract it
      var qs = url.substring(qsIdx + 1);

      // Split on &
      var splits = qs.split("&");

      // Spin over the key-value pairs, adding to the result
      for (var i = 0; i < splits.length; i++)
      {
        var keyValue = splits[i];

        var keyValueArray = keyValue.match(/([^=]*)=(.*)/);
        if (keyValueArray)
        {
          params[keyValueArray[1]] = keyValueArray[2];
        }
      }
    }

    return params;
  }

  /**
   * Convert the object form of object ID to the string form for use in a data
   * request.
   *
   * @param objectIdentity
   *          The object-form object identity to convert.
   * @return the string form of the the object identity.
   * @type String
   */
  /** @private */
  function objectIdentityToString(objectIdentity)
  {
    var string = "";

    if (objectIdentity)
    {
      var properties = [];
      for (var prop in objectIdentity)
      {
        properties.push(prop + "=" + objectIdentity[prop]);
      }
      string = properties.join("&");
    }

    return string;
  }

  /**
   * Create a new unique ID for use in naming generated elements etc.
   *
   * @param {String}
   *          base An ID-safe string used as part of the output ID. e.g. "iFrame"
   *          would be a good base when naming an iFrame.
   * @return An ID-safe string that is very likely to be unique.
   * @type String
   */
  /** @private */
  function createUniqueId(base)
  {
    return "CommPortal_" + base + "_" + sUniqueId++;
  }

  /**
   * Removes the specified element from the page.
   *
   * @param {Element} element The element to remove (may be undefined)
   */
  /** @private */
  function removeElement(element)
  {
    if (element)
    {
      var parent = element.parentNode;
      if (parent)
      {
        parent.removeChild(element);
      }
    }
  }

  /**
   * Append a script tag to the page.
   *
   * Stored in a variable to allow it to be more easily replaced for testing.
   */
  /** @private */
  var appendScriptTag = function(url)
  {
    var scriptTag = document.createElement("script");
    scriptTag.setAttribute("src", url);
    scriptTag.setAttribute("type", "text/javascript");
    document.body.appendChild(scriptTag);

    return scriptTag;
  };

  /**
   * Flattens the data object, to make this much more like a standard
   * javascript object, and less like the representation of an XML data
   * structure that is ultimately is.
   */
  /** @private */
  function flattenDataObject(obj, dataType)
  {
    // We are more thorough in checking for an array than you might expect.
    // This is because we expect to run in the presence of other libraries, or
    // simply other code, and javascript allows objects to be augmeted in ways
    // you would not expect
    if (Object.prototype.toString.apply(obj) === "[object Array]")
    {
      // This object is an array - so flatten each item
      var flat = [];

      for (var i = 0; i < obj.length; i++)
      {
        flat.push(flattenDataObject(obj[i], dataType));
      }
    }
    else
    {
      // This is an object, so flatten each of its fields
      var flat = {};
      for (var field in obj)
      {
        if (obj.hasOwnProperty(field))
        {
          if (field == "_")
          {
            // The field uses the _ convention
            flat = obj[field];
          }
          else
          {
            // The default is that we use the same name in the flat version
            var outfield = field;

            if (outfield[0] == "_")
            {
              // The name starts with a awkward underscore - discard it
              outfield = outfield.substring(1);
            }

            if (outfield == outfield.toUpperCase() &&
                !outfield.match("^(MADN|HTTP|SIP|URL)$"))
            {
              // The fieldname is UGLY CAPS and not a known acronym, so change
              // it to an easier to read Initial Caps.
              outfield = outfield.charAt(0) + outfield.substring(1).toLowerCase();
            }

            if (obj[field]._ !== undefined)
            {
              // This field has a value, so use that value
              flat[outfield] = obj[field]._;
            }
            else if (obj[field].Value && obj[field].Value._ !== undefined)
            {
              // This object has a default and value, so just use the value
              flat[outfield] = obj[field].Value._;
            }
            else if (obj[field] !== null && typeof obj[field] === "object")
            {
              // Some object, handled recursively
              flat[outfield] = flattenDataObject(obj[field], dataType);
            }
            else if (typeof obj[field] !== "function")
            {
              // Simple field
              flat[outfield] = obj[field];
            }
          }
        }
      }
    }

    return flat;
  }

  /**
   * Handles some special cases where the wire format is particularly poor.
   */
  /** @private */
  function handleSpecialCases(dataType, data)
  {
    if (dataType == "Meta_Subscriber_MetaSphere_SubscriberCapabilities")
    {
      // Subscriber caps is an array of objects each containing a Name and a
      // Value - so we convert them into a single object, with each
      // name being a property of that object - far easier to work with!
      var newData = {};

      for (var i = 0; i < data.length; i++)
      {
        var name = data[i].Name;
        var value = data[i].Value;

        // Remove any redundant "dcl" prefix from the name
        if (name.substring(0,3) == "dcl")
        {
          name = name.substring(3);
        }

        // Treat string representation of boolean concepts as real booleans
        value = (value == "true") ? true :
                (value == "false") ? false :
                value;

        newData[name] = value;
      }

      data = newData;
    }
    else if (dataType == "Meta_Subscriber_CallLists" ||
             dataType == "Meta_Subscriber_MetaSphere_CallList")
    {
      // Call lists have duration given as a display 00:00:00 form, but it's
      // much better to have these as a number of seconds
      // NB. Missed calls do not have a duration, so we don't look for those.
      var names = ["DialedCalls",
                   "AnsweredCalls",
                   "RejectedCalls"];
      for (var i = 0; i < names.length; i++)
      {
        var calls = data[names[i]];
        if (calls)
        {
          calls = calls.Call;
          if (calls)
          {
            for (var c = 0; c < calls.length; c++)
            {
              var asText = calls[c].Duration;
              var parts = asText.split(":");
              var duration = (parseInt(parts[0], 10) * 60 * 60) +
                             (parseInt(parts[1], 10) * 60) +
                             parseInt(parts[2], 10);

              calls[c].Duration = duration;
            }
          }
        }
      }
    }

    return data;
  }

  /** @private */
  function createiFrame(id)
  {
    // If we create an iframe via createElement then we don't seem to be able
    // to set the "name" attribute properly, so we do it the indirect way
    // via innerHTML
    var outer = document.createElement("div");

    var iframeText =
      '<iframe name="' + id + '" id="' + id +
      '" src="about:blank" style="display:none"></iframe>';
    outer.innerHTML = iframeText;

    var iframe = outer.firstChild;

    document.body.appendChild(iframe);

    return iframe;
  }

  /** @private */
  function createForm(id,
                      iframe,
                      action,
                      fields)
  {
    var target = iframe.getAttribute("name");

    var form = document.createElement("form");
    form.setAttribute("id", id);
    form.setAttribute("action", action);
    form.setAttribute("method", "post");
    form.setAttribute("target", target);
    form.style.display = "none";

    for (var name in fields)
    {
      if (fields.hasOwnProperty(name))
      {
        var value = fields[name];

        var input = document.createElement("input");
        input.setAttribute("name", name);
        input.setAttribute("value", value);
        input.setAttribute("type", "hidden");  // unnecessary since whole form is hidden!

        form.appendChild(input);
      }
    }

    document.body.appendChild(form);

    return form;
  }

  /** @private */
  function encodeFieldsForURL(fields)
  {
    var urlFields = [];

    for (var i in fields)
    {
      if (fields.hasOwnProperty(i))
      {
        urlFields.push(encodeURIComponent(i) + "=" + encodeURIComponent(fields[i]));
      }
    }

    return urlFields.join("&");
  }

  /** @private */
  function simulateSubmit(frame,
                          url)
  {
    // On Vista sidebar, we cannot do a form submission, since that breaks out
    // of the sidebar, and instead loads in a separate window.  To simulate the
    // submission, we instead write a new document in the frame that redirects
    // to where we really want to go.

    // We need this option to support Vista, so contentWindow is the place to write,
    // but for test purposes we also check the standards location first
    var doc = frame.contentDocument || frame.contentWindow.document;

    //CodeForTesting4 -- do not delete this line, it is used for automated testing

    doc.write('<meta http-equiv="refresh" content="0;url=' + url + '" />');
    doc.close();

    // Writing a new document can make the browser change the location url, so we need to
    // fetch what it thinks is current now, so that the polling code can tell when it changes
    return doc.location.href;
  }

  /** @private */
  function pollForResponse(connection,
                           frameOrWindow,
                           successCallback,
                           failureCallback,
                           handleURL,
                           previousURL)   // optional
  {
    var poll = setInterval(function()
    {
      if (frameOrWindow.closed)
      {
        // The user has closed the window

        // Stop polling
        clearInterval(poll);

        queueError(failureCallback,
                   connection,
                   CommPortal.ERROR_LOGIN_WINDOW_CLOSED);
      }
      else
      {
        try
        {
          // Assume this window is an iframe - we need to check its content document
          var url = frameOrWindow.contentDocument.location.href;
        }
        catch (e)
        {
          try
          {
            // Try the IE iframe variant
            var url = frameOrWindow.contentWindow.document.location.href;
          }
          catch (e2)
          {
            try
            {
              // Lastly we check if this is a window not an iframe
              var url = frameOrWindow.location.href;
            }
            catch (e3)
            {
            }
          }
        }

        // Safari sends newly opened windows to a url of "/", whereas other
        // browsers use "about:blank"
        if (url && url != "about:blank" && url != "/" && url != previousURL)
        {
          // Stop polling
          clearInterval(poll);

          // Act on the parameters contained in the url
          handleURL(url, connection, frameOrWindow, successCallback, failureCallback);
        }
      }
    },
    25);
  }

  /** @private */
  function toArray(object)
  {
    return (object instanceof Array) ? object : [object];
  }

  /**
   * Compare two version strings, e.g. "7.1" and "6.0".
   * Returns:
   *   -1 if firstVersion comes before secondVersion
   *   0 if the versions are the same
   *   1 if firstVersion comes after secondVersion.
   */
  /** @private */
  function compareVersion(firstVersion, secondVersion)
  {
    var comp = 0;

    // Split the version strings.
    var splitVersionOne = firstVersion.split(".");
    var splitVersionTwo = secondVersion.split(".");

    // Get the integer values.
    var firstVersionMajor = parseInt(splitVersionOne[0], 10);
    var firstVersionMinor = parseInt(splitVersionOne[1], 10);
    var secondVersionMajor = parseInt(splitVersionTwo[0], 10);
    var secondVersionMinor = parseInt(splitVersionTwo[1], 10);

    // Check the major version number first.
    if (firstVersionMajor > secondVersionMajor)
    {
      comp = 1;
    }
    else if (firstVersionMajor < secondVersionMajor)
    {
      comp = -1;
    }
    else
    {
      // Check the minor version number.
      if (firstVersionMinor > secondVersionMinor)
      {
        comp = 1;
      }
      else if (firstVersionMinor < secondVersionMinor)
      {
        comp = -1;
      }
    }

    return comp;
  }

  //---------------------------------------------------------------------------
  // The following empty functions are to make the documentation clearer
  // and are never explicitly called
  //---------------------------------------------------------------------------

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * The methods described here are callback methods that various methods
   * in the {@link CommPortal} SDK API call.
   */
  function callbacks() {}

  /**
   * General success callback signature used by a number of methods.
   * @param {CommPortal} connection the connection in use
   */
  callbacks.prototype.successCallback = function(connection) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#login}.
   * @param {CommPortal} connection the connection in use
   * @param {String} sessionId the session id
   */
  callbacks.prototype.loginCallback = function(connection, sessionId) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchToken}.
   * @param {CommPortal} connection the connection in use
   * @param {String} token the persistent login token
   */
  callbacks.prototype.tokenCallback = function(connection, token) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchVoicemailCount} and
   * {@link CommPortal#fetchFaxCount}.
   * @param {CommPortal} connection the connection in use
   * @param {Integer} total the total number of messages
   * @param {Integer} unread the number of unread or unheard messages
   */
  callbacks.prototype.countsCallback = function(connection, total, unread) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchVoicemails}.
   * @param {CommPortal} connection the connection in use
   * @param {voicemail[] voicemail} voicemails the array of voicemail objects
   */
  callbacks.prototype.voicemailsCallback = function(connection, voicemails) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchFaxes}.
   * @param {CommPortal} connection the connection in use
   * @param {fax[] fax} faxes the array of fax objects
   */
  callbacks.prototype.faxesCallback = function(connection, faxes) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchGreetings}.
   * @param {CommPortal} connection the connection in use
   * @param {greetings} greetingsdata greetings data
   */
  callbacks.prototype.greetingsCallback = function(connection, greetingsdata) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchSubscriberNumber}.
   * @param {CommPortal} connection the connection in use
   * @param {String} number the phone number
   */
  callbacks.prototype.numberCallback = function(connection, number) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchSubscriberName}.
   * <p>New in version 7.1</p>
   * @param {CommPortal} connection the connection in use
   * @param {String} name the subscribers name
   */
  callbacks.prototype.nameCallback = function(connection, name) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchMissedCalls},
   * {@link CommPortal#fetchDialedCalls},
   * {@link CommPortal#fetchAnsweredCalls}.
   * @param {CommPortal} connection the connection in use
   * @param {call[] call} calls the array of call objects
   */
  callbacks.prototype.callsCallback = function(connection, calls) {};

  /**
   * Success callback signature used by
   * {@link CommPortal#fetchContacts}.
   * @param {CommPortal} connection the connection in use
   * @param {contact[] contact} contacts the array of contact objects
   */
  callbacks.prototype.contactsCallback = function(connection, contacts) {};

  /**
   * Callback signature used by
   * {@link CommPortal#performAction}.
   * @param {CommPortal} connection the connection in use
   * @param {Object} actionData the action data, whose structure varies
   *        by action type
   */
  callbacks.prototype.actionCallback = function(connection, actionData) {};

  /**
   * Callback signature used by
   * {@link CommPortal#subscribeToEvents}.
   * @param {CommPortal} connection the connection in use
   * @param {String} number phone number of the line the event relates to
   * @param {String} eventType the event type that occurred as passed to subscribeToEvents
   * @param {Object} eventData the event data, whose structure varies by event type
   * @param {String} eventSubType the event subtype (if applicable)
   */
  callbacks.prototype.eventCallback = function(connection, number, eventType, eventData, eventSubType) {};

  /**
   * Callback signature used by incoming call handler
   * {@link CommPortal#setIncomingCallHandler}.
   * @param {CommPortal} connection the connection in use
   * @param {String} number line number the incoming call is on
   * @param {incomingcalldata} callData the data containing the information
   *   about the incoming call
   */
  callbacks.prototype.incomingCallback = function(connection, number, callData) {};

  /**
   * General failure callback signature used by many methods.
   * @param {CommPortal} connection the connection in use
   * @param {error} error the error that occured
   */
  callbacks.prototype.failureCallback = function(connection, error) {};

  /**
   * Progress callback signature used by
   * {@link CommPortal#makeCall}.
   * @param {CommPortal} connection the connection in use
   * @param {String} callId the call ID
   * @param {callstate} state the current progress state of the phone call
   */
  callbacks.prototype.progressCallback = function(connection, callId, state) {};

  /**
   * Success callback called once for each successfully returned piece of data
   * requested by {@link CommPortal#fetchData}.
   * @param {CommPortal} connection the connection in use
   * @param {String} dataType the dataType as used by the JSON interface
   * @param {Object} data the actual data, generally in a flattened and cleaned up form
   * @param {Object} objectIdentity the object identity as delivered by the JSON interface
   */
  callbacks.prototype.fetchCallback = function(connection, dataType, data, objectIdentity) {};

  /**
   * Callback set to handle URL for fetching data.
   * @private
   * @param url the requested url
   */
  callbacks.prototype.getRequestCallback = function(url) {};

  /**
   * Callback set to handle fetched data
   * @private
   * @param callbackName the name of the callback used by the SDK when returning data
   * @param objectIdentity the context of the current subscriber
   * @param dataType the service indication
   * @param getData the fetched data
   * @param getErrors any errors in fetching the data
   * @param updateData data to be updated (usually null)
   * @param updateErrors errors updating data (usually null)
   */
  callbacks.prototype.getResponseCallback = function(callbackName, objectIdentity,
                       dataType, getData, getErrors, updateData, updateErrors) {};

  /**
   * Callback set to handle form built to make update request
   * @private
   * @param form DOM form object
   */
  callbacks.prototype.updateRequestCallback = function(form) {};

  /**
   * Callback set to handle update redirect url
   * @private
   * @param url redirect url
   */
  callbacks.prototype.updateResponseCallback = function(url) {};

  /**
   * Success callback set to handle fetched transcription settings used by
   * {@link CommPortal#fetchTranscriptionEnabled}.
   * @param {CommPortal} connection the connection in use
   * @param {boolean} transcriptsEnabled whether STT transcriptions are enabled for this subscriber.
   */
  callbacks.prototype.transcriptsEnabledCallback = function(connection, transcriptsEnabled) {};

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that describe a voicemail.
   */
  function voicemail() {}
  /**
   * The phone number of the caller that left the voicemail, (missing if the number was withheld, or otherwise unavailable)
   * @type String
   */
  voicemail.prototype.From = 0;
  /**
   * The date and time when the voicemail was left (as a string)
   * @type String
   */
  voicemail.prototype.Received = 0;
  /**
   * A unique identifier for the voicemail - only valid within this login session
   * @type String
   */
  voicemail.prototype.Id = 0;
  /**
   * Not normally present, but may occur when the voicemail was sent from a system that is able to provide names as well as numbers
   * @type String
   */
  voicemail.prototype.Name = 0;
  /**
   * A boolean which is set if the voicemail has been marked as listened to
   * @type boolean
   */
  voicemail.prototype.Read = 0;
  /**
   * A boolean which is set if the voicemail has been marked as urgent
   * @type boolean
   */
  voicemail.prototype.Urgent = 0;
  /**
   * A boolean which is set if the voicemail has been marked as private
   * @type boolean
   */
  voicemail.prototype.Private = 0;
  /**
   * The size in bytes of the voicemail that was left
   * @type Integer
   */
  voicemail.prototype.Size = 0;
  /**
   * The URL of the wav file holding the voicemail audio - only valid within this login session
   * Requesting this file will implicitly mark the voicemail as heard, so after accessing this
   * you should call {@link CommPortal#confirmVoicemailsHeard}.
   * @type String
   */
  voicemail.prototype.AudioFile = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that describe a fax.
   */
  function fax() {}
  /**
   * The phone number of the caller that sent the fax, (missing if the number was withheld, or otherwise unavailable)
   * @type String
   */
  fax.prototype.From = 0;
  /**
   * The date and time when the fax was sent (as a string)
   * @type String
   */
  fax.prototype.Received = 0;
  /**
   * A unique identifier for the fax - only valid within this login session
   * @type String
   */
  fax.prototype.Id = 0;
  /**
   * Not normally present, but may occur when the fax came from a system that is able to provide names as well as numbers
   * @type String
   */
  fax.prototype.Name = 0;
  /**
   * A boolean which is set if the fax has been marked as viewed
   * @type boolean
   */
  fax.prototype.Read = 0;
  /**
   * A boolean which is set if the fax has been marked as urgent
   * @type boolean
   */
  fax.prototype.Urgent = 0;
  /**
   * A boolean which is set if the fax has been marked as private
   * @type boolean
   */
  fax.prototype.Private = 0;
  /**
   * The size in bytes of the fax
   * @type Integer
   */
  fax.prototype.Size = 0;
  /**
   * The number of pages in the fax
   * @type Integer
   */
  fax.prototype.Pages = 0;
  /**
   * The URL of the tiff file holding the fax image - only valid within this login session.
   * Requesting this file will implicitly mark the fax as viewed, so after accessing this
   * you should call {@link CommPortal#confirmFaxesViewed}.
   *
   * @type String
   *
   * @see CommPortal#confirmFaxesViewed
   */
  fax.prototype.ImageFile = 0;
  /**
   * The URL of the pdf file holding the fax image - only valid within this login session.
   * Requesting this file will implicitly mark the fax as viewed, so after accessing this
   * you should call {@link CommPortal#confirmFaxesViewed}.
   *
   * @type String
   *
   * @see CommPortal#confirmFaxesViewed
   */
  fax.prototype.ImageFilePDF = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that describe greetings data.
   *
   * <p>New in version 7.3</p>
   */
  function greetings() {}

  /**
   * Details for each of the available greetings for the subscriber
   *
   * @type greeting[]
   */
  greetings.prototype.greetingsList = 0;

  /**
   * The current default greeting type.
   *
   * @type String
   */
  greetings.prototype.defaultGreetingType = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that describe greetings data.
   *
   * <p>New in version 7.3</p>
   */
  function greeting() {}

  /**
   * Whether this greeting is available for being the default greeting
   *
   * @type Boolean
   */
  greeting.prototype.availableForDefault = 0;

  /**
   * Whether this greeting can be recorded
   *
   * @type Boolean
   */
  greeting.prototype.recordable = 0;

  /**
   * The type of this greeting
   *
   * @type String
   */
  greeting.prototype.greetingType = 0;

  /**
   * Whether this greeting is recorded
   *
   * @type Boolean
   */
  greeting.prototype.isRecorded = 0;

  /**
   * A URL at which the greeting file can be accessed
   *
   * @type String
   */
  greeting.prototype.audioFile = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that describe a contact.
   */
  function contact() {}

  /**
   * The first or given name of the person
   * @type String
   */
  contact.prototype.givenName = 0;
  /**
   * The surname or family name of the person
   * @type String
   */
  contact.prototype.familyName = 0;
  /**
   * A convenient combination of the given and family names
   * @type String
   */
  contact.prototype.displayName = 0;
  /**
   * A familiar name or nickname of the person
   * @type String
   */
  contact.prototype.nickname = 0;
  /**
   * The organization the person is associated with - often their employer
   * @type String
   */
  contact.prototype.organization = 0;
  /**
   * The person's job title
   * @type String
   */
  contact.prototype.jobTitle = 0;
  /**
   * The person's sms contact details
   * @type String
   */
  contact.prototype.sms = 0;
  /**
   * An id that identifies this contact object for other API calls
   * @type String
   */
  contact.prototype.uid = 0;
  /**
   * An array of up to 5 phone numbers that can be used to contact the person
   * @type String[0-5]
   */
  contact.prototype.phone = 0;
  /**
   * This array mirrors the phone array field, and for each entry provides a
   * type for the corresponding number - one of "home", "work", "cell", "fax" or empty.
   * @type String[0-5]
   */
  contact.prototype.phoneType = 0;
  /**
   * An array of up to 2 email addresses that can be used to contact the person
   * @type String[0-2]
   */
  contact.prototype.email = 0;
  /**
   * An array of up to 2 postal {@link address} that can be used to contact the person.
   * @type address[0-2]
   */
  contact.prototype.address = 0;
  /**
   * This array mirrors the address array field, and for each entry provides a
   * type for the corresponding address - one of "home" or "work"
   * @type String[0-2]
   */
  contact.prototype.addressType = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that form an address within a {@link contact}.
   */
  function address() {}

  /**
   * The street part of the address
   * @type String
   */
  address.prototype.street = 0;
  /**
   * The locality part of the address - for a US address this is typically the city
   * @type String
   */
  address.prototype.locality = 0;
  /**
   * For a US address this typically holds the State (or state abbreviation)
   * @type String
   */
  address.prototype.region = 0;
  /**
   * For a US address this holds the ZIP code
   * @type String
   */
  address.prototype.postalcode = 0;
  /**
   * As it says, the country part of the address
   * @type String
   */
  address.prototype.country = 0;
  /**
   * A convenience field - which joins together the various parts of the address into one value
   * @type String
   */
  address.prototype.displayAddress = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the fields that make up a call record.
   */
  function call() {}
  /**
   * May be present if the network provided a name when identifying the caller
   * @type String
   */
  call.prototype.Name = 0;
  /**
   * The phone number that was involved in this call, if available
   * @type String
   */
  call.prototype.DirectoryNumber = 0;
  /**
   * The date and time the call was dialed - as an English language string
   * @type String
   */
  call.prototype.DateTime = 0;
  /**
   * If this represents something other than a missed call,
   * the duration of the call, in seconds
   * @type Integer
   */
  call.prototype.Duration = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the properties that make up a callstate passed to the
   * {@link callbacks#progressCallback} of a {@link CommPortal#makeCall}.
   */
  function callstate() {}

  /**
   * A numeric value that identifies which state the call is in.
   *
   * One of
   *
   * <ul>
   *   <li>{@link CommPortal#CALLSTATE_CALLING}</li>
   *   <li>{@link CommPortal#CALLSTATE_FIRST_RINGING}</li>
   *   <li>{@link CommPortal#CALLSTATE_FIRST_ANSWERED}</li>
   *   <li>{@link CommPortal#CALLSTATE_SECOND_RINGING}</li>
   *   <li>{@link CommPortal#CALLSTATE_SECOND_ANSWERED}</li>
   *   <li>{@link CommPortal#CALLSTATE_CLEARING}</li>
   *   <li>{@link CommPortal#CALLSTATE_CLEARED}</li>
   *   <li>{@link CommPortal#CALLSTATE_FAILED}</li>
   *   <li>{@link CommPortal#CALLSTATE_FINAL}</li>
   * </ul>
   *
   * @type Integer
   */
  callstate.prototype.state = 0;
  /**
   * An English language explanation of the state (which will also be returned
   * if you call toString() on this object).
   *
   * @type String
   */
  callstate.prototype.message = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the properties that make up an error passed to a
   * {@link callbacks#failureCallback}.
   */
  function error() {}

  /**
   * A numeric value that identifies what error occured.
   * @type Integer
   */
  error.prototype.id = 0;
  /**
   * An English language explanation of the error (which will also be returned
   * if you call toString() on this object).
   *
   * @type String
   */
  error.prototype.message = 0;

  /**
   * <i>Ignore this constructor - this pseudo class just exists for documentation purposes.</i>
   * @class
   * Pseudo class documenting the properties that make up a incomingcalldata
   * passed to the {@link callbacks#incomingCallback} of a
   * {@link CommPortal#setIncomingCallHandler}.
   *
   * <p>New in version 7.1</p>
   */
  function incomingcalldata() {};

  /**
   * A unique identifier for this call
   * @type String
   */
  incomingcalldata.prototype.callID = 0;
  /**
   * A string value that identifies which state the call is in. (whether or not
   * the subscriber's phone is ringing)
   *
   * <p>
   * One of
   * </p>
   *
   * <ul>
   *   <li>{@link CommPortal#INCOMINGCALLSTATE_RINGING}</li>
   *   <li>{@link CommPortal#INCOMINGCALLSTATE_NOT_RINGING}</li>
   * </ul>
   *
   * @type String
   */
  incomingcalldata.prototype.callState = 0;
  /**
   * A string value containing the phone number that is receiving the call
   * @type String
   */
  incomingcalldata.prototype.number = 0;
  /**
   * A string value containing the caller phone number (available only when the
   * call state is {@link CommPortal#INCOMINGCALLSTATE_RINGING} and if the
   * number is known)
   * @type String
   */
  incomingcalldata.prototype.callerNumber = 0;
  /**
   * A string value that identifies the call type
   *
   * <p>
   * One of
   * </p>
   *
   * <ul>
   *   <li>{@link CommPortal#INCOMINGCALLTYPE_NORMAL}</li>
   *   <li>{@link CommPortal#INCOMINGCALLTYPE_LIVE_MESSAGE_SCREENING}</li>
   * </ul>
   *
   * @type String
   */
  incomingcalldata.prototype.callType = 0;

})();

/* Revision: $Rev$, $Date$ */
