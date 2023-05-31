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

service WeatherService {
    operations: [ListCities, RefreshCities]
}

@readonly
operation ListCities {
    output := {
        @required
        cities: Cities
    }
}

list Cities {
    member: City
}

structure City {
    @required
    name: String
}

operation RefreshCities {
    output := {
        @required
        result: String
    }
}
