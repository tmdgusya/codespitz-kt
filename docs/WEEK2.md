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



## Kotlin 사용할 때 생각하면 좋은점

- 다른 언어에서 뻔한 보일러 플레이트는 코틀린 built-in 에 존재하지 않는지 찾아보면 좋음

