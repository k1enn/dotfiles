"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const map_1 = require("./map");
const mapper_1 = require("./mapper");
const constants_1 = require("./constants");
function isAbbr(text) {
    return !text.includes(constants_1.symbols.enter);
}
function expand(text) {
    text = filter(text);
    return map_1.default(isAbbr(text) ? mapper_1.mapperSingleLine(text) : mapper_1.mapperMultiLine(text));
}
exports.expand = expand;
function validate(text) {
    return isAbbr(text) ? validateSingleLine(text) : validateMultiLine(text);
}
exports.validate = validate;
function validateSingleLine(abbr) {
    let left = 0, right = 0;
    for (let i = 0; i < abbr.length; i++) {
        switch (abbr[i]) {
            case constants_1.symbols.childrenBuilder:
                left++;
                break;
            case constants_1.symbols.childrenEnd:
                right++;
                break;
            case constants_1.symbols.childrenSeparator:
                if (left === 0) {
                    return false;
                }
                break;
            case constants_1.symbols.childBuilder:
                break;
            default:
                if (abbr[i].match(/\W/i)) {
                    return false;
                }
                break;
        }
    }
    if (left !== right) {
        return false;
    }
    if (abbr === '' || abbr.match(/(>|\[|,){2}|>]|]\[|]>/g)) {
        return false;
    }
    return true;
}
function validateMultiLine(abbr) {
    return true;
}
function filter(text) {
    text = text.trim();
    if (text.includes(constants_1.symbols.childrenBuilder)) {
        return limitAbbr(text);
    }
    else {
        return removeEndTriggers(text);
    }
}
function removeEndTriggers(text) {
    text = text.replace(/\,\]/g, constants_1.symbols.childrenEnd);
    return constants_1.triggers.some(e => text.endsWith(e))
        ? removeEndTriggers(text.slice(0, -1))
        : text;
}
function limitAbbr(abbr) {
    let text = '', block = 0, flag = false;
    let index = Array.from(abbr).findIndex((char, pos) => {
        text += char;
        switch (char) {
            case constants_1.symbols.childrenBuilder:
                flag = true;
                block++;
                break;
            case constants_1.symbols.childrenEnd:
                block--;
                break;
        }
        if (!block && flag) {
            return pos;
        }
    });
    return text.slice(0, index + 1);
}
//# sourceMappingURL=index.js.map