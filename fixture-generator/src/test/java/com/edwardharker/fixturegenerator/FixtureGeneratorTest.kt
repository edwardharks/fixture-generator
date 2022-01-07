package com.edwardharker.fixturegenerator

import com.edwardharker.fixturegenerator.ksp.*
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Modifier.PRIVATE
import com.google.devtools.ksp.symbol.Modifier.PUBLIC
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class FixtureGeneratorTest {

    private val fixtureGenerator = FixtureGenerator(FakeKSPLogger())

    @Test(expected = IllegalArgumentException::class)
    fun `throws when constructor is private`() {
        val constructorDeclaration = FakeKSFunctionDeclaration(
            modifiers = setOf(PRIVATE),
        )

        fixtureGenerator.generateFrom(constructorDeclaration)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when class is private`() {
        val constructorDeclaration = FakeKSFunctionDeclaration(
            parentDeclaration = FakeKSDeclaration(
                modifiers = setOf(PRIVATE),
            ),
            modifiers = setOf(PUBLIC)
        )

        fixtureGenerator.generateFrom(constructorDeclaration)
    }
}
