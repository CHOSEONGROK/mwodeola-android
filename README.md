# 뭐더라 앱
 
![뭐더라 스크린샷](https://user-images.githubusercontent.com/33805423/204464500-eb380c94-640d-413e-a491-f980c8b5f7d6.png)


## 서비스 소개

파편화된 웹 서비스 계정들의 아이디/비밀번호를 통합 저장, 관리하기 위한 안드로이드 앱 서비스입니다.

(스마트폰 사용에 어려움을 겪는 부모님을 도와드릴 때도 유용합니다.)

### 특징
- 웹 서버를 사용하여 영구적인 데이터 보관이 가능합니다.
- 다양한 CustomView, Motion/Animator 등을 적극 활용하여 현대적인 UI/UX 를 구현하였습니다.
- AutofillService 등 고객 입장에서의 사용성을 위해 끊임없이 고민하고 기능 개발을 하였습니다.

[뭐더라 앱 관련 UI/UX 개발 보러가기](https://jsl663.tistory.com/49)


<br/>

## 주요 기능

- **회원 기능**
  - 회원 가입: 휴대폰 번호(인증 필요), 이름, 이메일, 마스터 비밀번호(숫자4 + 영문자1)
  - 로그인
    - 토큰을 이용한 자동 로그인
    - 휴대폰 번호 인증 후 마스터 비밀번호 인증
- **계정(아이디/비밀번호) 관리**
  - 계정 생성
    - 웹 서비스마다 아이디/비밀번호 여러개 생성 가능
    - SNS 아이디로 가입된 계정도 만들 수 있음
    - 총 4가지 비밀번호 유형 지원(평문 비밀번호, PIN4, PIN6, 패턴 암호)
  - 계정 수정
  - 계정 삭제
- **자동 완성 서비스**
  - Android AutofillFramework 기술을 이용하여 아이디/비밀번호 자동 입력 서비스 기능 구현
    - 타 앱 회원 가입시: 가입 화면에서 작성한 아이디와 비밀번호, 앱 이름, 패키지명을 통해 자동으로 뭐더라 서버에 계정 생성
    - 타 앱 로그인시: 아이디 혹은 비밀번호 입력 EditText 활성화시 해당 앱 계정으로 저장된 아이디/비밀번호 자동 입력 기능
- **보안**
  - 휴대폰 번호 인증
  - 비밀번호 인증 횟수 제한
  - 앱 화면 잠금
  - 민감 정보 확인시 앱 내의 이중 잠금(ex. 패스워드 토글 클릭시 추가 인증 필요)
  - 캡쳐 방지
  - Android Keystore, KeyGenerator


<br/>

<details>
<summary>미구현(구현 예정)</summary>
<div markdown="1">

- 회원
    - [ ] 가족 회원 관리
- 계정 관리
    - [ ] 계정 썸네일 아이콘 생성 유형 다형화
    - [ ] SNS 계정과 연결된 일반 계정 목록 확인
    - [ ] 초기 가입자를 위해 여러 계정 동시 생성 기능
    - [ ] 계정 이름에 따른 웹 사이트 주소 자동 서치
- 금융 정보 등 다양한 데이터 유형 지원
- 보안
    - [ ] HTTPS: 서버 문제 미해결
    - [ ] 이메일 인증
    - [ ] End to End 암호화

</div>
</details>


<br/>

## 사용된 기술

- Kotlin, MVP
- Retrofit2, Okhttp3, Room
- Lifecycle, Navigation, PreferenceFragment
- Material Design UI, Motion/Animator
- CustomView
- AutofillFramework
- 문자 인증(Firebase Authentication, SmsRetriever, SafetyNet)
- 화면 잠금 비밀번호 암호화(Lifecycle, Keystore, SHA256withECDSA)
- 계정 검색 - Fuzzy matching 알고리즘


<br/>

## 앱 주요 화면

![뭐더라_주요화면_1](https://user-images.githubusercontent.com/33805423/204490344-66b026c1-1ae6-4eb0-a7ce-c3ae1b03ea27.png)

![뭐더라_주요화면_3](https://user-images.githubusercontent.com/33805423/204498414-796f2d81-71cc-4689-889a-6958d798ef57.png)


<br/>

## 시연 영상 - 유튜브

[![Video Label](https://img.youtube.com/vi/JI3wjMUU__s/0.jpg)](https://youtu.be/JI3wjMUU__s)

⬆️ 위 이미지 클릭 시 유튜브 동영상으로 연결됩니다.
