@file:OptIn(KspExperimental::class, KotlinPoetKspPreview::class)

package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class FixtureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val fixtureTypeGenerator = FixtureTypeGenerator()
    private val fileToFunctions = mutableMapOf<FileSpec, MutableList<FunSpec>>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Fixture::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(FixtureVisitor(), Unit) }

        return ret
    }

    override fun finish() {
        for ((file, functions) in fileToFunctions) {
            val fileBuilder = file.toBuilder()
            for (function in functions) {
                fileBuilder.addFunction(function)
            }

            val fileSpec = fileBuilder.build()
            fileSpec.writeTo(
                codeGenerator,
                fileSpec.kspDependencies(aggregating = false)
            )
        }
    }

    private fun createFixture(classDeclaration: KSClassDeclaration) {
        val fixtureAnnotation = classDeclaration.getKSAnnotationByType(Fixture::class).first()
        val fixtureType = fixtureAnnotation.arguments.first { it.name?.asString() == "clazz" }.value as KSType
        val fixtureClass = fixtureType.declaration as KSClassDeclaration
        val fixtureFunction =
            when (fixtureClass.classKind) {
                ClassKind.INTERFACE -> throw IllegalArgumentException("Interfaces cannot be annotated with @Fixture")
                ClassKind.CLASS, ClassKind.ENUM_CLASS -> {
                    val constructor = fixtureClass.primaryConstructor
                    fixtureTypeGenerator.generateFromConstructor(constructor!!)
                }
                ClassKind.ENUM_ENTRY -> TODO()
                ClassKind.OBJECT -> fixtureTypeGenerator.generateFromObject(fixtureClass)
                ClassKind.ANNOTATION_CLASS -> TODO()
            }
                .toBuilder()
                .receiver(classDeclaration.asStarProjectedType().toTypeName())
                .build()
        val fileName = classDeclaration.containingFile?.fileName?.removeSuffix(".kt") + "Ext"
        val fileSpec = FileSpec.builder(classDeclaration.packageName.asString(), fileName).build()

        if (!fileToFunctions.containsKey(fileSpec)) {
            fileToFunctions[fileSpec] = mutableListOf()
        }
        fileToFunctions.getValue(fileSpec).add(fixtureFunction)
    }

    private fun <T : Annotation> KSAnnotated.getKSAnnotationByType(annotationKClass: KClass<T>): Sequence<KSAnnotation> {
        return this.annotations.filter {
            it.shortName.getShortName() == annotationKClass.simpleName && it.annotationType.resolve().declaration
                .qualifiedName?.asString() == annotationKClass.qualifiedName
        }
    }


    inner class FixtureVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            when (classDeclaration.classKind) {
                ClassKind.CLASS, ClassKind.ENUM_CLASS -> TODO()
                ClassKind.OBJECT -> createFixture(classDeclaration)
                ClassKind.INTERFACE -> TODO()
                ClassKind.ENUM_ENTRY -> TODO()
                ClassKind.ANNOTATION_CLASS -> TODO()
            }
        }
    }
}

class FixtureProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return FixtureProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }
}
