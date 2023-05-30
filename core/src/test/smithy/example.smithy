$version: "2"

namespace smithy4s.example

structure Rec {
    next: Rec
    @required
    name: String
}

structure MovieTheater {
    name: String
}

union Foo {
    int: Integer
    str: String
    bInt: BigInteger
    bDec: BigDecimal
}
