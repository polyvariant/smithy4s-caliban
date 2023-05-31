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

intEnum EnumResult {
    FIRST = 1
    SECOND = 2
}

enum Ingredient {
    MUSHROOM
    CHEESE
    SALAD
    TOMATO = "Tomato"
}

structure CityCoordinates {
    @required
    latitude: Float
    @required
    longitude: Float
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
