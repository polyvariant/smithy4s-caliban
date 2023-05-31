$version: "2"

namespace hello

service HelloService {
    operations: [GetHello]
}

operation GetHello {
    input := {
        @required
        name: String
    }
    output := {
        @required
        greeting: String
    }
}
