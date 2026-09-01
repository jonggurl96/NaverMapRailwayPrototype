# Compose Android 프로젝트 리팩토링

## 리팩토링 기준

이 프로젝트는 단일 `app` 모듈 안에서 **기능(feature) 중심 패키지**와 공용 계층을 함께 사용하는 구조로 정리했다. 화면과 해당 화면 전용 컴포넌트는 `feature` 아래에 모으고, 파일·네트워크 같은 데이터 접근은 `data`, 여러 기능에서 재사용 가능한 Compose UI는 `core/ui`, 앱 전체 조합과 테마는 `ui`가 담당한다.

의존 방향은 다음을 기준으로 한다.

```text
MainActivity -> ui -> feature -> data
                         |
                         +-> core/ui
```

- `MainActivity`는 Android 생명주기와 위치 권한/플랫폼 API만 처리한다.
- `ui`는 앱 수준 Compose 루트와 테마를 제공한다.
- `feature/map`은 지도 기능의 모델, 화면, 전용 컴포넌트를 소유한다.
- `data`는 GeoJSON과 기상 API 같은 데이터 획득·변환 책임을 가진다.
- `core/ui`는 특정 기능에 종속되지 않는 재사용 컴포넌트를 제공한다.

## 패키지 구조

```text
com.example.prototype
├── MainActivity.kt
├── core
│   └── ui
│       └── component
│           └── SliderSwitch.kt
├── data
│   ├── local
│   │   └── geojson
│   │       └── GeoJsonParser.kt
│   └── remote
│       └── forecast
│           ├── ForecastRepository.kt
│           └── UltraShortTermForecast.kt
├── feature
│   └── map
│       ├── model
│       │   └── MapStyleOption.kt
│       └── ui
│           ├── MapScreen.kt
│           └── component
│               ├── ExpandableFabMenu.kt
│               └── MapControls.kt
└── ui
    ├── PrototypeApp.kt
    └── theme
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## 파일별 역할

### 앱 진입점과 앱 UI

| 파일 | 역할 |
| --- | --- |
| `MainActivity.kt` | Activity 생명주기, 위치 권한 요청, `FusedLocationProviderClient` 호출을 담당하고 Compose 콘텐츠를 시작한다. 화면 UI는 포함하지 않는다. |
| `ui/PrototypeApp.kt` | 앱의 최상위 `Scaffold`, 지도 스타일·레이어·현재 위치 UI 상태를 관리하고 지도 화면과 메뉴를 조합한다. 위치 토글을 끌 때 불필요한 위치 재조회 없이 상태만 초기화한다. |

### 지도 기능

| 파일 | 역할 |
| --- | --- |
| `feature/map/model/MapStyleOption.kt` | 기본·위성·다크 지도의 표시 이름, Naver `MapType`, 다크 모드 여부를 하나의 enum 모델로 표현한다. 기존 Java식 getter 기반 `MapTypeMetadata`를 대체한다. |
| `feature/map/ui/MapScreen.kt` | Naver Compose Map을 표시하고 카메라, 현재 위치 마커, GeoJSON 기반 철도 마커·선·폴리곤 오버레이를 렌더링한다. |
| `feature/map/ui/component/ExpandableFabMenu.kt` | 지도 옵션 패널을 열고 닫는 확장형 Floating Action Button을 제공한다. |
| `feature/map/ui/component/MapControls.kt` | 지도 스타일 선택기와 날씨·철도·위치 레이어 스위치 등 지도 전용 메뉴 UI를 제공한다. |

### 공용 UI

| 파일 | 역할 |
| --- | --- |
| `core/ui/component/SliderSwitch.kt` | 기능과 무관하게 재사용 가능한 애니메이션 토글 스위치 Composable이다. |

### 데이터

| 파일 | 역할 |
| --- | --- |
| `data/local/geojson/GeoJsonParser.kt` | GeoJSON의 Point, LineString, Polygon, MultiPolygon 모델과 JSON을 해당 모델로 변환하는 파서를 제공한다. 앱 assets의 철도 데이터를 지도 좌표로 바꾼다. |
| `data/remote/forecast/UltraShortTermForecast.kt` | `BASE_URL`과 `API_KEY`로 기상청 초단기예보 API를 호출하고, 받은 XML 스트림을 `parse`로 변환한다. 예보 카테고리/DTO/API 결과와 Retrofit 서비스 정의도 포함하며 아직 지도 화면과는 연결되지 않았다. |
| `data/remote/forecast/ForecastRepository.kt` | 앱 실행 중 매시간 50분에 초단기예보를 호출하고, 파싱된 최신 `ForecastApiResult`를 `StateFlow`에 메모리 보관한다. 실패 시 이전 값을 유지하고 다음 시간에 다시 시도한다. |

### 디자인 시스템

| 파일 | 역할 |
| --- | --- |
| `ui/theme/Color.kt` | 앱에서 사용하는 기본 색상 토큰을 정의한다. |
| `ui/theme/Theme.kt` | 라이트·다크·동적 색상과 MaterialTheme 적용을 담당한다. |
| `ui/theme/Type.kt` | Material 3 Typography 설정을 정의한다. |

## 주요 변경 사항

- 비대했던 `MainActivity.kt`에서 Compose 앱과 지도 컨트롤 UI를 분리했다.
- 구현 형태 중심의 `map/fab`, `map/layers`, `map/ui/checkbox`, `vo` 패키지를 책임 중심 패키지로 교체했다.
- `MapTypeMetadata`를 불변 enum인 `MapStyleOption`으로 변경해 인덱스/라벨/지도 설정을 한곳에서 관리한다.
- 공용 스위치와 지도 전용 컨트롤을 분리해 재사용 경계를 명확히 했다.
- 로컬 GeoJSON과 원격 기상 API 코드를 각각 `data/local`, `data/remote`로 구분했다.
- 사용처가 없고 잘못된 구성으로 APK 패키징 충돌을 만들던 Hilt 플러그인 및 의존성을 제거했다. 추후 DI 도입 시 KSP 또는 kapt 설정과 함께 다시 추가해야 한다.

## 후속 확장 권장 사항

- 화면 상태와 위치 요청이 커지면 `MapViewModel`과 불변 `MapUiState`를 추가한다.
- 기상 API를 실제 연결할 때 DTO를 화면 모델로 변환하는 repository를 두고 API 키는 소스가 아닌 `local.properties` 또는 안전한 서버에서 주입한다.
- GeoJSON 로딩을 repository로 감싸면 `MapScreen`에서 파일 I/O를 제거하고 테스트하기 쉬워진다.
