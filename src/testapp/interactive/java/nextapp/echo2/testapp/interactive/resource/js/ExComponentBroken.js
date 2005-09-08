/**
 * Client JavaScript module that will (deliberately) fail when the ClientEngine
 * attempts to load it.  No object "ExComponentBroken" has been defined, and 
 * thus attempting to set a property upon it will result in an exception.
 */
ExComponentBroken.thisWillBreak = function() { };