/**
 * Static object/namespace for ExComponent.
 * This object/namespace should not be used externally.
 */
ExComponent = function() { };

/**
 * Static object/namespace for ExComponent MessageProcessor 
 * implementation.
 */
ExComponent.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
ExComponent.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "fail":
                throw "ExComponent: Deliberate JavaScript Exception";
                break;
            }
        }
    }
};