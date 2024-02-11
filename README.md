# <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/b319291f-ec97-419b-949f-c6a7e3834c32" width="50" height="50"/> 맛.ZIP (위치기반 부산 맛집 공유 커뮤니티 앱)
![image](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/f65ff2dc-eddb-4f19-89d2-9a2e2cfdcca4)
- 일정 : 2023.11.17.~ 2023.12.01 (업데이트중)
- 배포 URL : (출시 진행중)
- Test ID : ooo@naver.com
- Test PW : 1234
<br>

## 프로젝트 소개
- 넘쳐나는 광고성 맛집 정보에 지친 사람들을 위한 신뢰 기반 부산 맛집 공유 SNS입니다.
- 개인의 프로필 페이지에 나의 맛집에 대한 정보를 작성할 수 있습니다.
- 내 근처 맛집 정보를 지도에 띄워 제공합니다.
- 검색을 통해 다른 유저들의 맛집 정보와 식당상세 정보를 찾아볼 수 있습니다.
- 다양한 유저들을 팔로우하며 마음에 드는 게시글에 좋아요를 누르거나 댓글을 작성 할 수 있습니다.
<br>

## 팀원 구성
| 손주원(팀장) | 김경태 | 서동옥 | 손유영 | 최수연 |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/637dc64e-6e5f-4fa1-b2a7-68a0f9174de0" width="90" height="80"/>| <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/684414ca-d820-4321-9860-dd8ed2dea7ec" width="80" height="80"/> | <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/a2e19a00-19cc-419b-8a26-e1b85be54b4b" width="90" height="90"/> | <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/547f8182-a3a9-4605-8202-1f2fa82f66a7" width="80" height="80"/> | <img src="https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/074fd9af-a75c-461c-b510-4c09f8491bd5" width="90" height="80"/> |
| [@jurwon](https://github.com/jurwon) | [@RomanticToad](https://github.com/RomanticToad) | [@eastok](https://github.com/eastok) | [@sonyuyoung](https://github.com/sonyuyoung) | [@Sophie](https://github.com/ssasdfccaf) |
<br>

## 역할 분담
### :tangerine: 손주원
- 기능 : 회원가입(유효성검사,firebase이미지서버,주소검색), jwt 토큰 로그인, 회원정보수정, 검색, 좋아요/싫어요, 회원탈퇴, 로그아웃
<br>

### :apple: 김경태
- 기능 : 게시글 작성(다중 이미지),게시글 수정&삭제, 게시글목록, 상세 게시글, 팔로우한 유저의 게시글목록
<br>

### :melon: 서동옥
- 기능 : 식당목록 ,식당 추가&삭제(관리자계정), 식당상세페이지, 내근처식당조회, 현 지도에서 재검색(반경설정), 지도에 좌표표시
<br>

### :grapes: 손유영
- 기능 : 내 프로필, 타인 프로필, 팔로우&언팔로우, 랭킹게시판, UI디자인&레이아웃 설계, 로고 splash, 댓글&대댓글
<br>

### :lemon: 최수연
- 기능 : 채팅(읽음표시, 사용자 좌우 분리, 팔로우한 유저 채팅목록, firebase 채팅서버와 db계정 연동)
<br>

## 1. 개발 환경
- Frontend : Android
- Backend : SpringBoot, MySQL, Firebase
- 버전관리 : GitHub
- Tool : Android Studio, Intelij, MySQL Workbench
<br>

## 2. 프로젝트 설계
![image](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/f12b2e6b-0419-452a-97b0-3f4032240fc2)
![image](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/69ea8669-d816-435b-8b2e-4bba5eaf5b80)
<br><br>

## 3. 화면 구성
![image](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/e5002030-483c-4a08-abcc-d55a5e1f0a30)<br>
<br>

## 4. 페이지별 기능
### \[초기화면\]
- 서비스 접속시 보이는 화면으로 splash 화면이 잠시 나온 뒤 다음 페이지가 나타납니다.
  - 자동로그인 되어 있지 않은 경우 : 로그인/회원가입 선택 페이지
  - 자동로그인 되어 있는 경우 : 홈 화면
  <br>
  
  | 초기화면 |
  | :---: |
  | ![초기화면](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/57f3d1c2-085c-41c1-8d22-a7fb97ba3529) |

  <br>

### \[회원가입\]
- 회원가입에 필요한 정보를 입력하고 버튼을 누르면 유효성검사가 진행되고 통과하지 못한 경우 경고 문구가 표시됩니다.
  <br>
  
  | 회원가입 |
  | :---: |
  | ![회원가입](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/c4ed80af-c05d-4bb5-baec-60ec133d5343) |

  <br>

### \[로그인\]
  - 로그인 버튼 클릭 시 이메일 주소 또는 비밀번호가 일치하지 않을 경우 경고 문구가 나타나며 로그인에 성공하면 홈 화면으로 이동합니다.
  - 자동로그인을 체크했다면 어플종료 후 재접속시 홈화면으로 이동하며, 자동로그인을 체크하지 않았다면 재접속시 로그인/회원가입 화면으로 이동합니다.
  <br>
  
  | 자동 로그인 체크한 경우 |
  | :---: |
  | ![로그인_자동저장x_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/04f373e9-24e2-4596-a1ca-e360aeba3cde)
 |

  <br>

  | 자동 로그인 체크하지 않은 경우 |
  | :---: |
  | ![로그인_자동저장_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/86ee9e06-4ed8-4d44-9a61-e5cf625627dd)|

  <br>
  
### \[프로필\]
- 내 프로필에서는 나의 정보, 팔로워목록, 팔로우목록, 게시글 등 나의 정보를 확인할 수 있으며 프로필 수정이 가능합니다.
- 타인의 프로필에서는 그 사람의 팔로워, 팔로우 목록, 게시글 등 그 사람의 정보를 확인할 수 있으며 그 사람에게 채팅,언팔로우,팔로우를 보낼 수 있습니다.
<br>

| 내 프로필 |
| :---: |
| ![프로필](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/2bd64943-8637-4a66-b774-3accacf035bb)|
<br>

| 다른 사람 프로필(+팔로우) |
| :---: |
| ![다른사람프로필_팔로우_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/8fc3680a-183a-466f-bbb9-11d7248ee230) |
<br>

### \[홈\]
- 게시글들이 최신순으로 표시되며 하단 플로팅 버튼 클릭시 내가 팔로우한 유저들의 게시글이 최신순으로 조회됩니다.
<br>

| 게시글 목록(팔로우한 유저 없을 때) |
| :---: |
| ![홈](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/a66bb134-cfeb-4560-9c49-5074d1bf6bb6) |

<br>

| 게시글 목록(팔로우한 유저 있을 때) |
| :---: |
| ![팔로잉한사람 게시글](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/db71ea86-9e6b-47ad-9334-6a09056f43bb) |

<br>

### \[게시글\]
- 유저가 작성한 맛집 평가가 표시되며 좋아요, 싫어요 클릭시 바로 반영됩니다.
- 댓글과 대댓글작성으로 유저들간의 소통 기능을 제공합니다.
- 게시글 상단의 유저 배너 클릭 시 게시글을 작성한 유저의 프로필 페이지로, 식당 클릭 시 식당 상세 페이지로 이동합니다.
  <br>

| 게시글 상세 |
  | :---: |
  | ![게시글상세_댓글-_online-video-cutter com_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/9e4e69fd-95f8-42df-ab14-1bf79c87ac3c) |

<br>

| 게시글 수정 |
  | :---: |
  | ![게시글수정-_online-video-cutter com_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/a6f8298b-a886-4897-a7c4-1388f83c304b) |

<br>

| 게시글 삭제 |
  | :---: |
  | ![게시글삭제-_online-video-cutter com_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/c3c31ef2-f8c0-4a2d-88b5-13c0bc52370c) |

<br>

### \[검색\]
- 게시글의 내용, 제목, 식당의 이름, 메뉴 혹은 유저ID로 검색이 가능합니다.
- 게시글, 식당, 계정 중 원하는 카테고리를 선택하면 각각의 결과가 표시됩니다.
- 검색창에 검색 기록이 표시되며 원하지 않을 시 자동저장 해지할 수 있습니다.
  <br>

| 검색 |
  | :---: |
  | ![검색_최근검색어_3배속_](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/2da32318-aea7-409d-bb78-3fb2e69d5867) |

<br>


### \[식당\]
- 식당목록은 등록순으로 정렬되며, 식당 검색이 가능합니다.
- 식당목록에서 식당 클릭시 상세 정보가 표시되며 약도보기, 식당에 전화걸기 기능이 제공됩니다.
- 상세페이지에서 리뷰보기 클릭시 게시글 목록이 표기됩니다.
  <br>

| 식당 목록 |
  | :---: |
  | ![식당목록](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/93e9d8e2-125a-4d78-accc-ea1e0255267e) |

<br>
- 식당(식당 상세,리뷰보기)

| 식당 상세 |
  | :---: |
  | ![식당상세](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/1a5798d3-95a0-43f3-9710-ad2757fadb95) |

<br>

### \[내 주변 식당 찾기\]
- 현재 위치를 기준으로 맛집이 표기됩니다.
- 범위 조절 후 현 지도에서 재검색 버튼을 클릭하면 좀 더 넓은 혹은 좁은 범위로 맛집을 조회할 수 있습니다.
  <br>

| 내 주변 식당 찾기 |
  | :---: |
  | ![내근처맛집](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/30bf3ceb-ae63-4685-b187-25575fd2185f)|
  
<br>

| 범위 변경 |
  | :---: |
  | ![내근처맛집(범위변경) (1)](https://github.com/jurwon/Find_My_Matzip_Android/assets/35756071/a003272d-edff-4622-bec2-9de4f0e72cbc)|

<br>

<br><br>

## 5. 코드 리뷰(개인 작성 부분)
<br>

## 6. 트러블 슈팅(개인 작성 부분)
<br>

## 7. 개선 목표



