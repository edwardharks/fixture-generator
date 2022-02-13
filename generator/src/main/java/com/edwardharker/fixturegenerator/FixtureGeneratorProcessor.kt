package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.writeTo

class FixtureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private val fixtureTypeGenerator = FixtureTypeGenerator()
    private var types = mutableListOf<Pair<PackageName, TypeSpec>>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Fixture::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(FixtureVisitor(), Unit) }

        return ret
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        for ((packageName, type) in types) {
            val fileSpec = FileSpec.builder(packageName, type.name!!)
                .addType(type)
                .build()
            fileSpec.writeTo(
                codeGenerator,
                fileSpec.kspDependencies(aggregating = false)
            )
        }
    }

    inner class FixtureVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            when (classDeclaration.classKind) {
                ClassKind.CLASS, ClassKind.ENUM_CLASS -> classDeclaration.primaryConstructor!!.accept(this, data)
                ClassKind.INTERFACE -> throw IllegalArgumentException("Interfaces cannot be annotated with @Fixture")
                ClassKind.ENUM_ENTRY -> TODO()
                ClassKind.OBJECT -> TODO()
                ClassKind.ANNOTATION_CLASS -> TODO()
            }
        }

        @OptIn(KotlinPoetKspPreview::class)
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val packageName = function.parentDeclaration!!.packageName.asString()
            types += packageName to fixtureTypeGenerator.generateFromConstructor(function)
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
            environment.options,
        )
    }
}

private typealias PackageName = String