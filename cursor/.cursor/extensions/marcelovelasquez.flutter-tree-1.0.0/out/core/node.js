"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class Node {
    constructor(name, children) {
        this.name = name[0].toUpperCase() + name.slice(1);
        this.children = children;
    }
    isUnique() {
        return (this.children instanceof Node) ? true : false;
    }
}
exports.Node = Node;
//# sourceMappingURL=node.js.map