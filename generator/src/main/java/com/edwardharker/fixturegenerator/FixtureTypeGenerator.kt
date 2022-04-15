@file:OptIn(KotlinPoetKspPreview::class)

package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

class FixtureTypeGenerator {

    fun generateFromObject(classDeclaration: KSClassDeclaration): FunSpec {
        val classVisibility = requireNotNull(classDeclaration.getVisibility().toKModifier())
        validateClass(classVisibility)

        return FunSpec.builder(buildFactoryMethodName(classDeclaration))
            .addModifiers(classVisibility)
            .addCode(CodeBlock.of("return ${classDeclaration.qualifiedName!!.asString()}"))
            .returns(classDeclaration.asStarProjectedType().toTypeName())
            .build()
    }

    fun generateFromConstructor(functionDeclaration: KSFunctionDeclaration): FunSpec {
        val parentDeclaration = requireNotNull(functionDeclaration.parentDeclaration)
        val constructorVisibility = requireNotNull(functionDeclaration.getVisibility().toKModifier())
        val classVisibility = requireNotNull(parentDeclaration.getVisibility().toKModifier())
        validateConstructor(constructorVisibility)
        validateClass(classVisibility)

        val classKind = (parentDeclaration as KSClassDeclaration).classKind
        val starProjectedType = parentDeclaration.asStarProjectedType()
        val returnValue = when {
            parentDeclaration.modifiers.contains(Modifier.SEALED) -> getFirstSealedSubClass(starProjectedType)
            classKind == ClassKind.CLASS -> buildConstructorCall(parentDeclaration, functionDeclaration)
            classKind == ClassKind.ENUM_CLASS -> getFirstEnumValue(starProjectedType)
            else -> throw  IllegalStateException("Unexpected class kind: $parentDeclaration")
        }

        return FunSpec.builder(buildFactoryMethodName(parentDeclaration))
            .addModifiers(constructorVisibility)
            .addCode(CodeBlock.of("return $returnValue"))
            .returns(functionDeclaration.returnType!!.resolve().toTypeName())
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

    private fun getFirstSealedSubClass(type: KSType): String {
        return (type.declaration as KSClassDeclaration).getSealedSubclasses()
            .filter { it.annotations.any { annotation -> annotation.shortName.asString() == Fixture::class.simpleName } }
            .firstOrNull()
            ?.let { sealedSubClass ->
                buildFactoryFunctionCall((sealedSubClass).asStarProjectedType())
            }
            ?: throw IllegalArgumentException("Sealed class has no subclasses annotated with @Fixture")
    }

    private fun buildFactoryFunctionCall(type: KSType): String {
        val declaration = type.declaration
        val functionCall = StringBuilder("${buildFactoryMethodName(declaration)}()")
        functionCall.insert(0, "${declaration.simpleName.asString()}Fixtures.")
        var parent = declaration.parent
        while (parent != null && parent is KSDeclaration) {
            functionCall.insert(0, "${parent.simpleName.asString()}Fixtures.")
            parent = parent.parent
        }
        functionCall.insert(0, "${declaration.packageName.asString()}.")
        return functionCall.toString()
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