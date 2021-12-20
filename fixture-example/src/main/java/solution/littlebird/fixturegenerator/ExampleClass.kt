package solution.littlebird.fixturegenerator

import com.edwardharker.fixturegenerator.Fixture

@Fixture
data class ExampleClass(
    val nullable: String?,
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val fixture: AnotherExampleClass
)

@Fixture
data class AnotherExampleClass(
    val anotherExample: String
)