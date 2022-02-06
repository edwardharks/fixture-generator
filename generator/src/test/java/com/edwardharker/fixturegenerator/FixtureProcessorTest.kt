package com.edwardharker.fixturegenerator

import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FixtureProcessorTest {
    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `generates file`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass()
                """.trimIndent()
            )
        )

        val result = compilation.compile()
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val generatedFileText = File(compilation.kspSourcesDir, "kotlin/test/ExampleClassFixtures.kt")
            .readText()
        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
  )
}

        """.trimIndent()
        )
    }

    private fun StringSubject.isEqualToKotlin(@Language("kotlin") expected: String) = isEqualTo(expected)

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                inheritClassPath = true
                symbolProcessorProviders = listOf(FixtureProcessorProvider())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = true
            }
    }
}