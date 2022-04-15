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

class ExampleClass

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
)

        """.trimIndent()
        )
    }

    @Test
    fun `nullable types are null`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass(val nullable: String?)

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
    nullable = null,
)

        """.trimIndent()
        )
    }

    @Test
    fun `primitive types are default`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass(
    val string: String,
    val char: Char,
    val int: Int,
    val float: Float,
    val double: Double,
    val short: Short,
    val long: Long,
    val byte: Byte,
    val boolean: Boolean,
)

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
    string = "",
    char = '\u0000',
    int = 0,
    float = 0.0F,
    double = 0.0,
    short = 0,
    long = 0L,
    byte = 0,
    boolean = false,
)

        """.trimIndent()
        )
    }

    @Test
    fun `collection types are empty`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass(
    val list: List<String>,
    val map: Map<String, String>,
    val set: Set<String>,
)

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
    list = emptyList(),
    map = emptyMap(),
    set = emptySet(),
)

        """.trimIndent()
        )
    }

    @Test
    fun `array types are empty`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass(
    val array: Array<String>,
    val intArray: IntArray,
    val charArray: CharArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,
    val shortArray: ShortArray,
    val longArray: LongArray,
    val byteArray: ByteArray,
    val booleanArray: BooleanArray,
)

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
    array = emptyArray(),
    intArray = intArrayOf(),
    charArray = charArrayOf(),
    floatArray = floatArrayOf(),
    doubleArray = doubleArrayOf(),
    shortArray = shortArrayOf(),
    longArray = longArrayOf(),
    byteArray = byteArrayOf(),
    booleanArray = booleanArrayOf(),
)

        """.trimIndent()
        )
    }

    @Test
    fun `fixture types call fixture`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass(
    val fixture: AnotherExample
)

class AnotherExample

@Fixture(ExampleClass::class)
object ExampleClassFixtures

@Fixture(AnotherExample::class)
object AnotherExampleFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        println(generatedFileText)

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test



        """.trimIndent()
        )
    }


    @Test
    fun `picks first enum value`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

enum class ExampleEnum {
    FIRST, SECOND
}

@Fixture(ExampleEnum::class)
object ExampleEnumFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleEnumFixtures.exampleEnum(): ExampleEnum = test.ExampleEnum.FIRST

        """.trimIndent()
        )
    }

    @Test
    fun `throws error when enum has no values`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

enum class ExampleEnum

@Fixture(ExampleEnum::class)
object ExampleEnumFixtures
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Enum has no values")
    }

    @Test
    fun `throws error for interfaces`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

interface ExampleInterface

@Fixture(ExampleInterface::class)
object ExampleInterfaceFixtures
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Interfaces cannot be annotated with @Fixture")
    }

    @Test
    fun `object fixture returns object instance`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

object ExampleObject

@Fixture(ExampleObject::class)
object ExampleObjectFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleObjectFixtures.exampleObject(): ExampleObject = test.ExampleObject

        """.trimIndent()
        )
    }

    @Test
    fun `cannot generate fixtures for private objects`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

private object ExampleObject

@Fixture(ExampleObject::class)
object ExampleObjectFixtures
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Cannot create fixtures for private classes")
    }

    @Test
    fun `cannot generate fixtures for private classes`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

private class ExampleClass

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Cannot create fixtures for private classes")
    }

    @Test
    fun `cannot generate fixtures for private constructors`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

private class ExampleClass private constructor()

@Fixture(ExampleClass::class)
object ExampleClassFixtures
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Cannot create fixtures for private primary constructors")
    }

    @Test
    fun `nested fixture types`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

class ExampleClass {
    data class ChildExampleClass(
        val example: String
    ) {
        data class AnotherChildExampleClass(
            val example: Int
        )
    }
}

@Fixture(ExampleClass::class)
object ExampleClassFixtures {
    @Fixture(ExampleClass.ChildExampleClass::class)
    object ChildExampleClassFixtures {
        @Fixture(ExampleClass.ChildExampleClass.AnotherChildExampleClass::class)
        object AnotherChildExampleClassFixtures
    }
}


                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun ExampleClassFixtures.exampleClass(): ExampleClass = test.ExampleClass(
)

public fun ExampleClassFixtures.ChildExampleClassFixtures.childExampleClass():
    ExampleClass.ChildExampleClass = test.ExampleClass.ChildExampleClass(
    example = "",
)

public
    fun ExampleClassFixtures.ChildExampleClassFixtures.AnotherChildExampleClassFixtures.anotherChildExampleClass():
    ExampleClass.ChildExampleClass.AnotherChildExampleClass =
    test.ExampleClass.ChildExampleClass.AnotherChildExampleClass(
    example = 0,
)

        """.trimIndent()
        )
    }

    @Test
    fun `nested types with child fixture`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture

object ExampleObject {
    data class ChildExampleClass(
        val example: String
    ) {
        data class AnotherChildExampleClass(
            val example: Int
        )
    }
}

@Fixture(ExampleObject.ChildExampleClass.AnotherChildExampleClass::class)
object AnotherChildExampleClassFixtures
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText("ExampleExt")

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public fun AnotherChildExampleClassFixtures.anotherChildExampleClass():
    ExampleObject.ChildExampleClass.AnotherChildExampleClass =
    test.ExampleObject.ChildExampleClass.AnotherChildExampleClass(
    example = 0,
)

        """.trimIndent()
        )
    }

    @Language("kotlin")
    private fun KotlinCompilation.compileAndGetFileText(className: String): String {
        val result = compile()
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        return File(kspSourcesDir, "kotlin/test/$className.kt").readText()
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