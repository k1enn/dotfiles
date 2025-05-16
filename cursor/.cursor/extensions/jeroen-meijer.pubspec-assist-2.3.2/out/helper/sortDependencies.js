"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.sortDependencies = void 0;
const YAML = require("yaml");
const types_1 = require("yaml/types");
const dependencyType_1 = require("../model/dependencyType");
function sortDependencies({ pubspecString, useLegacySorting, }) {
    const options = {
        schema: "core",
    };
    const pubspecDoc = YAML.parseDocument(pubspecString, options);
    // A token used to identify dependencies that are unbounded
    // (i.e., have no version constraint), so they can easily be set
    // to an empty value later.
    const unboundedReplacementToken = "__UNBOUNDED_DEPENDENCY__";
    for (const dependencyType of dependencyType_1.dependencyTypes) {
        const dependencyPath = pubspecDoc.get(dependencyType);
        const dependencyPathIsMap = dependencyPath instanceof types_1.YAMLMap;
        if (dependencyPath === null ||
            dependencyPath === undefined ||
            !dependencyPathIsMap) {
            continue;
        }
        const sortByKey = (a, b) => a.key.value < b.key.value ? -1 : 1;
        const containsKey = (key) => (item) => !!item.value &&
            item.value.type === "MAP" &&
            item.value.items.some((item) => item.key.value === key);
        const setNullValuePairsToUnbounded = (item) => !!item.value
            ? item
            : new types_1.Pair(item.key, new types_1.Scalar(unboundedReplacementToken));
        const baseSortedItems = dependencyPath.items
            .sort(sortByKey)
            .map(setNullValuePairsToUnbounded);
        let sortedDependencies = baseSortedItems;
        if (useLegacySorting) {
            const sortedItemsByImportType = {
                sdk: baseSortedItems.filter(containsKey("sdk")),
                path: baseSortedItems.filter(containsKey("path")),
                git: baseSortedItems.filter(containsKey("git")),
                hosted: baseSortedItems.filter(containsKey("hosted")),
            };
            sortedDependencies = [
                ...sortedItemsByImportType.sdk,
                ...sortedItemsByImportType.path,
                ...sortedItemsByImportType.git,
                ...sortedItemsByImportType.hosted,
                ...baseSortedItems,
            ];
        }
        const newDependencyMap = new types_1.YAMLMap();
        for (const item of sortedDependencies) {
            if (!newDependencyMap.has(item.key)) {
                newDependencyMap.add(item);
            }
        }
        pubspecDoc.set(dependencyType, newDependencyMap);
    }
    const dirtyPubspecString = pubspecDoc.toString();
    const cleanPubspecString = dirtyPubspecString
        .replace(new RegExp(unboundedReplacementToken, "g"), "")
        .split("\n")
        .map((line) => line.trimEnd())
        .join("\n");
    return cleanPubspecString;
}
exports.sortDependencies = sortDependencies;
exports.default = sortDependencies;
//# sourceMappingURL=sortDependencies.js.map