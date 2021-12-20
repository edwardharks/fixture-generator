package solution.littlebird.fixturegenerator

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExampleTest {

    @Test
    fun `generates fixtures`() {
        val actual = ExampleClassFixtures.exampleClass()
        val expected = ExampleClass(
            nullable = null,
            string = "",
            int = 0,
            boolean = false,
            fixture = AnotherExampleClass(
                anotherExample = ""
            ),
        )

        assertThat(expected).isEqualTo(actual)
    }
}