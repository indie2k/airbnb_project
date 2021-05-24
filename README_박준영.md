![image](https://user-images.githubusercontent.com/15603058/119284989-fefe2580-bc7b-11eb-99ca-7a9e4183c16f.jpg)

# 예제 - 숙소예약

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 예시 답안을 포함합니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - 숙소예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

AirBnB 커버하기

기능적 요구사항
1. 호스트가 임대할 숙소를 등록/수정/삭제한다.
2. 고객이 숙소를 선택하여 예약한다.
3. 고객이 결제한다.
4. 예약이 되면 예약 내역이 호스트에게 전달된다.
5. 고객이 예약을 취소할 수 있다.
6. 호스트가 본인의 임대 현황을 조회한다.
7. 예약 사항이 변경될 경우 알림을 보낸다.
8. 사용자가 후기를 남길 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 주문건은 아예 예약이 성립되지 않아야 한다  Sync 호출 
1. 장애격리
    1. 예약관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다  Circuit breaker, fallback
1. 성능
    1. 고객이 자주 예약관리에서 확인할 수 있는 예약상태를 예약시스템(프론트엔드)에서 확인할 수 있어야 한다  CQRS
    1. 예약상태가 바뀔때마다 카톡 등으로 알림을 줄 수 있어야 한다  Event driven


본인 수정 부분이 아니라 삭제



## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/QtpQtDiH1Je3wad2QxZUJVvnLzO2/share/6f36e16efdf8c872da3855fedf7f3ea9


### 이벤트 도출
![image](https://user-images.githubusercontent.com/15603058/119298548-337fda80-bc98-11eb-9f96-7d583d156fb9.png)


### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/15603058/119298594-4f837c00-bc98-11eb-9f67-ec2e882e1f33.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 등록시>RoomSearched, 예약시>RoomSelected :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/15603058/119298993-113a8c80-bc99-11eb-9bae-4b911317d810.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/15603058/119299589-2663eb00-bc9a-11eb-83b9-de7f3efe7548.png)

    - Room, Reservation, Payment, Review 은 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/15603058/119300858-6c21b300-bc9c-11eb-9b3f-c85aff51658f.png)

    - 도메인 서열 분리 
        - Core Domain:  reservation(front), room : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 reservation 의 경우 1주일 1회 미만, room 의 경우 1개월 1회 미만
        - Supporting Domain:   marketing, customer : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   payment : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

![image](https://user-images.githubusercontent.com/15603058/119303664-1b608900-bca1-11eb-8667-7545f32c9fb9.png)

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/15603058/119304604-73e45600-bca2-11eb-8f1d-607006919fab.png)

### 완성된 1차 모형

![image](https://user-images.githubusercontent.com/15603058/119305002-0edd3000-bca3-11eb-9cc0-1ba8b17f2432.png)

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/15603058/119306321-f110ca80-bca4-11eb-804c-a965220bad61.png)

    - 호스트가 임대할 숙소를 등록/수정/삭제한다.(ok)
    - 고객이 숙소를 선택하여 예약한다.(ok)
    - 고객이 결제한다.(ok)
    - 예약이 되면 예약 내역이 호스트에게 전달된다.(?)
    - 고객이 예약을 취소할 수 있다.(ok)
    - 호스트가 본인의 임대 현황을 조회한다.(View-green Sticker 추가로 ok)
    - 예약 사항이 변경될 경우 알림을 보낸다.(ok)
    - 사용자가 후기를 남길 수 있다.(ok)


### 모델 수정

![image](https://user-images.githubusercontent.com/15603058/119307481-b740c380-bca6-11eb-9ee6-fda446e299bc.png)
    
    - 수정된 모델은 모든 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/15603058/119311800-79df3480-bcac-11eb-9c1b-0382d981f92f.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 예약시 결제처리:  결제가 완료되지 않은 예약은 절대 받지 않는다고 결정하여, ACID 트랜잭션 적용. 예약 완료시 사전에 방 상태를 확인하는 것과 결제처리에 대해서는 Request-Response 방식 처리
        - 결제 완료시 Host 연결 및 예약처리:  reservation 에서 room 마이크로서비스로 예약요청이 전달되는 과정에 있어서 room 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 예약상태, 후기처리 등 모든 이벤트에 대해 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.



본인 수정 부분이 아니라 삭제
