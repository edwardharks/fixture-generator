package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

class FixtureTypeGenerator {

    fun generateFromObject(classDeclaration: KSClassDeclaration): TypeSpec {
        val classVisibility = requireNotNull(classDeclaration.getVisibility().toKModifier())
        validateClass(classVisibility)

        return buildTypeSpec(
            type = classDeclaration,
            classVisibility = classVisibility,
            functionVisibility = classVisibility,
            returnValue = classDeclaration.qualifiedName!!.asString(),
            returnType = classDeclaration.asStarProjectedType()
        )
    }

    fun generateFromConstructor(functionDeclaration: KSFunctionDeclaration): TypeSpec {
        val parentDeclaration = requireNotNull(functionDeclaration.parentDeclaration)
        val constructorVisibility = requireNotNull(functionDeclaration.getVisibility().toKModifier())
        val classVisibility = requireNotNull(parentDeclaration.getVisibility().toKModifier())
        validateConstructor(constructorVisibility)
        validateClass(classVisibility)

        val returnValue = when ((parentDeclaration as KSClassDeclaration).classKind) {
            ClassKind.CLASS -> buildConstructorCall(parentDeclaration, functionDeclaration)
            ClassKind.ENUM_CLASS -> getFirstEnumValue(parentDeclaration.asStarProjectedType())
            else -> throw  IllegalStateException("Unexpected class kind: $parentDeclaration")
        }

        return buildTypeSpec(
            type = parentDeclaration,
            classVisibility = classVisibility,
            functionVisibility = constructorVisibility,
            returnValue = returnValue,
            returnType = functionDeclaration.returnType!!.resolve()
        )
    }

    @OptIn(KotlinPoetKspPreview::class)
    fun generateEmptyFixtureType(
        typeName: String,
        containingFile: KSFile,
        classVisibility: KModifier
    ): TypeSpec {
        return TypeSpec.objectBuilder("${typeName}Fixtures")
            .addOriginatingKSFile(containingFile)
            .addModifiers(classVisibility)
            .build()
    }

    @OptIn(KotlinPoetKspPreview::class)
    private fun buildTypeSpec(
        type: KSDeclaration,
        classVisibility: KModifier,
        functionVisibility: KModifier,
        returnValue: String,
        returnType: KSType
    ): TypeSpec {
        return generateEmptyFixtureType(
            typeName = type.simpleName.getShortName(),
            containingFile = type.containingFile!!,
            classVisibility = classVisibility
        )
            .toBuilder()
            .addFunction(
                FunSpec.builder(buildFactoryMethodName(type))
                    .addModifiers(functionVisibility)
                    .addCode(CodeBlock.of("return $returnValue"))
                    .returns(returnType.toTypeName())
                    .build()
            )
            .build()
    }

    private fun buildConstructorCall(
        parentDeclaration: KSDeclaration,
        functionDeclaration: KSFunctionDeclaration
    ): String {
        return buildString {
            appendLine("${parentDeclaration.qualifiedName!!.asString()}(")
            for (parameter in functionDeclaration.parameters) {
                val memberValue = formatValue(parameter)
                appendLine("    ${parameter.name?.asString()} = $memberValue,")
            }
            appendLine(")")
        }
    }

    private fun formatValue(parameter: KSValueParameter): String {
        val type = parameter.type.resolve()
        val declaration = type.declaration
        val nullable = type.isMarkedNullable
        val typeName = declaration.qualifiedName?.asString()
        val isFixture = isFixture(type)
        return when {
            nullable -> "null"
            typeName == "kotlin.Int" -> "0"
            typeName == "kotlin.Float" -> "0.0F"
            typeName == "kotlin.Double" -> "0.0"
            typeName == "kotlin.Short" -> "0"
            typeName == "kotlin.Long" -> "0L"
            typeName == "kotlin.Byte" -> "0"
            typeName == "kotlin.Boolean" -> "false"
            typeName == "kotlin.String" -> "\"\""
            typeName == "kotlin.Char" -> "'\\u0000'"
            typeName == "kotlin.collections.List" -> "emptyList()"
            typeName == "kotlin.collections.Map" -> "emptyMap()"
            typeName == "kotlin.collections.Set" -> "emptySet()"
            typeName == "kotlin.Array" -> "emptyArray()"
            typeName == "kotlin.IntArray" -> "intArrayOf()"
            typeName == "kotlin.DoubleArray" -> "doubleArrayOf()"
            typeName == "kotlin.FloatArray" -> "floatArrayOf()"
            typeName == "kotlin.ShortArray" -> "shortArrayOf()"
            typeName == "kotlin.LongArray" -> "longArrayOf()"
            typeName == "kotlin.ByteArray" -> "byteArrayOf()"
            typeName == "kotlin.BooleanArray" -> "booleanArrayOf()"
            typeName == "kotlin.CharArray" -> "charArrayOf()"
            isFixture -> buildFactoryFunctionCall(type)
            else -> throw IllegalArgumentException("Unknown property type: $type. Classes must be annotated with @Fixture")
        }
    }

    private fun getFirstEnumValue(type: KSType): String {
        val enumValue = (type.declaration as KSClassDeclaration).declarations
            .filterNot { it is KSFunctionDeclaration }
            .firstOrNull()
        return enumValue?.qualifiedName?.asString()
            ?: throw IllegalArgumentException("Enum has no values")
    }

    private fun buildFactoryFunctionCall(type: KSType): String {
        val qualifiedName = type.declaration.qualifiedName
        return "${qualifiedName?.getQualifier()}.${qualifiedName?.getShortName()}Fixtures" +
                ".${buildFactoryMethodName(type.declaration)}()"
    }

    private fun buildFactoryMethodName(type: KSDeclaration) =
        type.simpleName.getShortName().replaceFirstChar { it.lowercaseChar() }

    private fun isFixture(type: KSType): Boolean {
        val annotations = type.declaration.annotations
        return annotations.any { annotation ->
            annotation.annotationType.resolve()
                .declaration.qualifiedName?.asString() == Fixture::class.qualifiedName
        }
    }

    private fun validateClass(classVisibility: KModifier) {
        if (classVisibility == KModifier.PRIVATE) {
            throw IllegalArgumentException("Cannot create fixtures for private classes")
        }
    }

    private fun validateConstructor(constructorVisibility: KModifier) {
        if (constructorVisibility == KModifier.PRIVATE) {
            throw IllegalArgumentException("Cannot create fixtures for private primary constructors")
        }
    }
}