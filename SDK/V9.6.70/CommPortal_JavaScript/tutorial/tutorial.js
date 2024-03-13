/**
 * CommPortal SDK tutorial code
 *
  Copyright (c) Microsoft Corporation. All rights reserved.
  Highly Confidential Material
 */

addTableOfContents();
processCodeFragments();

/**
 * Add a table of contents, built from the headings in the text.
 */
function addTableOfContents()
{
  var toc = document.getElementById("tableOfContents");

  if (toc)
  {
    // We need to get the headers at various levels, so we actually examine all
    // the elements on the page
    var elements = document.getElementsByTagName("*");

    var entries = [];
    var afterToc = false;

    // The elements is a live node list, so we can't update it whilst we
    // are also trying to work through it
    for (var i = 0; i < elements.length; i++)
    {
      var element = elements[i];

      if (element == toc)
      {
        afterToc = true;
      }

      if (afterToc)
      {
        if (element.tagName.match(/H[1234]/))
        {
          entries.push(element);
        }
      }
    }

    var links = [];

    for (var i = 0; i < entries.length; i++)
    {
      var element = entries[i];

      var level = parseInt(element.tagName.substring(1), 10);
      var padding = "    ".substring(0, level - 2).replace(/ /g, "- &nbsp;");

      var id = "toc" + i;

      links.push('<a href="#' + id + '">' + padding + element.innerHTML +
                 '</a>');

      element.id = id;
    }

    toc.innerHTML = "<ul><li>" + links.join("</li><li>") + "</li>";
  }
}

/**
 * Take code fragments from the HTML page in the iframe and place them in
 * appropriately marked parts of the main document.
 */
function processCodeFragments()
{
  // We want to get code fragments from the file in the iframe
  var iframe = document.getElementById("includedCode");

  var codeDocument = iframe.contentDocument;

  if (!codeDocument)
  {
    // Also try the IE alternative location
    if (iframe.contentWindow)
    {
      codeDocument = iframe.contentWindow.document;
    }
  }

  if (codeDocument &&
      iframe.contentWindow &&
      iframe.contentWindow.document &&
      iframe.contentWindow.document.body &&
      "CommPortal" in iframe.contentWindow)
  {
    // Find the code elements of our own document
    var codeFragments = document.getElementsByTagName("code");
    var missedFragments = 0;
    var foundFragments = 0;

    for (var i = 0; i < codeFragments.length; i++)
    {
      var codeTag = codeFragments[i];

      // Does the code element have an id - if it doesn't then we leave its
      // content unchanged
      var fragmentId = codeTag.id;
      if (fragmentId)
      {
        if (fragmentId == "#all")
        {
          // This is a special case
          var fragmentText = makeInnerHtmlSafe(readFileAsString(iframe.src));

          // Actually, if the class is "function name" then we only want
          // a single function from the fragment
          if (codeTag.className.indexOf("function") == 0)
          {
            var start = fragmentText.indexOf(codeTag.className);

            var fragmentText = "function " + fragmentText.substring(start).split("function ")[1];

            // Remove any comments from the code
            fragmentText = fragmentText.replace(/\/\/.*$/gm, "");
          }
        }
        else
        {
          // Find the corresponding fragment in the code document
          var element = codeDocument.getElementById(fragmentId);

          if (element)
          {
            // Code that is marked as HTML needs to use the outerHTML, otherwise
            // we just take the innerHTML
            if (codeTag.className == "html")
            {
              if (element.outerHTML)
              {
                var fragmentText = element.outerHTML;

                // Unfortunately IE (which is the thing that implements outerHTML)
                // returns highly manipulated HTML - in particular it has stripped
                // much of the whitespace and converted tag names to upper case.
                // We attempt to undo the worst of these changes.

                // Return tag names to lower case
                fragmentText = fragmentText.replace(/<\/?[A-Z]+[\s1-3\>]/g,
                                                    function(t)
                                                    {
                                                      return t.toLowerCase();
                                                    });

                // Insert a newline after end tags
                fragmentText = fragmentText.replace(/<\/.+?>/g, "$&\n");

                fragmentText = makeInnerHtmlSafe(fragmentText);
              }
              else
              {
                // Firefox does not implement outerHTML.
                // To get the outerHTML of the element, we move it to a new parent,
                // get the innerHTML of that, then move it back again
                var parent = element.parentNode;
                var tempElement = codeDocument.createElement(parent.tagName);
                tempElement.appendChild(element);

                var fragmentText = "\n" + makeInnerHtmlSafe(tempElement.innerHTML);

                parent.appendChild(element);
              }
            }
            else
            {
              var fragmentText = makeInnerHtmlSafe(element.innerHTML);
            }

            foundFragments++;
          }
          else
          {
            var fragmentText = "CODE NOT FOUND for " + fragmentId;

            missedFragments++;
          }
        }

        // Manipulate the text so that it can be easily copied and pasted out
        // of the HTML page again
        codeFragments[i].innerHTML = makeWhitespaceExplicit(fragmentText);
      }
    }

    if (missedFragments && !foundFragments)
    {
      // None of the fragments were found, which indicates a timing problem
      setTimeout(processCodeFragments, 100);
    }
  }
  else
  {
    // No document in the iframe yet - queue up for another attempt later
    setTimeout(processCodeFragments, 100);
  }

  // Make whitespace explicit, since HTML treats consecutive whitespace
  // as equivalent to a single space character
  //  - replace initial whitespace with non breaking spaces
  //  - replace new lines with explicit breaks
  function makeWhitespaceExplicit(text)
  {
    return text.replace(/^ +/gm, function(t)
                                 {
                                   var r = "";
                                   for (var i = 0; i < t.length; i++)
                                   {
                                     r += "&nbsp;";
                                   }
                                   return r;
                                 }).
                replace(/\r?\n/g, "<br/>");
  }

  function makeInnerHtmlSafe(text)
  {
    // make sure that text is a string
    text = "" + text;

    // Replace & with &amp;, and replace < and > with entities
    return text.replace(/&/g, "&amp;").
                replace(/</g, "&lt;").
                replace(/>/g, "&gt;");
  }

  function readFileAsString(file)
  {
    var fileAsString = "";
    try
    {
      if (window.XMLHttpRequest)
      {
        // If IE7, Mozilla, Safari, etc: Use native object
        var request = new XMLHttpRequest();
      }
      else if (window.ActiveXObject)
      {
        // ...otherwise, use the ActiveX control for IE5.x and IE6
        // (or later IE where native XHR support has been explicitly disabled)
        request = new ActiveXObject("Microsoft.XMLHTTP");
      }
    }
    catch (exception)
    {
      request = null;
    }

    if (request)
    {
      // IE will give us onreadystatechange events, even for synchronous
      // requests, which is what we use
      request.onreadystatechange = handler;
      if (navigator.userAgent.indexOf("Firefox") > -1)
      {
        // Firefox does not call onreadystatechange for synchronous requests,
        // so we also add an onload handler to catch this case.
        request.onload = loaded;
      }

      try
      {
        // Get the file contents, synchronously (we must supply the third parameter
        // explicitly because it defaults to true, meaning async)
        request.open("GET", file, false);
      }
      catch (e)
      {
        alert("Fragment include failed - were you running from a file URL? You must use an HTTP url.");
      }

      // The W3C spec says that the parameter here is optional, but Firefox
      // always needs a parameter to be passed.
      // NB. If Firebug 1.0x is installed, and the "Net" tab enabled, it
      // intercepts XMLHttpRequests, and its intercept supplies a null parameter
      // anyway so you don't notice the problem.
      request.send(null);
    }

    return fileAsString;

    // Handle the file being fully loaded - this is only called for Firefox
    // (not IE).
    function loaded(event)
    {
      includeText(event.target.responseText);
    }

    // Handle the progress of the file loading
    function handler()
    {
      if (request.readyState == 4)
      {
        // The file has been completely loaded, and the contents are available
        // as a string
        includeText(request.responseText);
      }
    }

    function includeText(text)
    {
      fileAsString = text;
    }
  }
}
