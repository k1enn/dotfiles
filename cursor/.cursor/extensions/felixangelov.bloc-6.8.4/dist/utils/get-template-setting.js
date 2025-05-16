"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getTemplateSetting = void 0;
const vscode_1 = require("vscode");
function getTemplateSetting(type) {
    let config;
    switch (type) {
        case 0 /* Bloc */:
            config = vscode_1.workspace.getConfiguration("bloc").get("newBlocTemplate.type");
            break;
        case 1 /* Cubit */:
            config = vscode_1.workspace.getConfiguration("bloc").get("newCubitTemplate.type");
            break;
        default:
            return 0 /* Auto */;
    }
    switch (config) {
        case "freezed":
            return 2 /* Freezed */;
        case "equatable":
            return 1 /* Equatable */;
        case "simple":
            return 3 /* Simple */;
        case "auto":
        default:
            return 0 /* Auto */;
    }
}
exports.getTemplateSetting = getTemplateSetting;
//# sourceMappingURL=get-template-setting.js.map