"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getFreezedBlocTemplate = exports.getBlocTemplate = void 0;
const changeCase = require("change-case");
function getBlocTemplate(blocName, type) {
    switch (type) {
        case 2 /* Freezed */:
            return getFreezedBlocTemplate(blocName);
        case 1 /* Equatable */:
            return getEquatableBlocTemplate(blocName);
        default:
            return getDefaultBlocTemplate(blocName);
    }
}
exports.getBlocTemplate = getBlocTemplate;
function getEquatableBlocTemplate(blocName) {
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const blocState = `${pascalCaseBlocName}State`;
    const blocEvent = `${pascalCaseBlocName}Event`;
    return `import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part '${snakeCaseBlocName}_event.dart';
part '${snakeCaseBlocName}_state.dart';

class ${pascalCaseBlocName}Bloc extends Bloc<${blocEvent}, ${blocState}> {
  ${pascalCaseBlocName}Bloc() : super(${pascalCaseBlocName}Initial()) {
    on<${blocEvent}>((event, emit) {
      // TODO: implement event handler
    });
  }
}
`;
}
function getDefaultBlocTemplate(blocName) {
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const blocState = `${pascalCaseBlocName}State`;
    const blocEvent = `${pascalCaseBlocName}Event`;
    return `import 'package:bloc/bloc.dart';
import 'package:meta/meta.dart';

part '${snakeCaseBlocName}_event.dart';
part '${snakeCaseBlocName}_state.dart';

class ${pascalCaseBlocName}Bloc extends Bloc<${blocEvent}, ${blocState}> {
  ${pascalCaseBlocName}Bloc() : super(${pascalCaseBlocName}Initial()) {
    on<${blocEvent}>((event, emit) {
      // TODO: implement event handler
    });
  }
}
`;
}
function getFreezedBlocTemplate(blocName) {
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const blocState = `${pascalCaseBlocName}State`;
    const blocEvent = `${pascalCaseBlocName}Event`;
    return `import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part '${snakeCaseBlocName}_event.dart';
part '${snakeCaseBlocName}_state.dart';
part '${snakeCaseBlocName}_bloc.freezed.dart';

class ${pascalCaseBlocName}Bloc extends Bloc<${blocEvent}, ${blocState}> {
  ${pascalCaseBlocName}Bloc() : super(_Initial()) {
    on<${blocEvent}>((event, emit) {
      // TODO: implement event handler
    });
  }
}
`;
}
exports.getFreezedBlocTemplate = getFreezedBlocTemplate;
//# sourceMappingURL=bloc.template.js.map