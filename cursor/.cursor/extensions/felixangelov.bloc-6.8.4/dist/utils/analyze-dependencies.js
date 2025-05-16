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
exports.analyzeDependencies = void 0;
const _ = require("lodash");
const semver = require("semver");
const vscode_1 = require("vscode");
const _1 = require(".");
const update_pubspec_dependency_1 = require("./update-pubspec-dependency");
const DEFAULT_VERSION_VALUE = "0.0.0";
const openBlocMigrationGuide = {
    name: "Open Migration Guide",
    callback: () => {
        vscode_1.env.openExternal(vscode_1.Uri.parse("https://bloclibrary.dev/migration"));
    },
};
const openEquatableMigrationGuide = {
    name: "Open Migration Guide",
    callback: () => {
        vscode_1.env.openExternal(vscode_1.Uri.parse("https://github.com/felangel/equatable/blob/master/doc/migration_guides/migration-0.6.0.md"));
    },
};
const deps = [
    { name: "angular_bloc", actions: [openBlocMigrationGuide] },
    { name: "bloc", actions: [openBlocMigrationGuide] },
    { name: "bloc_concurrency", actions: [openBlocMigrationGuide] },
    { name: "equatable", actions: [openEquatableMigrationGuide] },
    { name: "flutter_bloc", actions: [openBlocMigrationGuide] },
    { name: "hydrated_bloc", actions: [openBlocMigrationGuide] },
    { name: "replay_bloc", actions: [openBlocMigrationGuide] },
    { name: "sealed_flutter_bloc", actions: [openBlocMigrationGuide] },
];
const devDeps = [{ name: "bloc_test", actions: [openBlocMigrationGuide] }];
function analyzeDependencies() {
    return __awaiter(this, void 0, void 0, function* () {
        const dependencies = yield getDependencies(deps);
        const devDependencies = yield getDependencies(devDeps);
        const pubspecLock = yield _1.getPubspecLock();
        const pubspecLockDependencies = _.get(pubspecLock, "packages", {});
        checkForUpgrades(dependencies, pubspecLockDependencies);
        checkForUpgrades(devDependencies, pubspecLockDependencies);
    });
}
exports.analyzeDependencies = analyzeDependencies;
function checkForUpgrades(dependencies, pubspecDependencies) {
    for (let i = 0; i < dependencies.length; i++) {
        const dependency = dependencies[i];
        if (_.isEmpty(dependency.version))
            continue;
        if (_.has(pubspecDependencies, dependency.name)) {
            const currentDependencyVersion = _.get(pubspecDependencies, dependency.name).version;
            const hasLatestVersion = currentDependencyVersion === dependency.version;
            if (hasLatestVersion)
                continue;
            showUpdateMessage(dependency, currentDependencyVersion);
        }
    }
}
function showUpdateMessage(dependency, dependencyVersion) {
    const minVersion = _.get(semver.minVersion(dependencyVersion), "version", DEFAULT_VERSION_VALUE);
    if (!semver.satisfies(minVersion, dependency.version)) {
        vscode_1.window
            .showWarningMessage(`This workspace contains an outdated version of ${dependency.name}. Please update to ${dependency.version}.`, ...dependency.actions.map((action) => action.name).concat("Update"))
            .then((invokedAction) => {
            if (invokedAction === "Update") {
                return update_pubspec_dependency_1.updatePubspecDependency({
                    name: dependency.name,
                    latestVersion: `^${dependency.version}`,
                    currentVersion: dependencyVersion,
                });
            }
            const action = dependency.actions.find((action) => action.name === invokedAction);
            if (!_.isNil(action)) {
                action.callback();
            }
        });
    }
}
function getDependencies(dependencies) {
    return __awaiter(this, void 0, void 0, function* () {
        const futures = dependencies.map((dependency) => __awaiter(this, void 0, void 0, function* () {
            return {
                name: dependency.name,
                actions: dependency.actions,
                version: yield _1.getLatestPackageVersion(dependency.name),
            };
        }));
        return Promise.all(futures);
    });
}
//# sourceMappingURL=analyze-dependencies.js.map