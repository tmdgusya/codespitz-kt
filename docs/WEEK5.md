# Week4

# 변성 (Variance)

Generic Type 의 대체 가능성을 정의 **(무공변, 반공변, 공변), 변성은 Genrice Parameter Type 간의 관계를 나타낼때 쓰이는 단어**임.

## 무공변(invariance)

Generic 의 Parameter Type 은 각각 고유하므로 GenericType 사이의 대체 가능성은 기본적으로 성립하지 않음. 이 상태가 **무공변(invariance)** 상태임. 이 말을 듣고 느낀 점은, 지금 까지 Generic Parameter 에 넣는 Type 을 자꾸, 내가 생성한 Class 들의 상속관계로 가져와서 생각하다보니, 공변을 한동안 이해하기 어려웠던 건가? 이런 생각이 들었다.

```kotlin
class Tree<T>(val value: T)
var tree: Tree<Number> = Tree<Int>(10) // error
```

## 공변(covariance)

하지만 Kotlin 도 객체지향 언어이므로, 대체 가능성을 지원하고 있음. 따라서 Generic Parameter Type 에서 대체가능성을 지원하는 경우가 있는데 이때를 **"공변(covariance)"** 라고 함. **Parameter Type 이 기존 Type 의 관계를 가져가려는 시도**임. 

```kotlin
class Tree<T>(val value: T)
var tree: Tree<Number> = Tree<Int>(10) // success
tree.value.toDouble()
```

공변이 성립되는 경우는 위의 예제에서 Type Parameter 로 쓰인 T Type 이 바깥으로 노출되는 구조일땐데, 즉 **Producer 형태로 값을 노출시키는 구조에만 가능**하다. 그 이유는 다른 값을 받게 될 경우에는 문제가 발생할 수도 있다. 예를 들면, 지금은 init 하는 시점에 받은 Int Value 만 사용하는데 후에 다른 Type 의 값이 들어오게 될 수도 있기 때문이다. 아래 예시를 보면 알겠지만, 그래서 코틀린에서는 이를 방지하고 있다.

```kotlin
 var list: MutableList<Number> = mutableListOf<Int>(10) // Type mismatch: inferred type is MutableList<Int> but MutableList<Number> was expected
    
list.add(10.0) // 이런식으로 이상 한 값이 들어올 수 있음. 하지만 Kotlin 에서는 안됨. 

println(list)
```

위의 예제를 보면 **일반적인 Type 의 대체 가능성을 생각해본다면 가능**할 것 같다. **하지만 불가능**하다. 그 이유는 **Invariance** 하기 때문이다. Invariance 한 이유는 mutableList 는 Producer 와 Consumer 역할을 동시에 하기 때문이다.

```kotlin
fun main() {
    var list: List<Number> = listOf<Int>(10)
    println(list)
}
```

반면의 위의 List 를 사용한 예제는 잘 동작한다. 그 이유는 코틀린의 List Collection 은 기본적으로 immutable 하다. 따라서 Producer 의 역할만 하게된다. 그래서 공변이 성립할 수 있게 되는 것이다. 즉, Producer 역할만 하게 되는 경우에는 Covariance 하다.

### out

그래서 공변의 경우에는 생산자가 **외부에 T Type 을 노출**하게 되는 구조이므로, `out` 식별자를 사용하게 된다. 그래서 아래와 같이 적게 되면, 내가 외부에 이 List 의 원소들을 Number Type 으로 노출시킬꺼야. 라고 컴파일 시점에 공표하는 것과 같다. (Intellij 를 쓴다면 out 식별자를 저기 붙여주면, List 는 이미 invariance 하니까 없애라고 나온다.)

```kotlin
val list: List<out Number> = listOf(10)
```

## 반공변성(contravariant) && in

반대로 파라미터 구상타입에 추상타입이 들어오는 경우를 **반공변성(contravariant)** 라고 함. 즉 `in` 은 Consumer 구조 일때 사용가능하다.

```kotlin
internal class Node<in T : Number> (
    private val value: T,
    private val next: Node<T>? = null
) {
    operator fun contains(target: T): Boolean {
        return if (value.toInt() == target.toInt()) true else next?.contains(target) ?: false
    }

    fun isPositive(target: T): Boolean {
        return target.toInt().isPositive()
    }
}

fun main() {
    val node: Node<Int> = Node<Number>(10.0)
    node.contains(8)
}
```

위의 예시를 보면 **Int Type Node 에 Number Type Node 를 대입**하고 있다. 이게 어떻게 가능한 일일까? 일단 하나하나씩 살펴보면 아래와 같은 이유를 추론할 수 있다.

1. contains 메소드 안에 들어왔을때, `toInt()` 메소드가 실행하는데 **Number Type 들은 해당메소드로 Convert 시 반드시 Int Type 으로 변한다.** 따라서 Number Type 으로만 UpperBound 규약이 있다면 상관없음.

2. contains 로직은 Number 의 method 이므로 아무런 문제가 없음.

값을 UpperBound 로 규정지어서 가능하다. 아래 예시를 한번 보면 좀 더 이해가 편할 것 이다.

```kotlin
internal class Node<in T : Number> (
    private val value: T,
    private val next: Node<T>? = null
) {
    operator fun contains(target: T): Boolean {
        return if (value.toInt() == target.toInt()) true else next?.contains(target) ?: false
    }

    fun isPositive(target: T): Boolean {
        return target.toInt().isPositive() // 반드시 Int 로 Bound 를 규정짓고만 확장함수를 사용가능.
    }
}

// 확장함수 추가
fun Int.isPositive(): Boolean {
    return this >= 0
}
```

## 선언지점 변성

코틀린에서는 `in`, `out` 을 이용해서 컴파일 시점에 변성에 대한 조건을 체크할 수 있다. Java 에서는 컴파일 시점에 변성 체크가 불가능하다. 따라서 아래 코드를 만나면 죽는다.

```kotlin
 var list: MutableList<Number> = mutableListOf<Int>(10) 
    
list.add(10.0) // dead
list.get(0)

println(list)
```

그래서 위와 같은 상황에서는 **"무공변성(invariance)"** 를 유지해야만 한다.

# Kotlin DSL

**Domain Specific Language** 란? 특정 문제를 해결하기 위해 알고리즘으로 구현하지 않고, 미리 정해진 함수나 클래스 등의 표현을 사용하여 구현. 예를 들면 SQL 이 있다.