"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const vscode = require("vscode");
const core_1 = require("./core");
class CompletionProvider {
    provideCompletionItems(document, position, token, context) {
        let expandedAbbr = getExpandedAbbreviation(document, position);
        let completionItems = expandedAbbr ? [expandedAbbr] : [];
        return Promise.resolve(new vscode.CompletionList(completionItems, true));
    }
}
exports.CompletionProvider = CompletionProvider;
function extractAbbreviation(document, position) {
    let lineText = document.lineAt(position.line).text;
    let lineSplit = lineText.split(' ');
    let abbreviation = lineSplit[lineSplit.length - 1];
    let start = new vscode.Position(position.line, lineText.length - abbreviation.length);
    let end = new vscode.Position(position.line, lineText.length);
    return [new vscode.Range(start, end), abbreviation];
}
function getExpandedAbbreviation(document, position) {
    let [rangeToReplace, wordToExpand] = extractAbbreviation(document, position);
    let valid = core_1.validate(wordToExpand);
    let completionItem = new vscode.CompletionItem(wordToExpand);
    completionItem.detail = 'Flutter Tree';
    if (valid) {
        let expandedWord = core_1.expand(wordToExpand);
        completionItem.insertText = new vscode.SnippetString(expandedWord);
        completionItem.documentation = removeTabStops(expandedWord);
        completionItem.range = rangeToReplace;
    }
    else {
        completionItem.documentation = 'Invalid syntax';
        completionItem.insertText = new vscode.SnippetString();
    }
    return completionItem;
}
function removeTabStops(expandedWord) {
    return expandedWord.replace(/[(]+[$]+[0-9]+[)]/g, '()');
}
//# sourceMappingURL=completion.js.map