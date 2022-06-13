# KClass

### KClass - 코틀린 클래스를 얻는 방법?

- 클래스::class
- 인스턴스::class

### KClass.members

- Collection<KCallable<*>>

### KCallable

- 모든 호출가능한 요소
    - KProperty 속성
        - 여기서 의문은 왜 Class Property 는 호출 가능한 요소인가? 라고 하면 Kotlin 에서는 값을 만들면 getter 또는 setter 를 자동으로 만들어준다. 물론 이건 변수가 val /
          var 냐의 차이도 있지만, kotlin delegate 를 공부해보면 kotlin property 는 호출 가능한 요소임을 좀 더 와닿게 알 수 있다.
        - 예시가 허접하지만 대충 이렇게 된다
          - ```kotlin
            class Person {
              var name: String by PersonNameDelegator()
            }
    
            class PersonNameDelegator {
              operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return "$thisRef, thank you for delegating '${property.name}' to me!"
              }
    
              operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                 println("$value has been assigned to '${property.name}' in $thisRef.") 
              }
            }
            ```
          
    - KFunction 함수, 메소드
      - ::함수, 인스턴스::메소드 (이런건 자바에서 많이해봐서 익숙했다.)
      - 함수의 리플렉션은 그 함수의 참조를 가르키는게 아니라, 람다를 계속해서 생산해 낸다. (오버헤드가 있음. 자바랑 마찬가지)

## Kotlin Collection 구조

<img width="1999" alt="image" src="https://user-images.githubusercontent.com/57784077/173362617-635785a7-fe2e-4da6-8c95-635fe330876b.png">

위 사진처럼 구조를 지니기에 Array 를 쓰지만 Iterator Interface 를 이용할 수 있고, Collection 을 이용할 수 있다.
반대로 Set 에서도 Iterator 를 사용할 수 있다. 이건 정말 좋은 기능같다. 사실 이렇게 까지 생각해본적은 없는데 생각해보니 정말 편리하게 잘 만든것 같다는 생각이 들었다.

## 프로그래밍 언어의 문과 식

- **"문(Statement)"** 은 컴파일 되고 난뒤, CPU 에 내리는 명령으로 바뀜
- **"식(Expression)"** 은 컴파일 되고 난뒤, 메모리에 올라가는 값으로 바뀜.
  - 함수형 언어나, 식을 강화하려는 언어는 "Statement" 를 "Expression" 으로 바꿨기 때문임.
    - Runtime 에 좀 더 자유로움을 주기 위해서
    - Statement 는 한번 실행하면 끝나고, 여러번 실행시킬수 없고 제어하기 힘들어서 이걸 Expression 으로 많이 바꿈.

이 말을 듣고 좀 더 Expression 이라는 것에 대한 이해가 생긴것 같기도 함. 그니까 자바의 Switch 나 If 를 보면 이 조건에 부합할때
code: 10 라인으로 가! 이런 느낌인데, 이건 사실 CPU Instruct 에 가깝고, 이걸 좀 더 런타임에서 유연하게 쓰기 위해 코틀린은 이걸 Expression 으로 변환했다.
이런식으로 느껴졌음.

그럼 어떻게 Converting 하는데? 보통은 식이였던 구문을 Function 으로 한차례 Wrapping 해서 이를 Expression 처럼 사용할 수 있게 함.

### 나만의 궁금증
그럼 삼항 연산자는 `if..else` 보일러 플레이트를 줄이기 위해 Statement 를 Expression 으로 올린건가?

## 코틀린 함수 기본값

<img width="391" alt="image" src="https://user-images.githubusercontent.com/57784077/173370472-959b735a-f1c2-4dda-9268-5f2eb2e27137.png">

위와 같이 있을때 sep 부터 기본 값이 있다고 해보자, 그러면 (T) -> String 도 기본값이 있어야 한다.
이러한 문제를 피하기 위해 `Passing Trailing Lambdas` 의 경우에는 기본값이 없어도 된다. (약간의 유연성을 위한 회피 느낌)
실제로 이런식의 코드가 코틀린 내부에 많다고 한다.


```kotlin
public fun <T, A : Appendable> Iterable<T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): A {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            buffer.appendElement(element, transform)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}
```

흠 근데 위를 보면 기본값이 있는데, Hika 님이 말씀하시고자 했던건 어떤의미일까? 잘 모르겠다.
혹시 잘 이해한사람이 있다면 댓글이나 ISSUE 로 알려주면 고마울 것 같다 ㅎㅎ..

## Smart Casting

<img width="911" alt="image" src="https://user-images.githubusercontent.com/57784077/173374026-57366750-b76e-4e92-895d-7b32a58544d2.png">

SmartCast 는 쉽게 설명하면 저 밑줄그은 부분을 기점으로 위에서 null 이 아님을 체크하고 내려왔다면, 아래에서는 null 이 아님을 전제로 코딩할 수 있다는 뜻. 
다만 필수 조건으로 변수가 immutable 해야 한다. 왜냐하면 Multi-Thread 나 여러 참조에 의해 예기치 못하게 값이 바뀔 수도 있기 때문이다. 
그래서 이를 잘 이용하기 위해 SmartCast 가 필요한 Context 내부에서는 val 임시변수에 받아서 썼던 기억이 난다.

### Matcher

Java 나 C 의 Switch, case 는 어셈블리가 작동할때 router map 처럼 작동해서 동시에 어디로 가게될지 안다고함 (분기를 이때함)
하지만 코틀린의 회(?) 는 위에서 아래 부터 순차적으로 작동하기 때문에 smartCasting 이 가능한 이유임. 반대로 말하면 Kotlin 의 회는 부하가 심하다는 뜻임.
그래서 많이 걸리는 케이스를 위로 올려야 함. (한 40% 이해한 거 같음.) -> 3개월뒤에 이 강의를 다시 들어보자.. 그땐 다르겠지

#### Keyword
- Matcher 를 제공하는 언어

### 중복 관련 이야기

#### 1번 코드
```kotlin
fun <T : Any> jsonObject(target: T): String {
    val builder = buildString {
        target::class.members
            .filterIsInstance<KProperty<*>>()
            .joinTo(buffer = this, separator = ", ", prefix = "{", postfix = "}") {
                val value = it.getter.call(target) // call 은 java reflection 의 invoke 를 생각하면 됨.
                "${jsonString(it.name)} : ${when (value) {
                    null -> "null"
                    is String -> jsonString(value)
                    is Boolean, is Number -> value.toString()
                    is List<*> -> jsonList(value)
                    else -> jsonObject(value)
                }}"
            }
    }

    return builder
}
```

#### 2번 코드
```kotlin
fun <T : Any> jsonObject(target: T): String {
    val builder = buildString {
        target::class.members
            .filterIsInstance<KProperty<*>>()
            .joinTo(buffer = this, separator = ", ", prefix = "{", postfix = "}") {
                "${jsonValue(it.name)} : ${jsonValue(it.getter.call(target))}"
            }
    }

    return builder
}
```

위의 중복 이야기는 흥미로웠음. `"${jsonValue(it.name)} : ${jsonValue(it.getter.call(target))}"` 으로 코드가 바뀌는 과정을 설명해줄때 참 좋았음.
단순 코드가 같은 것만이 중복이 아니다. 나도 이 의견에 동의. **결국 context 가 하고자 하는것이 겹치거나 다르게 표현되고 있으면 그것이 중복아닌가?** 라는 생각

순차적으로 작성하다가 결국 코드가 아래처럼 됬을때, 뭔가 깨달음이 딱 왔다. 

<img width="851" alt="image" src="https://user-images.githubusercontent.com/57784077/173377088-66203188-d080-4c50-9235-cf3fb1b7cc35.png">

근데 코드 짜는걸 보면서 참 잘 느껴지는 것이 Recursive 와 Divide And Conquer 처럼 코드를 작성하시는게 놀라웠음. 약간 재귀가 생활화 되있는 느낌..? 
- 난 재귀적 사고가 강하지 않아서 알고리즘을 열심히 풀며 노력해야 겠다는 생각도 많이들었다.

### Named Argument (a.k.a Name Parameter)

나는 개인적으로 이게 엄청 좋다고 생각한다. 예전 루비했을때 Named Argument 가 존재해서 진짜 좋았음.
이유는 Java 에서 Builder 를 쓰는 이유는 순전 Named Argument 가 없어서 라고 생각함. 왜냐면 어떤 속성을 넣는지 알기 힘드니까. (그래서 코틀린에서 Builder 를 써야 한다는 말을 잘 이해하지 못하겠음)

### JoinTo

<img width="1262" alt="image" src="https://user-images.githubusercontent.com/57784077/173379733-3327d10e-a177-425b-966f-4ac18fabb070.png">

위 사진 처럼 계속해서 StringBuilder 객체를 생성해야 하는 부담(오버헤드)이 있음. 사실 하나의 StringBuilder 객체면 충분한 구조임. 

### 확장 함수

개인적으로 되게 좋아하는 기능 ㅎㅎ. 난 이걸 통해서 객체지향을 좀 더 할 수 있다고 생각함. 이게 없어서 다른 언어는 XXUtil 로 Convert 하는게 너무 많음.
- 확장함수 안에서는 this 를 사용가능. (수신 객체)

- Extension Function 에 접근제한자를 private 으로 걸면 그 function 은 해당 파일내에서만 사용 가능하다.
  - C external 예시를 들었는데, JS 의 external 이나 default 처럼 나도 생각했다.

### 개선된 로직

<img width="1726" alt="image" src="https://user-images.githubusercontent.com/57784077/173386355-564261a8-1081-4742-8cfc-542b1e918dfb.png">

위처럼 모든 함수에서 builder 를 인자로 받고 builder 를 이용함. 그렇다면 어떻게 이 인자를 줄일 수 있을까? 
확장 함수를 통해서 가능(요건 수신객체의 개념을 이해하면 됨.) 코틀린에서는 위와 같은 상황이 중복일 수 있음. 따라서 아래와 같이 중복 제거 가능

<img width="1432" alt="image" src="https://user-images.githubusercontent.com/57784077/173387474-baaace73-4015-4cc7-bc76-39ce9a19e01e.png"> 
---
---
<img width="625" alt="image" src="https://user-images.githubusercontent.com/57784077/173390595-09bf788b-c7e5-4d4c-a413-9130f007b45f.png">


위처럼 Builder 를 변수로 이용하기에 사이에 많은 공격을 통해 로직 도중에 값을 변경시킬 수 있음. (하나의 트랜잭션이라고 이해하면 편함)
그래서 교수님께서는 `run()` 을 쓰셨으나, 나는 `buildString` 를 썼음.

```kotlin
fun stringify(value: Any?): String = buildString {
    jsonValue(value)
}
```

### 내장 확장함수

<img width="188" height="100" alt="image" src="https://user-images.githubusercontent.com/57784077/173390870-b225d128-5fef-4b3f-b05e-f01881e86db3.png">

## Annotation

Code 외적으로 힌트를 추가하는 Meta Programming 의 일종.
- Annotation 의 장점은 기존 로직에 아무런 영향도 없다.
  - 예를 들어 이건 절대 건드리지 마시오를 `@DoNotModify` 요런식으로 작성해도 된다는 뜻.
  - 물론 프로그래밍에 활용 가능함 (Spring 에서 `@Bean` 과 같이)

<img width="3112" alt="image" src="https://user-images.githubusercontent.com/57784077/173392538-4666ba28-42a8-4e90-a187-0e379a22e9c0.png">

Java Annotation Target 을 바이너리로 해두면, Compile 단계의 Annotation Processor 과정에서 이용 가능하다. 근데 런타임으로 바꾸면 이를 런타임에서 참조 가능하다.
옛날에 Web Server -> Application Server 로 천천히 컨버팅할때 Meta Programming 비스무리 하게 했었는데 이때가 기억났다 ㅎㅎ..

Annotation 의 단점은 알아야지만 사용할 수 있다는 점. JPA 에서 Entity 를 표현하기 위해서는 @Entity, @Id 를 알아야지만 사용가능하다. (xml 이나 다른 방식도 있지만 예시로..)

개인적으로 코틀린의 Annotation 이 참 간결하게 만들었다는 생각이 들었음.

## Kotlin 사용할 때 생각하면 좋은점

- 다른 언어에서 뻔한 보일러 플레이트는 코틀린 built-in 에 존재하지 않는지 찾아보면 좋음
- Kotlin 우선 순위 처리 로직을 잘 알자!

