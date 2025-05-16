"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const node_1 = require("./node");
const constants_1 = require("./constants");
function generateSnippet(rootNode) {
    let snippet = map(rootNode);
    let count = 1;
    for (let i = 0; i < snippet.length; i++) {
        const char = snippet[i];
        if (char === constants_1.flag) {
            snippet = snippet.replace(char, `$${count}`);
            count++;
        }
    }
    return snippet.replace(constants_1.flag, '$1');
}
exports.default = generateSnippet;
function map(rootNode, tab = 1) {
    let result = `${rootNode.name}(${constants_1.flag}),`;
    if (rootNode.children) {
        if (rootNode.children instanceof node_1.Node) {
            let child = map(rootNode.children, tab + 1);
            let text = `\n${tabs(tab)}child: ${child}\n${tabs(tab - 1)}`;
            result = splice(result, text);
        }
        else if (rootNode.children) {
            let children = '';
            tab += 2;
            rootNode.children.forEach((child, index) => {
                if (index !== 0) {
                    children += '\n' + tabs(tab - 1);
                }
                children += map(child, tab);
            });
            tab -= 2;
            let text = `\n${tabs(tab)}children: <Widget>[\n${tabs(tab + 1)}${children}\n${tabs(tab)}],\n${tabs(tab - 1)}`;
            result = splice(result, text);
        }
    }
    return result;
}
function tabs(number) {
    let string = '';
    for (let i = 0; i < number; i++) {
        string += '\t';
    }
    return string;
}
function splice(str, add) {
    let index = str.indexOf(constants_1.flag);
    return str.slice(0, index) + add + str.slice(index + 1);
}
//# sourceMappingURL=map.js.map