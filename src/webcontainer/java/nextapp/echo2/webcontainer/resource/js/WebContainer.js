EchoCoreProperties = new function() { };

EchoCoreProperties.FillImageBorder = function(color, borderInsets, contentInsets, fillImages) {
    this.borderInsets = borderInsets;
    this.contentInsets = contentInsets;
    this.color = color;
    if (fillImages) {
        if (fillImages.length != 8) {
            throw new Error("Image array must contain eight images.");
        }
        this.fillImages = fillImages;
    } else {
        this.fillImages = new Array(8);
    }
};

EchoCoreProperties.FillImageBorder.IMAGE_TL = 0;
EchoCoreProperties.FillImageBorder.IMAGE_T = 1;
EchoCoreProperties.FillImageBorder.IMAGE_TR = 2;
EchoCoreProperties.FillImageBorder.IMAGE_L = 3;
EchoCoreProperties.FillImageBorder.IMAGE_R = 4;
EchoCoreProperties.FillImageBorder.IMAGE_BL = 5;
EchoCoreProperties.FillImageBorder.IMAGE_B = 6;
EchoCoreProperties.FillImageBorder.IMAGE_BR = 7;

EchoCoreProperties.Insets = function() {
    this.top = 0;
    this.right = 0;
    this.bottom = 0;
    this.left = 0;
    
    if (arguments.length == 1) {
        this.loadValuesFromString(arguments[0]);
    } else if (arguments.length == 2) {
        this.top = this.bottom = arguments[0];
        this.right = this.left = arguments[1];
    } else if (arguments.length == 4) {
        this.top = arguments[0];
        this.right = arguments[1];
        this.bottom = arguments[2];
        this.left = arguments[3];
    }
};

EchoCoreProperties.Insets.prototype.loadValuesFromString = function(insetsString) {
    insetsString = new String(insetsString);
    var elements = insetsString.split(" ");
    switch (elements.length) {
    case 1:
        this.top = this.left = this.right = this.bottom = parseInt(elements[0]);
        break;
    case 2:
        this.top = this.bottom = parseInt(elements[0]);
        this.right = this.left = parseInt(elements[1]);
        break;
    case 3:
        this.top = parseInt(elements[0]);
        this.right = this.left = parseInt(elements[1]);
        this.bottom = parseInt(elements[2]);
        break;
    case 4:
        this.top = parseInt(elements[0]);
        this.right = parseInt(elements[1]);
        this.bottom = parseInt(elements[2]);
        this.left = parseInt(elements[3]);
        break;
    default:
        throw "Illegal inset value: " + insetsString;
    }
};

EchoCoreProperties.Insets.prototype.toString = function(insetsString) {
    if (this.top == this.bottom && this.right == this.left) {
        if (this.top == this.right) {
            return this.top + "px"
        } else {
            return this.top + "px " + this.right + "px";
        }
    } else {
        return this.top + "px " + this.right + "px " + this.bottom + "px " + this.left + "px";
    }
};
