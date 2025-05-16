"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getRestApiError = exports.PubApiSearchError = exports.PubApiNotRespondingError = exports.PubError = exports.OtherSearchInfo = exports.PackageSearchInfo = exports.PageSearchInfo = exports.QuerySearchInfo = void 0;
var SearchType;
(function (SearchType) {
    SearchType["PACKAGE"] = "PACKAGE";
    SearchType["QUERY"] = "QUERY";
    SearchType["PAGE"] = "PAGE";
    SearchType["OTHER"] = "OTHER";
})(SearchType || (SearchType = {}));
const QuerySearchInfo = (query) => {
    return {
        searchType: SearchType.QUERY,
        query,
        details: `Query: ${query}`,
    };
};
exports.QuerySearchInfo = QuerySearchInfo;
const PageSearchInfo = (pageNumber) => {
    return {
        searchType: SearchType.PAGE,
        pageNumber,
        details: `Page number: ${pageNumber}`,
    };
};
exports.PageSearchInfo = PageSearchInfo;
const PackageSearchInfo = (name) => {
    return {
        searchType: SearchType.PACKAGE,
        name,
        details: `Package name: ${name}`,
    };
};
exports.PackageSearchInfo = PackageSearchInfo;
const OtherSearchInfo = (info) => {
    return {
        searchType: SearchType.OTHER,
        info,
        details: `Search info: ${info}`,
    };
};
exports.OtherSearchInfo = OtherSearchInfo;
class PubError extends Error {
    constructor(message) {
        super(message);
        Object.setPrototypeOf(this, PubError.prototype);
    }
}
exports.PubError = PubError;
class PubApiNotRespondingError extends PubError {
    constructor() {
        super("The Pub API is not responding.\nPlease check your internet connection or try again.");
        Object.setPrototypeOf(this, PubApiNotRespondingError.prototype);
    }
}
exports.PubApiNotRespondingError = PubApiNotRespondingError;
class PubApiSearchError extends PubError {
    constructor(searchInfo) {
        let message = `
    No response from Pub API call.\n
    Search type: "${searchInfo.searchType.toString()}".\n
    Details: "${searchInfo.details}".`;
        super(message);
        Object.setPrototypeOf(this, PubApiSearchError.prototype);
    }
}
exports.PubApiSearchError = PubApiSearchError;
function getRestApiError(error) {
    if (error instanceof Error &&
        ["ENOTFOUND", "ETIMEDOUT"].some((errorDescription) => error.message.includes(errorDescription))) {
        return new PubApiNotRespondingError();
    }
    else {
        return new PubError(`Rest client error: ${error}`);
    }
}
exports.getRestApiError = getRestApiError;
//# sourceMappingURL=pubError.js.map