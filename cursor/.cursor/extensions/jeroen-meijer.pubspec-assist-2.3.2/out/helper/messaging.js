"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.handleCriticalError = exports.showRetryableError = exports.showError = exports.showInfo = void 0;
const vscode = require("vscode");
const web_1 = require("./web");
var ErrorOptionType;
(function (ErrorOptionType) {
    ErrorOptionType["report"] = "Report issue";
    ErrorOptionType["ignore"] = "Ignore";
    ErrorOptionType["tryAgain"] = "Try Again";
    ErrorOptionType["close"] = "Close";
})(ErrorOptionType || (ErrorOptionType = {}));
let criticalErrorOptions = [
    { title: ErrorOptionType.report },
    { title: ErrorOptionType.ignore },
];
let retryableErrorOptions = [
    { title: ErrorOptionType.tryAgain },
    { title: ErrorOptionType.close },
];
function showInfo(message) {
    return vscode.window.showInformationMessage(message);
}
exports.showInfo = showInfo;
function showError(error, isCritical = false) {
    if (!isCritical) {
        vscode.window.showErrorMessage(error.message);
    }
    else {
        vscode.window
            .showErrorMessage(`A critical error has occurred.\n
      If this happens again, please report it.\n\n

      Error message: ${error.message}`, {}, ...criticalErrorOptions)
            .then((option) => {
            if (option) {
                handleErrorOptionResponse(option.title, error);
            }
        });
    }
}
exports.showError = showError;
function showRetryableError(error) {
    return __awaiter(this, void 0, void 0, function* () {
        const response = yield vscode.window.showWarningMessage(`An error has occurred:\n${error.message}`, {}, ...retryableErrorOptions);
        return !!response && response.title === ErrorOptionType.tryAgain;
    });
}
exports.showRetryableError = showRetryableError;
function handleErrorOptionResponse(option, error) {
    switch (option) {
        case ErrorOptionType.report:
            (0, web_1.openNewGitIssueUrl)(error);
            break;
        default:
            break;
    }
}
function handleCriticalError(error) {
    if (error instanceof Error) {
        if (error.message.includes("Document with errors cannot be stringified")) {
            showError(new Error("Your pubspec YAML file is invalid or contains errors. " +
                "Please fix them and try again."));
        }
        else {
            showError(error, true);
        }
    }
    else {
        showError(new Error(`Unknown error: ${error}`), true);
    }
}
exports.handleCriticalError = handleCriticalError;
//# sourceMappingURL=messaging.js.map