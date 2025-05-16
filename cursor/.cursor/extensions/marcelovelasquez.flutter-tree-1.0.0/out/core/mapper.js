"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const node_1 = require("./node");
const constants_1 = require("./constants");
// Abbreviation to Node tree
function mapperSingleLine(abbr, options) {
    return getChildren(abbr);
}
exports.mapperSingleLine = mapperSingleLine;
function mapperMultiLine(selection) {
    return new node_1.Node('');
}
exports.mapperMultiLine = mapperMultiLine;
function getChildren(abbr) {
    if (isSingleChild(abbr)) {
        return getSingleChild(abbr);
    }
    else {
        return getMultiChild(abbr);
    }
}
function getSingleChild(abbr) {
    let name;
    let node;
    let position = abbr.indexOf(constants_1.symbols.childBuilder);
    if (position === -1) {
        name = abbr.substring(0, abbr.length);
        node = new node_1.Node(name);
    }
    else {
        name = abbr.substring(0, position);
        let child = getChildren(abbr.substring(position + 1, abbr.length));
        node = new node_1.Node(name, child);
    }
    return node;
}
function getMultiChild(abbr) {
    let position = abbr.indexOf(constants_1.symbols.childrenBuilder);
    let name = abbr.substring(0, position);
    let abbrWithoutName = abbr.substring(position + 1, abbr.length - 1);
    let abbrArray = split(abbrWithoutName);
    let children = new Array();
    abbrArray.forEach(abbr => {
        let child = abbr || '';
        children.push(getChildren(child));
    });
    let node = new node_1.Node(name, children);
    return node;
}
function split(abbr) {
    let results = Array();
    let str = '';
    let left = 0, right = 0;
    function keepResult() {
        results.push(str);
        str = '';
    }
    for (let i = 0; i < abbr.length; i++) {
        switch (abbr[i]) {
            case ',':
                if ((left === right)) {
                    keepResult();
                    left = right = 0;
                }
                else {
                    str += abbr[i];
                }
                break;
            case '[':
                left++;
                str += abbr[i];
                break;
            case ']':
                right++;
                str += abbr[i];
                break;
            default:
                str += abbr[i];
        }
    }
    keepResult();
    return results;
}
function isSingleChild(abbr) {
    let symbolChild = abbr.indexOf(constants_1.symbols.childBuilder);
    let symbolChildren = abbr.indexOf(constants_1.symbols.childrenBuilder);
    if (symbolChild === -1 && symbolChildren === -1) {
        return true;
    }
    if (symbolChild === -1) {
        return false;
    }
    if (symbolChildren === -1) {
        return true;
    }
    return symbolChild < symbolChildren ? true : false;
}
//# sourceMappingURL=mapper.js.map