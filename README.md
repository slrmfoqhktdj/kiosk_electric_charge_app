[개요] 과충전 방지 전기차 충전기(2023.10~2024.06, 아이젠)의 사용자 화면 앱.
       커넥터 2개(좌/우)를 선택해 결제하고 충전을 시작하는 키오스크 단말.

[사용기술]
 - Java, Android (minSdk 24, compileSdk/targetSdk 33)
 - OCPP 1.6 클라이언트: Java-OCA-OCPP(ChargeTimeEU) + OkHttp WebSocket
 - Retrofit 2.9 + Gson, Glide, ViewPager, ViewModel/LiveData
 - 결제 단말(NVCAT) 연동: 외부 결제 앱 Intent 호출/결과 수신
 - 키오스크 운영: Device Admin(KioskDeviceAdminReceiver), 부팅 시 자동 실행(BootCompleteReceiver),
   HOME 런처 카테고리 등록, Foreground/Background 서비스, APK 자동 업데이트
 - 광고: 이미지/영상 혼합 재생 뷰(ImageVideoView)

[주요기능]
 - 화면 흐름(Fragment): 충전구 선택(ChargeSelect) → 결제수단 선택(PaySelect: 카드/QR/RFID)
   → 결제 입력(InputPay: 금액/충전량) → 결제(Pay) → 충전 정보(ChargeInfo)
 - NVCAT 연동 결제: 카드 승인/취소, QR 읽기·QR 결제, RFID 잔액 조회·결제, 카드 삽입 확인,
   NVCAT 재시작(오류 복구) — Intent extra("NVCATSENDDATA")로 요청, 결과코드/데이터로 수신
 - OCPP 통신: BootNotification, Heartbeat, Authorize, StartTransaction, StopTransaction,
   MeterValues, StatusNotification, DataTransfer(결제정보 저장 등)
 - 서버 원격 명령 수신 시 화면 반영(가용성 변경 등), 서버 연결 끊김 다이얼로그
 - 서버에서 광고 목록 조회·다운로드 후 반복 재생, 날씨 정보 표시/보고용 모델
 - APK 버전 확인 → 다운로드 → 설치 → 서버에 업데이트 완료 보고

[핵심 시스템 구조와 구현 방식]
 - SplashActivity(초기화·권한·Device Admin·업데이트 확인·서버 연결) → CenterActivity(중앙
   제어, 약 7만 자) 가 ViewPager의 Fragment들과 JsonClient(OCPP 클라이언트)를 조율.
 - 화면 간 상태 공유는 MainViewModel(LiveData: 결제금액, 선택 커넥터, 카드 상태 등),
   서비스/컴포넌트 간 이벤트는 Intent 브로드캐스트와 콜백 인터페이스 사용.
 - JsonClient(약 3만 자): OCPP 요청 생성/전송과 서버 원격 요청 핸들러를 한 클래스에 구성하고
   WebSocket 재연결 처리. 결제 승인번호를 OCPP idTag처럼 사용해 Authorize/Start 흐름에 연결.
 - 결제 결과/장애 시 NVCAT 재시작 후 첫 화면 복귀 등 현장 복구 시나리오를 코드에 반영.
 - 시리얼 통신 라이브러리(android_serialport_api)가 포함돼 있으나, 이 앱에서는 충전 제어용
   PLC 통신이 주된 흐름으로 보이지 않음(PLC 통신은 18번 폴 앱에 구현, 확인 필요).

[데이터 흐름]
 사용자 → 커넥터 선택 → 결제수단 선택 → NVCAT(Intent) 결제 승인 → 승인번호 획득
   → OCPP Authorize/StartTransaction → Go CSMS(4번) → 충전 시작
   충전 중 MeterValues/StatusNotification 전송, 종료 시 StopTransaction + DataTransfer(결제정보)
 서버 → 광고/APK 파일(Retrofit) 다운로드 → 단말에서 재생/설치


 <img width="2992" height="2992" alt="KakaoTalk_20250417_163948276_04" src="https://github.com/user-attachments/assets/93655b49-959f-4a44-bbbf-43b11bb431a3" />
