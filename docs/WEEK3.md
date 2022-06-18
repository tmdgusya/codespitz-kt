# Week3

## Kotlin OOP

### Constructor

Kotlin 에서는 기존 Java 나 다른 언어처럼 아래와 같이 Constructor 를 선언하는 것이 가능하다.

```kotlin
class TestUser {
    private val user: String,
    private val pw: String,
    constructor(user: String, pw: String) { // SubConstructor
        this.user = user
        this.pw = pw
    }

    constructor(user: String) { // SubConstructor Overloading
        this.user = user
        this.pw = pw
    }
}
```

하지만 주 생성자에 한해서는 좀 더 다른 방법으로 선언이 가능하다. 코틀린에서 이렇게 선언되는 Constructor 는 `주 생성자` 라고 부른다.

```kotlin
class TestUser(user: String, pw: String) { 
    private val user: String,
    private val pw: String,

    constructor(user: String): this(user, "")
}
```

### Order of calling Constructor 

이렇게 만들게 되면 중요한 하나의 원칙이 생기는데, 그 원칙은 `부 생성자는 반드시 주생성자를 호출한다.` 라는 원칙이다. 
여기서 알게 될수 있는 점은 생성자의 호출순서가 `부 생성자 -> 주 생성자` 의 흐름으로 변경된다는 것이다. 아래 코드를 한번 보자. 어떤 결과가 나올 것 같은가?

```kotlin
class TestUser(user: String, pw: String) // 2 { 
    private val user: String,
    private val pw: String,

    constructor(user: String): this(user + "+", "") // 1
    
    init { // 3
        this.user = user + "-"
        this.pw = pw + "-"
    }
}
```

주석으로 적은것과 같은 순서대로 실행된다. 첫번째로 부생성자가 주생성자를 호출하고, 주 생성자는 init 을 호출한다. 자바 코드를 살펴보면 아래와 같다.

<img width="500" alt="image" src="https://user-images.githubusercontent.com/57784077/174435351-182b967a-24ce-4a04-9b17-8c3e5a63caeb.png">

코드를 확인해보면 주생성자와 부생성자가 만들어져있고, `init` 은 constructor 안에 포함되어 있다. 
값이 `TestUser(user: "roach")` 로 넘어간다면 2번째 주생성자를 호출해서 `TestUser(user = "roach+", pw = "")` 으로 되어 있을 것이다.
그리고 주생성자가 호출되면서 주 생성자의 안에 있는 `init` 블록이 실행될 것이다. 그래서 결과값은 아래와 같을 것이다. 

```sh
TestUser(user = "roach+-", pw = "-")
```

위의 Java 로 바뀐 코드를 보면 알겠지만 Kotlin `init` 블록과 Java 의 `static` 블록은 다른 것 이다. Kotlin 의 `init` 은 주생성자에 병합되는 코드이기 때문이다.
그렇다면 위의 코드에서 내부 property 에 기본값을 대입하는 경우에 순서는 어떻게 될까? 

```kotlin
class TestUser(user: String, pw: String) { 

    init {
        println("test!")
    }

    private val user: String
    private val pw: String = pw

    constructor(user: String): this(user + "+", "") 
    
    init {
        this.user = user + "-"
    }
}
```

JavaCode 로 디컴파일 하면 아래와 같은 코드로 전환된다. 

<img width="448" alt="image" src="https://user-images.githubusercontent.com/57784077/174435786-dd0c25a0-6afe-4007-964f-76a550e685bd.png">

위의 코드에서 한가지 중요한점을 알아야 하는데, property 에 할당하는 로직이 보면 `println` 을 호출하는 `init` 블록보다 아래에 있다. 
즉 `속성할당`과 `init` 은 적혀있는 순서대로 주생성자에 병합된다. 그래서 보통은 `property` 를 할당하는 것보다 아래에 `init` 을 많이 적는다고 한다. 그래서 만약 바꾼다면 아래와 같을 것이다. 즉, 요런것도 내가 느끼기에는 Team 내 규약이 필요해보인다.

```kotlin
class TestUser(user: String, pw: String) { 

    private val user: String
    private val pw: String = pw

    constructor(user: String): this(user + "+", "") 
    
    init {
        println("test!")
        this.user = user + "-"
    }
}
```

### 생성자에 property 를 선언하는 방법

Intellij 가 Java 사용자의 코드를 보면 대부분의 class 에서 Constructor 의 역할은 단순히 property 에 값을 할당하는 역할만 담당하고 있다고 조사했다고 한다. 그래서 아래와 같이
**생성자에 property 를 선언**할 수 있도록 만들어 버렸다.

```kotlin
class TestUser(
    private val user: String, 
    private val pw: String
) { 

    constructor(user: String): this(user + "+", "") 
    
    init {
        println("test!")
        this.user = user + "-"
    }
}
```

이렇게 Direct 로 주생성자에 속성을 선언하는 경우, **최상위 `init` 블록보다도 높은 순서로 매칭**된다.

```java
public final class TestUser {
   private final String user;
   private final String pw;

   public TestUser(@NotNull String user, @NotNull String pw) {
      Intrinsics.checkNotNullParameter(user, "user");
      Intrinsics.checkNotNullParameter(pw, "pw");
      super();
      this.user = user; // 순서가 init 보다도 높음
      this.pw = pw;
      String var3 = "test!";
      System.out.println(var3); // init 블록
   }

   public TestUser(@NotNull String user) {
      Intrinsics.checkNotNullParameter(user, "user");
      this(user + "+", "");
   }
}
```

이 짧은 8 ~ 10 분동안의 강의를 정리하면서 느낀점은, **나는 Constructor 에서 작업이 필요하면 주 생성자를 아래로 내려서 작업시키곤 했었는데, 
그게 아니라 init 블록에서 하는게 더 코틀린스러운 방법**이구나. 라고 생각했다.

### Constructor 를 잘 사용하는 방법 

#### 생성자가 1개인 경우 - 주 생성자 + 속성 할당 + init (요걸 Hika 님은 주생성자 전략이라고 하심)

주 생성자 전략에서는 왠만한 속성을 그냥 기본적으로 받고, 무언가 가공이 필요하거나 Sub 작업이 필요하다면 그 작업을 `init` 작업에서 하도록 유도하는 전략이다.

- 예시코드
    ```kotlin
    class TestUser(
        private val user: String, 
        private val pw: String
    ) {         
        init {
            this.pw = pw + "-"
        }
    }
    ```

#### 생성자가 여러개인 경우

- **기본 값으로 해결 가능한 경우 (Kotlin Style)**
    - 이 상황에서는 위에 선언한 `주 생성자 전략` 을 사용한다.
    - 아래 예시코드 처럼 작성하게 되면, user 를 넘겨줬을때와 pw 를 안넘겨주었을때의 대응이 가능하다. 즉, constrcutor 하나로 두개의 constructor 대응을 하는 것과 같은 효과를 낸다는 것이다.
    - 예시코드
        ```kotlin
        class TestUser(
            private val user: String, 
            private val pw: String = ""
        ) {         
            init {
                this.pw = pw + "-"
            }
        }
        ```

- 인자의 타입이 달라 기본 값으로 해결할 수 없는 경우 (보통의 overloading 을 하는 이유)
    - **부 생성자만 사용 (Java Style)**
    - 근데 요런 경우에는 Factory Method 로 따로 빼는게 좋다고 함.

## OOP

### 객체지향 언어

객체지향을 처음부터 지원하는 언어는 아래와 같이 두가지 사항을 지원한다.

- **대체가능성**: 하위형은 상위형으로 대체할 수 있음
    - Parent 에 Child 를 대입가능
- **내적동질성**: 어떤 형으로 객체를 참조해도 원래 형으로 동작한다.

#### 내적동질성 예시코드

```kotlin
open class Parent {
    protected open fun print() {
        println("parent")
    }

    fun message() {
        println()
    }
}

class Child : Parent() {
    override fun print() {
        println("child")
    }
}

fun main() {
    val actor: Parent = Child() // 대체가능성
    actor.message()
}
```
예를 들면 위의 main 문에서 `message()` 를 호출하면 Parent Class 의 `message` 가 호출될 것이다. 근데 만약에 아래와 같이 작성하면 어떻게 될까?

```kotlin
class Child : Parent() {

    override fun message() {
        println("child message!!")
    }

    override fun print() {
        println("child")
    }
}
```

결과는 `child message!!` 이다. 즉, **변수는 `Parent` 타입임에도 불구하고, 처음에 Child 로 생성되었기에 `Child` 의 속성을 계속해서 가지게 된다.** **이것을 내적 동질성**이라고 한다. 이 두가지를 잘 지키고 있다면 **객체지향언어**이다.

### 객체지향 프로그래밍

1. 어떤 문제를 해결할때 객체 간의 협력으로 해결한다.
2. 객체는 협력을 위해 메세지를 주고 받는다.

변화율에 따라 코드를 역할로 나누고, 역할에 맞는 객체를 만든 뒤, 코드를 최대한 다른 객체에게 떠민다.

위의 문장에서 조영호님도 항상 말하시는 부분이 "역할" 을 먼져나누고, 그 뒤에 "객체" 를 만든다. 라는 설명을 해주시는데 이게 중요한 것 같다. 
즉, 코드로 설명하자면 **"A" 라는 행동을 만들고 이걸 수행하는 객체인 "AImpl" 을 만드는 것** 이라고 나는 생각한다. 그래서 이렇게 만들게 되면 **"AInterface" , "AImpl-1", "AImpl-2" 가 있어 쉽게 변경**하기도 좋다. 

- **변화율** : 코드마다 변하는 이유가 다름, 이걸 변화율이라고 함. 내가 느끼기엔 설명하시고자 하는게, **단일 책임의 원칙(단일 변경점의 원칙)** 을 말하시는 것 같았음 (SRP)
- **역할에 맞는 객체** : 보다 추상적인 개념에서 이걸 어떻게 설명할 것인가. -> **즉, 구체적인 구현에 의존하지 말라는 뜻**. 내가 위에 적어놓은 설명을 참조하면 된다. (OCP)
- **코드를 최대한 다른 객체에게 떠민다.** : **내가 하고 있는 일을 책임에 맞는 객체에게 위임**하는 것이다. (LSP)
    - 다운 캐스팅이 있는 것이 있나? 다운 캐스팅이 있다면 LSP 를 위반하는 것 이다.

### 객체지향 프로그래밍 예시

요구사항: 정해진 내용이 정해진 시간에 전달된다.

#### 필요한 객체

- **User**
- **Item**: 정해진 내용
- **Scheduler**: 정해진 시간
    - 내가 어떠한 주기로 보낼것인지에 대한 변화요소가 있음
- **Sender**: 내용 전달
    - 내가 메일로 보낼지, 카카오톡으로 보낼지 등등으로 인한 변화요소가 있음
- **Looper**: Pull 역할을 담당.
    - Pull-Push 구조 Event Architecture
    - Socket 통신을 생각하면 편함.

<img width="615" alt="image" src="https://user-images.githubusercontent.com/57784077/174438117-cee001b8-959a-4862-83ca-cb64f5fa8b9b.png">

#### 예시하다가 나온 얘기들 

- 객체지향에서 제일 위험한 포인트는 **"순환 참조 고리"** 가 생길때, 요걸 듣고 경석님이 동일 Layer 에서는 단방향 참조를 하자~! 라고 했던게 기억났음. 
Intellij 에서 이를 찾아주는 기능이 있다고 함.

- Java 기준으로 짤때 static 한 수준으로 올라가는 경우가 많다면, 무언가 잘못된 것. static level 까지 올라가면 유연하지 못하다. 
이 얘기를 들었을때 조영호님이 **Compile Type 에 어떻게 될지 아는 것보다, Runtime 에 순서가 결정되는 것이 좀 더 유연한 설계**를 할 수 있다고 말씀하신게 떠올랐음. 
예를 들면 **Java 에서는 DI 와 위에서 설명한 내적동질성으로 이러한 코드를 작성**할 수 있다.

- TDD 파는 의존성이 낮은 Sender 부터 위에서 만드는게 좋다고 함. 다른 부분은 User -> Sender 로 내려오는 구조임.

- 객체지향에서 객체를 판단하는 `Identifier` 가 굉장히 중요함.
    - 객체를 식별하는 근본적인 비교는 **객체를 담고있는 주소로 비교**함. 이걸 **Reference Context** 라고 함. 반대로 가지고 있는 값에 의존하는 것은, **Value Context** 라고 함. 여기서 많은 생각이 든게, 아 이래서 Java 에 `hashcode` 를 둔것이고, 이걸 Override 할 수 있게 한건가? 이런 생각이 많이 들었음. 그래서 `hashSetOf` 에 관한 내용도 와닿았음.
    - 그래서 다른 언어에서는 `Structure` 랑 `Class` 를 따로 만들 수 있게 되어 있는 언어들도 있다. Kotlin 의 경우 `data class` 와 `class` 로 생각하면 됨.
    - 객체지향프로그래밍에서는 Reference Context 를 사용하는게 기본이다. 라는 것을 잊지말아야 함.

- 코드를 작성할때 이 코드가 Abstract 한 곳에 있어야 할까? 아니면 Concrete 한 곳에 있어야 할까? 를 고민을 많이 해야 함.
  - 상태를 가져야 하니까, Abstract Class 로 Interface 를 바꾸고, 내부 구현은 가시성을 낮추었음. -> **Template Method Pattern**

- 상속은 문제가 많은데, **Template Method Pattern** 을 사용하면, 문제점들을 회피할 수 있음.

- 코드를 작성할때, 전략패턴으로 변경하고, 객체의 라이프사이클과 스코프를 확줄였는데, 이게 **합성** 이 상속보다 유리한 이유.  
  - 앞으로 **합성** 을 이용할때 Scope 나 Life-Cycle 을 줄일수 있는 방법도 잘 생각해봐야 할것 같다는 생각이 들었다. 
  - 전략객체의 단점은, Runtime 에 조합되는 구조임.

## 참고

https://www.youtube.com/watch?v=GycJOZWpjr8