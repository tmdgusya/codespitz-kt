class TestUser(
    private val user: String,
    private val pw: String
) {
    init {
        println("test!")
    }

    constructor(user: String) : this(user + "+", "")
}
