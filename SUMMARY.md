# Prototype 안드로이드 템플릿 분석 정리

## 1. 이 문서의 목적

이 문서는 안드로이드 개발이 처음인 사람을 기준으로, 현재 `Prototype` 프로젝트가 어떤 상태인지 설명하고 특히 `app/src/main/java/com/example/prototype/ui/theme/Theme.kt`가 무슨 역할을 하는지 자세히 정리한 문서다.

지금 프로젝트는 기능 개발을 시작하기 전의 템플릿에 가깝다. 그래서 오히려 배우기에는 좋다. 복잡한 비즈니스 로직, 네트워크, 데이터베이스, 상태관리 아키텍처가 거의 없고, 안드로이드 앱이 어떤 식으로 시작되고 화면이 그려지는지 기본 흐름을 보기 쉽기 때문이다.

---

## 2. 이 프로젝트를 한 문장으로 요약하면

이 프로젝트는 **Jetpack Compose 기반의 최신 안드로이드 단일 모듈 앱 템플릿**이다.

조금 더 풀어서 말하면 다음과 같다.

- 화면을 XML 레이아웃이 아니라 Kotlin 코드로 작성한다.
- 앱의 시작점은 `AndroidManifest.xml`과 `MainActivity.kt`다.
- 화면의 공통 색상/타이포그래피는 `ui/theme` 패키지에서 관리한다.
- 아직 실제 앱 기능은 없고, 샘플 화면과 샘플 테스트만 들어 있다.
- `Theme.kt`는 Compose UI 전체의 색상 체계를 정하는 핵심 파일이다.

---

## 3. 먼저 꼭 알아둘 핵심 개념 6가지

### 3.1 이 프로젝트는 XML UI보다 Compose UI가 중심이다

예전 안드로이드에서는 `activity_main.xml` 같은 XML 파일에 화면을 만들고, Kotlin/Java 코드에서 그 XML을 연결하는 방식이 많았다.  
이 프로젝트는 그 방식이 아니다.

여기서는 다음처럼 Kotlin 함수가 화면 그 자체다.

- `PrototypeApp()`
- `Greeting()`
- `PrototypeTheme()`

즉, "화면 = Kotlin 코드"라고 생각하면 된다.

### 3.2 `themes.xml`과 `Theme.kt`는 역할이 다르다

초보자가 가장 많이 헷갈리는 부분이다.

- `res/values/themes.xml`
  - 안드로이드 시스템이 액티비티를 띄울 때 참고하는 XML 테마다.
  - 앱 시작 직후, Compose가 화면을 그리기 전에도 의미가 있다.
- `ui/theme/Theme.kt`
  - Compose 내부에서 Material 3 색상/글꼴 체계를 적용하는 Kotlin 테마다.
  - 실제 Compose 버튼, 텍스트, 카드 등의 색감은 주로 여기 영향을 받는다.

즉, **XML 테마와 Compose 테마는 함께 존재하지만 담당 범위가 다르다.**

### 3.3 `colors.xml`과 `Color.kt`도 역할이 다르다

이것도 자주 헷갈린다.

- `res/values/colors.xml`
  - XML 리소스 시스템용 색상 파일이다.
  - 전통적인 View/XML 기반 UI에서 자주 사용된다.
- `ui/theme/Color.kt`
  - Compose 코드에서 직접 사용하는 색상 객체를 정의한다.

현재 프로젝트의 `Theme.kt`는 `colors.xml`이 아니라 **`Color.kt`에 있는 색상 값**을 사용한다.

### 3.4 이 프로젝트는 동적 색상(dynamic color)을 기본 사용한다

`Theme.kt`에서 `dynamicColor: Boolean = true`로 되어 있다.  
그리고 `minSdk = 33`이라서 이 앱은 Android 13 이상에서만 실행된다.

이 말은 거의 항상 다음을 뜻한다.

- 실행 기기의 안드로이드 버전이 동적 색상을 지원한다.
- 그래서 실제 실행 시에는 `Color.kt`의 보라색 계열보다, 기기 배경화면 기반 시스템 색상이 우선 적용될 가능성이 매우 높다.

중요한 결론:

- `Color.kt` 값을 바꿨는데 화면 색이 안 바뀌는 것처럼 보일 수 있다.
- 그 이유는 `dynamicColor = true` 때문일 가능성이 높다.

### 3.5 `build/` 폴더는 생성물이다

프로젝트를 보면 `app/build/...` 아래 파일이 매우 많을 수 있다.  
이 파일들은 대부분 Gradle과 Android Studio가 자동 생성한 결과물이다.

보통 초보자는 다음 원칙만 기억하면 된다.

- `src/`는 사람이 직접 수정하는 소스다.
- `build/`는 도구가 생성하는 산출물이다.
- 기능 개발은 주로 `app/src/main/...`에서 한다.

### 3.6 지금은 "앱 기능"보다 "앱 골격"이 있는 상태다

현재 들어 있는 것은 대략 다음뿐이다.

- 진입 액티비티
- Compose 테마
- 샘플 내비게이션 UI
- 샘플 텍스트 `"Hello Android!"`
- 샘플 단위 테스트, 샘플 계측 테스트

즉, 이 프로젝트는 완성품이 아니라 **학습용 출발점**이다.

---

## 4. 현재 프로젝트 구조

핵심만 보면 구조는 대략 이렇다.

```text
Prototype/
├─ app/
│  ├─ build.gradle.kts
│  └─ src/
│     ├─ main/
│     │  ├─ AndroidManifest.xml
│     │  ├─ java/com/example/prototype/
│     │  │  ├─ MainActivity.kt
│     │  │  └─ ui/theme/
│     │  │     ├─ Color.kt
│     │  │     ├─ Theme.kt
│     │  │     └─ Type.kt
│     │  └─ res/
│     │     ├─ drawable/
│     │     ├─ mipmap-*/
│     │     ├─ values/
│     │     └─ xml/
│     ├─ test/
│     └─ androidTest/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradle.properties
└─ gradle/libs.versions.toml
```

각 파일의 의미를 간단히 정리하면 다음과 같다.

- `MainActivity.kt`
  - 앱 실행 후 가장 먼저 보이는 액티비티이자 Compose UI 시작점
- `Theme.kt`
  - Compose 전역 테마 적용
- `Color.kt`
  - 테마에서 사용할 색상 값 정의
- `Type.kt`
  - 타이포그래피 정의
- `AndroidManifest.xml`
  - 앱 메타정보, 런처 액티비티, 아이콘, 테마 등 선언
- `app/build.gradle.kts`
  - 앱 모듈의 SDK 버전, 의존성, Compose 활성화 등 설정
- `libs.versions.toml`
  - 라이브러리 버전을 모아두는 카탈로그

---

## 5. 앱이 실행되는 전체 흐름

이 프로젝트의 실행 흐름은 다음 순서로 이해하면 된다.

1. 사용자가 앱 아이콘을 누른다.
2. 안드로이드 시스템이 `AndroidManifest.xml`을 읽는다.
3. 런처 액티비티로 지정된 `.MainActivity`를 실행한다.
4. `MainActivity.onCreate()`가 호출된다.
5. `enableEdgeToEdge()`로 시스템 바 영역까지 그릴 수 있는 모드가 켜진다.
6. `setContent { ... }` 안에서 Compose UI 트리가 시작된다.
7. `PrototypeTheme { ... }`가 전체 화면에 공통 테마를 입힌다.
8. 그 안에서 `PrototypeApp()`이 실제 화면 구조를 만든다.
9. `NavigationSuiteScaffold`와 `Scaffold`가 기본 레이아웃 뼈대를 만든다.
10. `Greeting("Android")`가 `"Hello Android!"` 텍스트를 보여준다.

이 흐름을 짧게 코드 관점으로 줄이면 다음과 같다.

```text
AndroidManifest.xml
→ MainActivity
→ onCreate()
→ setContent { PrototypeTheme { PrototypeApp() } }
→ NavigationSuiteScaffold
→ Scaffold
→ Greeting
```

---

## 6. `Theme.kt`가 하는 일 한눈에 보기

`Theme.kt`는 Compose 앱의 공통 디자인 규칙을 적용하는 파일이다.

이 파일이 담당하는 핵심은 다음 세 가지다.

1. 라이트 모드용 색상 세트 정의
2. 다크 모드용 색상 세트 정의
3. 현재 기기 상태에 맞는 색상 세트를 선택해서 `MaterialTheme`로 화면 전체에 적용

즉, `Theme.kt`는 "앱 전체의 분위기와 기본 색 체계를 결정하는 문지기"라고 보면 된다.

---

## 7. `Theme.kt` 상세 해설

### 7.1 파일의 핵심 코드 구조

`Theme.kt`는 크게 세 부분으로 나뉜다.

1. `DarkColorScheme` 정의
2. `LightColorScheme` 정의
3. `PrototypeTheme()` 함수 정의

이 구조는 Compose Material 3 템플릿에서 아주 흔한 형태다.

### 7.2 `DarkColorScheme`

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
```

이 블록의 의미는 다음과 같다.

- 다크 모드일 때 사용할 기본 색상 역할을 정한다.
- `primary`, `secondary`, `tertiary`는 단순한 "색 3개"가 아니라 **역할(role)** 기반 색상이다.
- Material 3 컴포넌트는 이 역할 기반 색을 참조해서 버튼, 강조 요소, 일부 배경 등을 구성한다.

여기서 중요한 점:

- `private`라서 이 파일 밖에서는 직접 접근할 수 없다.
- 즉, 외부 화면 코드가 `DarkColorScheme.primary`를 직접 쓰는 구조가 아니라, `MaterialTheme.colorScheme.primary`를 통해 우회적으로 접근하게 된다.

### 7.3 `LightColorScheme`

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
```

이건 라이트 모드용 정적 색상 세트다.

다크/라이트 색상이 서로 다른 이유는 단순히 취향이 아니라 **명도 대비** 때문이다.

- 어두운 화면에서는 너무 어두운 포인트 색을 쓰면 묻힌다.
- 밝은 화면에서는 너무 밝은 포인트 색을 쓰면 대비가 약하다.

그래서 템플릿은 흔히 다크 모드에 더 밝은 톤, 라이트 모드에 더 진한 톤을 배치한다.

### 7.4 왜 `primary`, `secondary`, `tertiary`만 지정했을까

`lightColorScheme(...)`, `darkColorScheme(...)`에는 더 많은 항목을 넣을 수 있다.

예를 들면 다음 같은 역할도 있다.

- `background`
- `surface`
- `onPrimary`
- `onSecondary`
- `onBackground`
- `onSurface`

그런데 현재 템플릿은 최소값만 지정했다.  
주석에도 "필요하면 나머지 기본 색을 override 하라"는 뜻의 예시가 남아 있다.

즉, 현재 상태는 다음에 가깝다.

- 기본 템플릿이 최소한만 커스터마이징함
- 나머지는 Material 3 기본 동작에 맡김

### 7.5 `PrototypeTheme()` 함수 시그니처

```kotlin
@Composable
fun PrototypeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

이 함수는 "테마를 적용한 UI 컨테이너"다.

각 파라미터 의미는 다음과 같다.

- `darkTheme`
  - 현재 다크 테마를 쓸지 여부
  - 기본값이 `isSystemInDarkTheme()`라서, 사용자가 기기 전체를 다크 모드로 쓰면 앱도 기본적으로 그 흐름을 따른다.
- `dynamicColor`
  - 시스템 동적 색상을 사용할지 여부
  - 기본값이 `true`
- `content`
  - 이 테마 안에서 렌더링할 실제 UI

즉, `PrototypeTheme { PrototypeApp() }`는  
"`PrototypeApp()`을 이 테마 규칙 아래에서 그려라"라는 뜻이다.

### 7.6 `isSystemInDarkTheme()`

이 함수는 Compose가 제공하는 헬퍼다.

역할은 단순하다.

- 현재 시스템 설정이 다크 모드인지 확인
- `true`면 다크 테마 경로로 갈 수 있게 함
- `false`면 라이트 테마 경로로 갈 수 있게 함

초보자 관점에서는 이렇게 이해하면 충분하다.

- "기기 설정을 자동으로 따라가게 만드는 기본값"

### 7.7 `dynamicColor`

이 값은 현재 `Theme.kt`에서 가장 중요한 포인트다.

코드는 다음 로직을 가진다.

```kotlin
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
```

의미를 순서대로 풀면 다음과 같다.

1. `dynamicColor`가 `true`인지 본다.
2. 기기 버전이 Android 12(API 31, `S`) 이상인지 본다.
3. 두 조건이 모두 만족되면, 정적 보라색 테마 대신 시스템 동적 색 테마를 사용한다.
4. 동적 색을 못 쓰는 경우에만 `DarkColorScheme` 또는 `LightColorScheme`를 쓴다.

### 7.8 이 프로젝트에서는 왜 동적 색상이 특히 중요할까

현재 `app/build.gradle.kts`에서 다음 설정이 있다.

- `minSdk = 33`

즉, 이 앱은 Android 13 이상에서만 설치 가능하다.  
Android 13은 이미 Android 12보다 높은 버전이므로, `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` 조건을 항상 만족한다.

그래서 기본 상태에서는 사실상 다음처럼 생각해도 큰 무리가 없다.

- `dynamicColor = true`라면
- 실제 실행 환경에서 `Color.kt`의 정적 보라색은 거의 fallback 역할이 된다.

이건 아주 중요한 관찰이다.

초보자가 흔히 하는 실수:

1. `Color.kt`에서 색상을 바꾼다.
2. 앱을 실행한다.
3. 색이 기대대로 안 바뀐다.
4. "왜 안 바뀌지?" 하고 혼란스러워한다.

현재 템플릿에서는 충분히 가능한 일이다.  
왜냐하면 동적 색상이 켜져 있으면 시스템 색상이 우선되기 때문이다.

### 7.9 `LocalContext.current`

동적 색상 함수를 호출하려면 `Context`가 필요하다.  
Compose 안에서는 보통 `LocalContext.current`로 현재 컨텍스트를 가져온다.

이 값은 다음에 쓰인다.

- `dynamicDarkColorScheme(context)`
- `dynamicLightColorScheme(context)`

즉, 기기/앱 환경 정보를 바탕으로 시스템이 제공하는 색상 세트를 읽어오기 위한 재료다.

### 7.10 `MaterialTheme(...)`

마지막 줄은 이 파일의 결론이다.

```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
)
```

이 코드는 다음 의미를 가진다.

- 방금 고른 `colorScheme`를 현재 UI 트리 전체에 제공
- `Type.kt`에 정의한 `Typography`를 함께 제공
- 자식 Composable들이 이 테마 값을 참조할 수 있게 함

예를 들어 자식 화면에서는 대개 이런 식으로 사용할 수 있다.

- `MaterialTheme.colorScheme.primary`
- `MaterialTheme.typography.bodyLarge`

즉, `MaterialTheme`는 단순한 함수가 아니라  
**Compose 화면 전체에 공통 디자인 값을 흘려보내는 공급자(provider)** 역할을 한다.

### 7.11 `shapes`를 왜 안 넣었을까

Material 3 테마는 보통 세 축으로 생각할 수 있다.

- colors
- typography
- shapes

현재 `Theme.kt`는 `shapes`를 직접 넘기지 않는다.  
그래서 기본 Material 3 shape 체계를 그대로 사용한다고 이해하면 된다.

즉, 지금은 다음만 커스터마이징되어 있다.

- 색상 체계
- 타이포그래피 일부

### 7.12 이 파일에서 초보자가 꼭 기억할 결론

`Theme.kt`를 보면 다음 사실을 기억하면 된다.

1. 이 파일은 Compose 전역 테마를 적용한다.
2. 라이트/다크 모드 분기가 있다.
3. 동적 색상이 기본 활성화되어 있다.
4. 그래서 `Color.kt` 수정이 곧바로 보이지 않을 수 있다.
5. 실제 화면 컴포넌트는 `MaterialTheme`를 통해 이 값을 사용한다.

---

## 8. `Theme.kt`와 연결된 다른 테마 파일들

### 8.1 `Color.kt`

현재 내용은 정적 색상 상수 정의다.

- `Purple80`
- `PurpleGrey80`
- `Pink80`
- `Purple40`
- `PurpleGrey40`
- `Pink40`

이 파일의 역할은 단순하다.

- Compose 코드에서 사용할 색을 Kotlin 객체로 정의
- `Theme.kt`가 이 값을 가져다 씀

중요한 점:

- 이 파일은 **Compose용 색상 정의 파일**이다.
- `res/values/colors.xml`과 이름은 비슷하지만 쓰임이 다르다.

### 8.2 `Type.kt`

`Type.kt`는 앱 타이포그래피를 정의한다.

현재는 `bodyLarge`만 직접 설정한다.

- `fontFamily = FontFamily.Default`
- `fontWeight = FontWeight.Normal`
- `fontSize = 16.sp`
- `lineHeight = 24.sp`
- `letterSpacing = 0.5.sp`

즉, 아직 타이포그래피도 거의 기본 템플릿 상태다.

주석으로 남아 있는 `titleLarge`, `labelSmall` 예시는  
"원하면 더 많은 텍스트 스타일을 여기서 커스터마이징하라"는 안내에 가깝다.

### 8.3 `themes.xml`

현재 XML 테마는 아래 한 줄이 핵심이다.

```xml
<style name="Theme.Prototype" parent="android:Theme.Material.Light.NoActionBar" />
```

이 파일을 이렇게 이해하면 된다.

- 안드로이드 시스템 레벨 테마
- 액션바 없는 라이트 테마를 기반으로 함
- 액티비티 시작 시 적용됨

하지만 Compose 화면 내부의 구체적인 Material 3 색감은 주로 `Theme.kt`가 결정한다.

즉, 이 파일은 중요하지만 역할이 다르다.

- `themes.xml`: 시스템/액티비티 단 레벨
- `Theme.kt`: Compose UI 레벨

### 8.4 `colors.xml`

현재 `colors.xml`에는 다음 색들이 있다.

- `purple_200`
- `purple_500`
- `purple_700`
- `teal_200`
- `teal_700`
- `black`
- `white`

하지만 현재 소스 기준으로 이 색들은 **실제 앱 코드에서 참조되지 않는다.**

즉, 이 파일은 지금 상태에서는 거의 템플릿 잔재에 가깝다.

초보자가 꼭 기억할 점:

- `colors.xml`을 바꿔도 Compose UI 색이 안 바뀔 수 있다.
- 현재 구조에서는 `Theme.kt`와 `Color.kt`를 먼저 보는 것이 맞다.

---

## 9. `MainActivity.kt` 상세 해설

`Theme.kt`를 이해하려면, 그 테마가 어디서 적용되는지도 같이 봐야 한다.

### 9.1 `MainActivity`의 역할

```kotlin
class MainActivity : ComponentActivity()
```

`MainActivity`는 안드로이드 앱 화면 진입의 기본 단위인 Activity다.

이 프로젝트에서는 이 액티비티가 다음 일을 한다.

- 앱 시작 시 최초 진입점 역할
- Compose UI 루트 생성
- 전체 앱 테마 적용 시작

### 9.2 `onCreate()`

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        PrototypeTheme {
            PrototypeApp()
        }
    }
}
```

여기서는 세 줄이 핵심이다.

#### `super.onCreate(savedInstanceState)`

- 부모 액티비티 초기화
- 안드로이드 생명주기 시작 과정의 표준 호출

#### `enableEdgeToEdge()`

- 시스템 상태바/내비게이션바 영역까지 앱이 그릴 수 있게 도와주는 설정
- 최근 안드로이드 UI에서 자주 쓰는 패턴

쉽게 말하면 "화면을 더 넓게 쓰는 모드"에 가깝다.

#### `setContent { ... }`

- Compose UI의 시작점
- 예전 XML 방식의 `setContentView(R.layout....)`와 비슷한 역할
- 하지만 XML 레이아웃 대신 Compose 함수를 넣는다

그리고 그 안에서 `PrototypeTheme`로 감싼다.  
즉, 앱 UI는 시작부터 테마 안에서 그려진다.

### 9.3 `PrototypeApp()`

이 함수는 현재 샘플 앱의 실제 화면 구조다.

핵심 요소는 다음과 같다.

- `rememberSaveable`
- `mutableStateOf`
- `NavigationSuiteScaffold`
- `Scaffold`
- `Greeting`

### 9.4 `rememberSaveable`과 상태(state)

```kotlin
var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
```

이 한 줄은 Compose 초보자에게 매우 중요하다.

뜻을 쪼개 보면 다음과 같다.

- `mutableStateOf(...)`
  - Compose가 관찰하는 상태 값을 만든다.
  - 값이 바뀌면 관련 UI가 다시 그려질 수 있다.
- `rememberSaveable`
  - 상태를 단순히 기억하는 것에서 한 단계 더 나아가, 구성 변경 등에 대비해 저장 가능한 형태로 유지하려고 시도한다.
- `by`
  - 상태 객체를 직접 다루지 않고 일반 변수처럼 쓰게 해주는 Kotlin 위임 문법

즉, 이 코드는 "현재 선택된 메뉴가 무엇인지 기억하는 상태"를 만든다.

### 9.5 `AppDestinations`

```kotlin
enum class AppDestinations(
    val label: String,
    val icon: Int,
)
```

이 enum은 내비게이션 항목 목록이다.

현재 항목은 세 개다.

- `HOME`
- `FAVORITES`
- `PROFILE`

각 항목은 다음 정보를 가진다.

- 사용자에게 보여줄 라벨 문자열
- 아이콘 리소스 ID

즉, 이 enum은 "앱의 메뉴 정의서" 역할을 한다.

### 9.6 `NavigationSuiteScaffold`

이 컴포넌트는 이름 그대로 **적응형(adaptive) 내비게이션 레이아웃용 Scaffold**다.

코드에서는 `AppDestinations.entries.forEach`로 각 메뉴를 순회하면서 항목을 만든다.

```kotlin
item(
    icon = {
        Icon(
            painterResource(it.icon),
            contentDescription = it.label
        )
    },
    label = { Text(it.label) },
    selected = it == currentDestination,
    onClick = { currentDestination = it }
)
```

여기서 배우면 좋은 포인트:

- `painterResource(it.icon)`
  - `R.drawable.ic_home` 같은 리소스를 Compose에서 읽어온다.
- `contentDescription`
  - 접근성용 설명이다.
  - 화면을 음성으로 읽어주는 도구가 이 값을 활용할 수 있다.
- `selected`
  - 지금 선택된 메뉴인지 여부
- `onClick`
  - 누르면 상태를 바꾼다

중요한 점은, **현재 템플릿은 메뉴 선택 상태만 바뀌고 실제 화면 전환은 하지 않는다**는 것이다.  
즉, 아직 내비게이션 구조는 "겉모양 샘플" 수준이다.

### 9.7 `Scaffold`

`Scaffold`는 Material 레이아웃 뼈대다.

현재 코드는 다음처럼 되어 있다.

```kotlin
Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Greeting(
        name = "Android",
        modifier = Modifier.padding(innerPadding)
    )
}
```

핵심 의미:

- 화면 전체를 채우는 레이아웃 컨테이너
- `innerPadding`을 자식에게 전달
- 자식이 Scaffold가 관리하는 영역과 겹치지 않도록 도와줌

### 9.8 `Greeting()`

```kotlin
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
```

이 함수는 Compose의 가장 단순한 예제다.

- 문자열을 받아서
- `Text`로 출력한다

현재 앱에 들어 있는 실제 사용자 콘텐츠는 사실상 이것뿐이다.

### 9.9 `@Preview`와 `@PreviewScreenSizes`

현재 파일에는 미리보기용 어노테이션이 두 종류 있다.

- `@PreviewScreenSizes`
  - 여러 화면 크기에서 Compose 미리보기를 보려는 의도
- `@Preview(showBackground = true)`
  - 배경이 있는 단일 프리뷰를 보기 위한 기본 어노테이션

이들은 앱 런타임 기능이 아니라 **Android Studio 디자인 미리보기용 도구**다.

---

## 10. 안드로이드 리소스 파일 해설

### 10.1 `AndroidManifest.xml`

Manifest는 앱 선언서다.

현재 핵심 내용은 다음과 같다.

- 앱 아이콘 설정
- 앱 이름 연결
- 백업 규칙 연결
- 앱 기본 테마 연결
- 런처 액티비티 설정

특히 다음 두 줄이 중요하다.

- `android:name=".MainActivity"`
- `android.intent.category.LAUNCHER`

이 조합 때문에 앱 실행 시 `MainActivity`가 첫 화면이 된다.

### 10.2 `strings.xml`

현재는 다음만 있다.

- `app_name = Prototype`

안드로이드에서는 사용자에게 보여줄 문자열을 코드에 직접 하드코딩하기보다  
`strings.xml`에 넣는 것이 일반적이다.

그 이유는 다음과 같다.

- 다국어 지원이 쉬워짐
- 문자열 관리가 쉬워짐
- 디자이너/기획 변경 대응이 쉬워짐

현재 `Greeting()`의 `"Hello $name!"`는 샘플이라 코드에 직접 들어가 있지만,  
실제 앱에서는 문자열 리소스로 옮기는 경우가 많다.

### 10.3 `drawable/`

여기에는 앱 내부에서 쓸 벡터 아이콘이 들어 있다.

- `ic_home.xml`
- `ic_favorite.xml`
- `ic_account_box.xml`

현재 `AppDestinations`가 이 리소스를 사용한다.

즉, 이 아이콘들은 런처 아이콘이 아니라 **앱 내부 내비게이션용 아이콘**이다.

### 10.4 `mipmap/`

`mipmap-*` 폴더에는 런처 아이콘이 들어 있다.

- `ic_launcher`
- `ic_launcher_round`

보통 초보자가 헷갈리는 차이는 이렇다.

- `drawable`: 화면 내부 이미지/아이콘
- `mipmap`: 홈 화면에 보이는 앱 아이콘

### 10.5 `xml/backup_rules.xml`

이 파일은 백업 규칙용 템플릿이다.

현재는 샘플 주석만 있고 사실상 비어 있다.

의미:

- 어떤 파일/데이터를 백업에 포함할지 정할 수 있음
- 아직 실제 정책은 설정되어 있지 않음

### 10.6 `xml/data_extraction_rules.xml`

이 파일도 백업/복원 관련 템플릿에 가깝다.

현재는 TODO 주석만 있고 구체 설정은 없다.

즉, 둘 다 현재 앱 기능의 핵심은 아니고 템플릿 기본 제공 파일이다.

---

## 11. Gradle 설정 파일 해설

안드로이드 프로젝트를 이해할 때 Kotlin 코드만 보면 반쪽짜리다.  
빌드 설정을 같이 이해해야 전체 그림이 보인다.

### 11.1 루트 `build.gradle.kts`

현재 내용은 매우 단순하다.

- 루트 레벨에서 플러그인 alias를 선언
- 실제 적용은 각 모듈에서 하게 함

즉, "프로젝트 공통 진입 선언" 정도로 이해하면 된다.

### 11.2 `settings.gradle.kts`

이 파일은 프로젝트 전체의 Gradle 구조를 정한다.

핵심 포인트:

- 플러그인 저장소 설정
- 의존성 저장소 설정
- 프로젝트 이름 설정
- 포함 모듈 선언

현재 모듈은 하나다.

- `include(":app")`

즉, 아직 멀티모듈 구조는 아니다.

또한 다음 설정도 중요하다.

- `RepositoriesMode.FAIL_ON_PROJECT_REPOS`

이건 모듈별로 제각각 저장소를 선언하지 말고,  
저장소 관리를 중앙에서 하겠다는 의미다.

### 11.3 `app/build.gradle.kts`

이 파일은 현재 앱 모듈에서 가장 중요한 설정 파일이다.

#### 플러그인

- Android application 플러그인
- Kotlin Compose 플러그인

#### SDK 관련 설정

- `namespace = "com.example.prototype"`
- `applicationId = "com.example.prototype"`
- `compileSdk = 37`
- `minSdk = 33`
- `targetSdk = 37`

각 의미는 다음과 같다.

- `namespace`
  - 코드에서 생성되는 `R` 클래스 등의 네임스페이스 기준
- `applicationId`
  - 앱의 고유 패키지 식별자
- `compileSdk`
  - 어떤 안드로이드 SDK 기준으로 컴파일할지
- `minSdk`
  - 앱이 설치 가능한 최소 안드로이드 버전
- `targetSdk`
  - 어떤 안드로이드 동작 기준에 맞춰 최적화/검증할지

초보자가 꼭 기억할 점:

- `minSdk = 33`은 생각보다 높은 값이다.
- 즉, 구형 기기 호환성보다는 최신 안드로이드 기준 템플릿에 가깝다.

#### 버전 정보

- `versionCode = 1`
- `versionName = "1.0"`

템플릿 초기값이라고 보면 된다.

#### 빌드 타입

현재 `release` 빌드에서 난독화/최적화 계열 설정은 꺼져 있다.

```kotlin
release {
    optimization {
        enable = false
    }
}
```

즉, 아직 배포용 최적화보다 개발 시작 상태에 가깝다.

#### Java 호환성

- `sourceCompatibility = JavaVersion.VERSION_11`
- `targetCompatibility = JavaVersion.VERSION_11`

즉, 현재 모듈 코드는 Java 11 호환 기준으로 컴파일된다.

#### Compose 활성화

- `buildFeatures { compose = true }`

이 한 줄이 Compose UI 프로젝트라는 사실을 분명하게 보여준다.

### 11.4 의존성(dependencies)

현재 의존성은 거의 Compose 기본 묶음이다.

#### 핵심 런타임

- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`

#### Compose UI

- `androidx.compose.ui:ui`
- `androidx.compose.ui:ui-graphics`
- `androidx.compose.ui:ui-tooling-preview`

#### Material 3

- `androidx.compose.material3:material3`
- `androidx.compose.material3:material3-adaptive-navigation-suite`

여기서 `adaptive-navigation-suite`가 들어 있다는 점 때문에  
기본 템플릿이 단순 텍스트 화면이 아니라 적응형 내비게이션 샘플을 포함하게 된 것으로 볼 수 있다.

#### 테스트

- `junit`
- `androidx.test.ext:junit`
- `espresso-core`
- `compose-ui-test-junit4`

#### 디버그 전용

- `ui-tooling`
- `ui-test-manifest`

초보자가 알아두면 좋은 점:

- `implementation`
  - 앱 실행에 필요한 일반 의존성
- `testImplementation`
  - 로컬 JVM 테스트용
- `androidTestImplementation`
  - 실제 기기/에뮬레이터 테스트용
- `debugImplementation`
  - 디버그 빌드에서만 필요한 도구

### 11.5 `gradle/libs.versions.toml`

이 파일은 **버전 카탈로그(version catalog)** 다.

장점은 다음과 같다.

- 라이브러리 버전을 한 군데에서 관리 가능
- `build.gradle.kts`가 짧고 읽기 쉬워짐
- 여러 모듈이 생겨도 버전 일관성을 유지하기 좋음

현재 확인되는 주요 버전:

- AGP `9.3.1`
- Kotlin `2.2.10`
- Compose BOM `2025.12.00`

즉, 템플릿 자체는 비교적 최신 계열 구성을 따르고 있다.

### 11.6 `gradle.properties`

이 파일에는 Gradle 동작 관련 전역 설정이 있다.

중요한 항목:

- `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`
- `org.gradle.configuration-cache=true`
- `kotlin.code.style=official`

특히 `configuration-cache=true`는 빌드 속도 최적화에 도움을 주는 설정이다.

### 11.7 `gradle/gradle-daemon-jvm.properties`

이 파일은 Gradle 데몬 JVM 관련 자동 생성 설정이다.

여기에는 `toolchainVersion=25`가 보인다.  
이 값은 Gradle 데몬이 사용할 JVM 선택과 관련된 정보로 볼 수 있다.

초보자가 여기서 꼭 구분해야 할 점:

- Gradle이 어떤 JVM에서 실행되느냐
- 앱 소스가 어떤 Java 호환성으로 컴파일되느냐

이 둘은 완전히 같은 개념이 아니다.

현재 소스 호환성은 `app/build.gradle.kts` 기준으로 Java 11이다.

---

## 12. 테스트 파일 해설

### 12.1 `ExampleUnitTest.kt`

현재 테스트는 다음 한 줄이 핵심이다.

- `assertEquals(4, 2 + 2)`

즉, 실제 앱 로직 검증이 아니라  
"로컬 단위 테스트 파일이 이런 형태다"를 보여주는 샘플이다.

### 12.2 `ExampleInstrumentedTest.kt`

이 테스트는 안드로이드 실행 환경에서 돌아간다.

현재는 앱 컨텍스트의 패키지명이 맞는지 확인한다.

- 기대값: `com.example.prototype`

이 역시 샘플에 가깝다.

즉, 현재 테스트 구조는 갖춰져 있지만  
실제 기능 테스트는 아직 시작되지 않은 상태다.

---

## 13. 초보자가 가장 헷갈리기 쉬운 포인트 정리

### 13.1 왜 XML 레이아웃 파일이 거의 없지?

Compose 기반 프로젝트이기 때문이다.  
화면은 XML이 아니라 Kotlin 함수로 만든다.

### 13.2 왜 `themes.xml`이 있는데 또 `Theme.kt`가 있지?

둘 다 테마지만 계층이 다르다.

- `themes.xml`은 안드로이드 시스템/액티비티 레벨
- `Theme.kt`는 Compose UI 레벨

### 13.3 왜 `colors.xml`을 고쳐도 화면이 안 바뀔 수 있지?

현재 Compose 테마는 `Color.kt`를 본다.  
게다가 동적 색상이 켜져 있으면 `Color.kt`조차 바로 반영되지 않을 수 있다.

### 13.4 왜 `Color.kt`를 고쳐도 색이 안 바뀔 수 있지?

`dynamicColor = true`라서 시스템 색상이 우선 적용될 수 있기 때문이다.

### 13.5 왜 메뉴를 눌러도 다른 화면으로 안 가지?

현재 템플릿은 `currentDestination` 상태만 바꾸고,  
선택값에 따라 다른 Composable을 그리는 로직은 아직 없다.

즉, 진짜 화면 전환이 아니라 샘플 내비게이션 UI만 있는 상태다.

### 13.6 왜 `build/` 폴더 파일이 이렇게 많지?

빌드 결과물, 중간 산출물, 테스트 리포트가 자동 생성되기 때문이다.  
보통 직접 수정할 대상이 아니다.

---

## 14. 지금 이 템플릿에서 보이는 "템플릿 흔적"들

현재 프로젝트에는 아직 정리되지 않은 템플릿 흔적이 조금 있다.

- `Theme.kt`에 사용되지 않는 import가 있다.
- `MainActivity.kt`에도 사용되지 않는 import가 있다.
- `colors.xml`은 현재 소스에서 참조되지 않는다.
- `Greeting()`은 대표적인 샘플 함수다.
- 테스트 두 개도 샘플 테스트다.
- 백업 규칙 XML도 거의 비어 있다.

이 말은 곧 다음 뜻이기도 하다.

- 지금 구조를 이해한 뒤, 필요 없는 샘플 코드는 천천히 정리해도 된다.

---

## 15. `Theme.kt`를 실제로 수정할 때 어떻게 접근하면 좋은가

### 15.1 브랜드 색을 고정하고 싶다면

현재처럼 동적 색상이 켜져 있으면 기기마다 색감이 달라질 수 있다.  
만약 앱 고유의 브랜드 색을 강하게 유지하고 싶다면 다음 선택지를 생각할 수 있다.

#### 방법 1. `dynamicColor = false`로 사용

예를 들어 루트에서 이렇게 호출하면 된다.

```kotlin
PrototypeTheme(dynamicColor = false) {
    PrototypeApp()
}
```

그러면 `Theme.kt`의 정적 `LightColorScheme` / `DarkColorScheme`가 실제로 사용될 가능성이 커진다.

#### 방법 2. 정적 ColorScheme를 더 풍부하게 정의

예를 들어 다음 역할들까지 채우면 디자인 통제력이 커진다.

- `background`
- `surface`
- `onPrimary`
- `onBackground`
- `onSurface`

### 15.2 다크 모드와 라이트 모드를 다르게 설계하고 싶다면

현재는 각 모드에서 3개 역할만 바꾸고 있다.  
실제 앱에서는 보통 다음까지 함께 생각한다.

- 배경색
- 카드/시트 표면색
- 텍스트 대비색
- 에러 색상
- 테두리/분리선 계열 색상

### 15.3 타이포그래피도 같이 맞춰야 한다

테마는 색상만 바꾸는 작업이 아니다.

현재 `Type.kt`는 거의 기본 상태이므로,  
앱 분위기를 바꾸려면 다음도 함께 손대는 경우가 많다.

- 제목 크기
- 본문 크기
- 버튼 글자 스타일
- 자간(letterSpacing)
- 줄간격(lineHeight)

### 15.4 `MaterialTheme`를 사용하는 습관이 중요하다

실제 화면을 만들 때 색상과 글꼴을 직접 하드코딩하기보다  
가능하면 `MaterialTheme`를 통해 가져오는 습관이 좋다.

예를 들면 이런 식이다.

```kotlin
Text(
    text = "예시",
    color = MaterialTheme.colorScheme.primary,
    style = MaterialTheme.typography.bodyLarge
)
```

이렇게 해야 테마를 바꿨을 때 화면 전체 일관성이 유지된다.

---

## 16. 초보자 기준으로 앞으로 어디부터 손대면 좋은가

추천 순서는 다음과 같다.

1. `Greeting()` 대신 실제 첫 화면 Composable 하나 만들기
2. `"Hello Android!"` 같은 하드코딩 문자열을 `strings.xml`로 옮기기
3. `AppDestinations` 선택값에 따라 다른 화면을 그리도록 분기 추가하기
4. `Theme.kt`의 동적 색상 사용 여부를 의도적으로 결정하기
5. `Type.kt`와 `Color.kt`를 앱 브랜드에 맞게 정리하기
6. 필요 없는 템플릿 흔적 정리하기

특히 초보자에게는 3번이 좋은 연습이다.  
지금은 메뉴를 눌러도 본문이 바뀌지 않으므로, `currentDestination` 값에 따라 다른 Composable을 보여주게 만들면 Compose 상태 개념을 이해하기 좋다.

---

## 17. 이 프로젝트에서 당장 기억하면 좋은 용어 사전

### Activity

안드로이드 앱의 화면 단위 진입점.  
현재는 `MainActivity` 하나만 있다.

### Composable

Compose에서 화면 조각을 만드는 함수.  
예: `PrototypeTheme`, `PrototypeApp`, `Greeting`

### State

화면이 기억하고 반응해야 하는 값.  
예: `currentDestination`

### Recomposition

상태가 바뀌었을 때 필요한 UI를 다시 그리는 과정.

### Resource

문자열, 색상, 아이콘, 레이아웃처럼 `res/` 아래에 관리되는 자원.  
예: `R.drawable.ic_home`, `R.string.app_name`

### Theme

색상, 글꼴, 모양 같은 공통 디자인 규칙 묶음.

### Preview

앱을 실제 실행하지 않고 Android Studio 안에서 미리 화면을 보는 기능.

---

## 18. 아주 짧은 결론

현재 `Prototype` 프로젝트는 **최신 Compose 안드로이드 템플릿을 거의 그대로 유지한 상태**다.  
그리고 `Theme.kt`는 그 안에서 **앱 전역의 색상 체계와 타이포그래피 적용을 담당하는 핵심 파일**이다.

특히 지금 구조에서 가장 중요한 사실은 이것이다.

- `Theme.kt`가 Compose 테마의 중심이다.
- `themes.xml`은 별도의 시스템 레벨 역할을 가진다.
- `dynamicColor = true`와 `minSdk = 33` 조합 때문에, 정적 색상 정의가 바로 눈에 안 보일 수 있다.

이 세 가지만 정확히 이해해도 이 템플릿 프로젝트를 읽는 난이도가 크게 내려간다.
