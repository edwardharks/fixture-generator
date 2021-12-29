package solution.littlebird.fixturegenerator

import com.edwardharker.fixturegenerator.Fixture

@Fixture
data class PrimitiveExamples(
    val nullable: String?,
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

@Fixture
data class FixtureExamples(
    val fixture: FixtureExampleClass,
)

@Fixture
data class CollectionExamples(
    val list: List<String>,
    val map: Map<String, String>,
    val set: Set<String>,
)

@Fixture
data class ArrayExamples(
    val array: Array<String>,
    val intArray: IntArray,
    val charArray: CharArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,
    val shortArray: ShortArray,
    val longArray: LongArray,
    val byteArray: ByteArray,
    val booleanArray: BooleanArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrayExamples

        if (!array.contentEquals(other.array)) return false
        if (!intArray.contentEquals(other.intArray)) return false
        if (!charArray.contentEquals(other.charArray)) return false
        if (!floatArray.contentEquals(other.floatArray)) return false
        if (!doubleArray.contentEquals(other.doubleArray)) return false
        if (!shortArray.contentEquals(other.shortArray)) return false
        if (!longArray.contentEquals(other.longArray)) return false
        if (!byteArray.contentEquals(other.byteArray)) return false
        if (!booleanArray.contentEquals(other.booleanArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = array.contentHashCode()
        result = 31 * result + intArray.contentHashCode()
        result = 31 * result + charArray.contentHashCode()
        result = 31 * result + floatArray.contentHashCode()
        result = 31 * result + doubleArray.contentHashCode()
        result = 31 * result + shortArray.contentHashCode()
        result = 31 * result + longArray.contentHashCode()
        result = 31 * result + byteArray.contentHashCode()
        result = 31 * result + booleanArray.contentHashCode()
        return result
    }

}

@Fixture
data class FixtureExampleClass(
    val example: String
)

// TODO

/* Don't generate fixtures for private constructors */
//@Fixture
//class PrivateConstructor private constructor(val foo: String)

/* Inner class Fixture should be child of outer class fixture to avoid conflicts */
//@Fixture
//class OuterClass(
//    val inner: InnerClass
//) {
//    @Fixture
//    class InnerClass(foo: String)
//}

/* Support types that cannot be annotated */
//@Fixture
//data class LibraryTypes(
//    val libraryType: Date
//)

/* How do we know which constructor to generate the fixture for?
   Easy solution is just to use the primary constructor */
//@Fixture
//class MultipleConstructors(
//    val one: Int,
//    val two: Int,
//) {
//    constructor() : this(1, 2)
//}
