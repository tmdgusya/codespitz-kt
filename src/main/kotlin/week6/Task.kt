package week6
class Task(
    val run: (Controller) -> Unit
) {
    var isCompleted = false
    var result: Result<Any?>? = null
    var next: Task? = null
}
