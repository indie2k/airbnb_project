![image](https://user-images.githubusercontent.com/15603058/119284989-fefe2580-bc7b-11eb-99ca-7a9e4183c16f.jpg)

# 숙소예약(AirBnB)+렌터카(RentCar)

숙소 예약에 대한 업무 비지니스를 간소화하여 MSA로 구현을 해 보는 것을 목표로 합니다.
본 프로젝트는 팀 프로젝트 결과물을 기반으로 기능이 추가된 개인 프로젝트입니다.


# 서비스 시나리오

AirBnB 커버하기

기능적 요구사항
1. 호스트가 임대할 숙소를 등록/수정/삭제한다.
2. 고객이 숙소를 선택하여 예약한다.
3. 예약과 동시에 결제가 진행된다.
4. 예약이 되면 예약 내역(Message)이 전달된다.
5. 고객이 예약을 취소할 수 있다.
6. 예약 사항이 취소될 경우 취소 내역(Message)이 전달된다.
7. 숙소에 후기(review)를 남길 수 있다.
8. 전체적인 숙소에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다.(viewpage)

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 예약 건은 성립되지 않아야 한다.  (Sync 호출)
1. 장애격리
    1. 숙소 등록 및 메시지 전송 기능이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 예약 시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다  Circuit breaker, fallback
1. 성능
    1. 모든 방에 대한 정보 및 예약 상태 등을 한번에 확인할 수 있어야 한다  (CQRS)
    1. 예약의 상태가 바뀔 때마다 메시지로 알림을 줄 수 있어야 한다  (Event driven)

[개인 프로젝트 추가 요구 사항]
1. 숙소 예약 시 렌터카도 예약할 수 있다.
2. 렌터카를 등록/수정/삭제할 수 있다.
3. 예약이 취소되면 렌터카도 같이 취소된다.
4. 렌터카 예약/취소에 대한 내역도 기존 메시지 서비스로 전달된다.
5. 통합 뷰 페이지에서 렌터카 예약 정보도 같이 확인할 수 있다.(CQRS)

[개인 프로젝트 비기능적 추가 요구 사항]
1. 렌터카 예약시 렌터카 예약 가능 상태를 반드시 확인하여야 한다. (Sync)



# 분석/설계

## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/77129832/119316165-96ca3680-bcb1-11eb-9a91-f2b627890bab.png)

## TO-BE 조직 (Vertically-Aligned)  
  ![image](https://user-images.githubusercontent.com/77129832/119315258-a09f6a00-bcb0-11eb-9940-c2a82f2f7d09.png)


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
        - Core Domain:  reservation, room : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 reservation 의 경우 1주일 1회 미만, room 의 경우 1개월 1회 미만
        - Supporting Domain:   message, viewpage : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   payment : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 

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
    - 예약과 동시에 결제가 진행된다.(ok)
    - 예약이 되면 예약 내역(Message)이 전달된다.(?)
    - 고객이 예약을 취소할 수 있다.(ok)
    - 예약 사항이 취소될 경우 취소 내역(Message)이 전달된다.(?)
    - 숙소에 후기(review)를 남길 수 있다.(ok)
    - 전체적인 숙소에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다.(View-green Sticker 추가로 ok)
    
### 모델 수정

![image](https://user-images.githubusercontent.com/15603058/119307481-b740c380-bca6-11eb-9ee6-fda446e299bc.png)
    
    - 수정된 모델은 모든 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/15603058/119311800-79df3480-bcac-11eb-9c1b-0382d981f92f.png)

- 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
- 고객 예약시 결제처리:  결제가 완료되지 않은 예약은 절대 받지 않는다고 결정하여, ACID 트랜잭션 적용. 예약 완료시 사전에 방 상태를 확인하는 것과 결제처리에 대해서는 Request-Response 방식 처리
- 결제 완료시 Host 연결 및 예약처리:  reservation 에서 room 마이크로서비스로 예약요청이 전달되는 과정에 있어서 room 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
- 나머지 모든 inter-microservice 트랜잭션: 예약상태, 후기처리 등 모든 이벤트에 대해 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.

### [개인프로젝트]추가 요구사항으로 인한 모델 추가 수정
- 렌터카 관련 추가 요구 사항으로 인하여 신규 RENTCAR 모델이 추가
- 기존 VIEW페이지 항목 및 예약 서비스 등 변경사항 발생

![image](https://user-images.githubusercontent.com/31723044/120928399-71acdd80-c71f-11eb-96ba-614024d69abe.png)


### [개인프로젝트추가] 추가 요구 사항에 대한 검증

1. 숙소 예약 시 렌터카도 예약할 수 있다.                       - (OK)
2. 렌터카를 등록/수정/삭제할 수 있다.                          - (OK)
3. 예약이 취소되면 렌터카도 같이 취소된다.                      - (OK)
4. 렌터카 예약/취소에 대한 내역도 기존 메시지 서비스로 전달된다. - (OK)
5. 통합 뷰 페이지에서 렌터카 예약 정보도 같이 확인할 수 있다.    - (OK)


## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/80744273/119319091-fc6bf200-bcb4-11eb-9dac-0995c84a82e0.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

### [개인프로젝트]헥사고날 아키텍처 다이어그램 추가

![image](https://user-images.githubusercontent.com/31723044/120928860-72467380-c721-11eb-8b35-eaae1a1b1bf5.png)


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 
구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
   mvn spring-boot:run
```

## CQRS

숙소(Room) 의 사용가능 여부, 리뷰 및 예약/결재 등 총 Status 에 대하여 고객(Customer)이 조회 할 수 있도록 CQRS 로 구현하였다.
- room, review, reservation, payment 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링 (ROOMVIEW)

  ![image](https://user-images.githubusercontent.com/31723044/121186602-3dadf580-c8a2-11eb-8941-a54bd3744d26.png)

- viewpage MSA ViewHandler 를 통해 구현 ("RoomRegistered" 이벤트 발생 시, Pub/Sub 기반으로 별도 Roomview 테이블에 저장)
  ![image](https://user-images.githubusercontent.com/77129832/119321162-4d7ce580-bcb7-11eb-9030-29ee6272c40d.png)
  ![image](https://user-images.githubusercontent.com/31723044/119350185-fccab400-bcd9-11eb-8269-61868de41cc7.png)
  
- 실제로 view 페이지를 조회해 보면 모든 room에 대한 전반적인 예약 상태, 결제 상태, 리뷰 건수 등의 정보를 종합적으로 알 수 있다
  ![image](https://user-images.githubusercontent.com/31723044/121303043-895daf00-c935-11eb-9474-86891619db36.png)
  
## API 게이트웨이
      1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
       
          - application.yaml 예시
            ```
            spring:
              profiles: docker
              cloud:
                gateway:
                  routes:
                    - id: payment
                      uri: http://payment:8080
                      predicates:
                        - Path=/payments/** 
                    - id: room
                      uri: http://room:8080
                      predicates:
                        - Path=/rooms/**, /reviews/**, /check/**
                    - id: reservation
                      uri: http://reservation:8080
                      predicates:
                        - Path=/reservations/**
                    - id: message
                      uri: http://message:8080
                      predicates:
                        - Path=/messages/** 
                    - id: viewpage
                      uri: http://viewpage:8080
                      predicates:
                        - Path= /roomviews/**
                    - id: rentcar
                      uri: http://rentcar:8080
                      predicates:
                    - Path=/rentcars/**, /chkcar/**
                  globalcors:
                    corsConfigurations:
                      '[/**]':
                        allowedOrigins:
                          - "*"
                        allowedMethods:
                          - "*"
                        allowedHeaders:
                          - "*"
                        allowCredentials: true

            server:
              port: 8080            
            ```

         
      2. Kubernetes용 Deployment.yaml 을 작성하고 Kubernetes에 Deploy를 생성함
          - Deployment.yaml 예시
          

            ```
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: gateway
              namespace: airbnb
              labels:
                app: gateway
            spec:
              replicas: 1
              selector:
                matchLabels:
                  app: gateway
              template:
                metadata:
                  labels:
                    app: gateway
                spec:
                  containers:
                    - name: gateway
                      image: 247785678011.dkr.ecr.us-east-2.amazonaws.com/gateway:1.0
                      ports:
                        - containerPort: 8080
            ```               
            

            ```
            Deploy 생성
            kubectl apply -f deployment.yaml
            ```     
          - Kubernetes에 생성된 Deploy. 확인
            
![image](https://user-images.githubusercontent.com/80744273/119321943-1d821200-bcb8-11eb-98d7-bf8def9ebf80.png)
	    
            
      3. Kubernetes용 Service.yaml을 작성하고 Kubernetes에 Service/LoadBalancer을 생성하여 Gateway 엔드포인트를 확인함. 
          - Service.yaml 예시
          
            ```
            apiVersion: v1
              kind: Service
              metadata:
                name: gateway
                namespace: airbnb
                labels:
                  app: gateway
              spec:
                ports:
                  - port: 8080
                    targetPort: 8080
                selector:
                  app: gateway
                type:
                  LoadBalancer           
            ```             

           
            ```
            Service 생성
            kubectl apply -f service.yaml            
            ```             
            
            
          - API Gateay 엔드포인트 확인
           
            ```
            Service  및 엔드포인트 확인 
            kubectl get svc -n airbnb           
            ```                 
![image](https://user-images.githubusercontent.com/80744273/119318358-2a046b80-bcb4-11eb-9d46-ef2d498c2cff.png)

# Correlation

Airbnb 프로젝트에서는 PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면

예약(Reservation)을 하면 동시에 연관된 방(Room), 결제(Payment) 등의 서비스의 상태가 적당하게 변경이 되고,
예약건의 취소를 수행하면 다시 연관된 방(Room), 결제(Payment) 등의 서비스의 상태값 등의 데이터가 적당한 상태로 변경되는 것을
확인할 수 있습니다.

예약등록
![image](https://user-images.githubusercontent.com/31723044/119320227-54572880-bcb6-11eb-973b-a9a5cd1f7e21.png)
예약 후 - 방 상태
![image](https://user-images.githubusercontent.com/31723044/119320300-689b2580-bcb6-11eb-933e-98be5aadca61.png)
예약 후 - 예약 상태
![image](https://user-images.githubusercontent.com/31723044/119320390-810b4000-bcb6-11eb-8c62-48f6765c570a.png)
예약 후 - 결제 상태
![image](https://user-images.githubusercontent.com/31723044/119320524-a39d5900-bcb6-11eb-864b-173711eb9e94.png)
예약 취소
![image](https://user-images.githubusercontent.com/31723044/119320595-b6b02900-bcb6-11eb-8d8d-0d5c59603c72.png)
취소 후 - 방 상태
![image](https://user-images.githubusercontent.com/31723044/119320680-ccbde980-bcb6-11eb-8b7c-66315329aafe.png)
취소 후 - 예약 상태
![image](https://user-images.githubusercontent.com/31723044/119320747-dcd5c900-bcb6-11eb-9c44-fd3781c7c55f.png)
취소 후 - 결제 상태
![image](https://user-images.githubusercontent.com/31723044/119320806-ee1ed580-bcb6-11eb-8ccf-8c81385cc8ba.png)


## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 room 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.

```
package airbnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Room_table")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long roomId;       // 방ID
    private String status;     // 방 상태
    private String desc;       // 방 상세 설명
    private Long reviewCnt;    // 리뷰 건수
    private String lastAction; // 최종 작업

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public Long getReviewCnt() {
        return reviewCnt;
    }

    public void setReviewCnt(Long reviewCnt) {
        this.reviewCnt = reviewCnt;
    }
    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package airbnb;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="rooms", path="rooms")
public interface RoomRepository extends PagingAndSortingRepository<Room, Long>{

}
```
- 적용 후 REST API 의 테스트
```
# room 서비스의 room 등록
http POST http://localhost:8088/rooms desc="Beautiful House"  

# reservation 서비스의 예약 요청
http POST http://localhost:8088/reservations roomId=1 status=reqReserve

# reservation 서비스의 예약 상태 확인
http GET http://localhost:8088/reservations

```

## 동기식 호출(Sync) 과 Fallback 처리

분석 단계에서의 조건 중 하나로 예약 시 숙소(room) 간의 예약 가능 상태 확인 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 또한 예약(reservation) -> 결제(payment) 서비스도 동기식으로 처리하기로 하였다.

- 룸, 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# PaymentService.java

package airbnb.external;

<import문 생략>

@FeignClient(name="Payment", url="${prop.room.url}")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void approvePayment(@RequestBody Payment payment);

}

# RoomService.java

package airbnb.external;

<import문 생략>

@FeignClient(name="Room", url="${prop.room.url}")
public interface RoomService {

    @RequestMapping(method= RequestMethod.GET, path="/check/chkAndReqReserve")
    public boolean chkAndReqReserve(@RequestParam("roomId") long roomId);

}

# RentcarService.java

<import문 생략>

@FeignClient(name="rentcar", url="${prop.room.url}")
public interface RentcarService {

    @RequestMapping(method= RequestMethod.GET, path="/chkcar/chkStatus")
    public boolean chkStatus(@RequestParam("carId") long carId);

}


```

- 예약 요청을 받은 직후(@PostPersist) 가능 상태 확인 및 결제를 동기(Sync)로 요청하도록 처리
```
# Reservation.java (Entity)

    @PostPersist
    public void onPostPersist(){

        ////////////////////////////////
        // RESERVATION에 INSERT 된 경우 
        ////////////////////////////////

        ////////////////////////////////////
        // 예약 요청(reqReserve) 들어온 경우
        ////////////////////////////////////

        // 해당 ROOM이 Available한 상태인지 체크
        boolean result = ReservationApplication.applicationContext.getBean(airbnb.external.RoomService.class).chkAndReqReserve(this.getRoomId());
        
        if(result == false) {
            return; // 방이 예약 가능한 상태가 아닌 경우 여기서 끝낸다
        }

        // 방이 예약 가능한 상태이면서 렌터카 예약이 들어온 경우 렌터카 체크 한번 더 한다.
        if(carId > 0) {

            result = ReservationApplication.applicationContext.getBean(airbnb.external.RentcarService.class).chkStatus(this.getCarId());
            
            if(result == false) {
                return; // 해당 렌터카가 예약 가능한 상태가 아닌 경우 여기서 끝낸다
            }

        } else {
            System.out.println("######## No RentCar Reservation");
        }
        
        //////////////////////////////////////////
        // 이 지점까지 왔다는 것은 예약이 가능한 상태
        //////////////////////////////////////////

        //////////////////////////////
        // PAYMENT 결제 진행 (POST방식)
        //////////////////////////////
        airbnb.external.Payment payment = new airbnb.external.Payment();
        payment.setRsvId(this.getRsvId());
        payment.setRoomId(this.getRoomId());
        payment.setCarId(this.getCarId());
        payment.setStatus("paid");
        ReservationApplication.applicationContext.getBean(airbnb.external.PaymentService.class)
                .approvePayment(payment);

        /////////////////////////////////////
        // 이벤트 발행 --> ReservationCreated
        /////////////////////////////////////
        ReservationCreated reservationCreated = new ReservationCreated();
        BeanUtils.copyProperties(this, reservationCreated);
        reservationCreated.publishAfterCommit();
        
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제 (pay) 서비스를 잠시 내려놓음 (ctrl+c)

# 예약 요청
http POST http://localhost:8088/reservations roomId=1 status=reqReserve   #Fail

# 결제서비스 재기동
cd payment
mvn spring-boot:run

# 예약 요청
http POST http://localhost:8088/reservations roomId=1 status=reqReserve   #Success
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

결제가 이루어진 후에 숙소 시스템의 상태가 업데이트 되고, 예약 시스템의 상태가 업데이트 되며, 예약 및 취소 메시지가 전송되는 시스템과의 통신 행위는 비동기식으로 처리한다.
 
- 이를 위하여 결제가 승인되면 결제가 승인 되었다는 이벤트를 카프카로 송출한다. (Publish)
 
```
# Payment.java

package airbnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Payment_table")
public class Payment {

    ....

    @PostPersist
    public void onPostPersist(){
        ////////////////////////////
        // 결제 승인 된 경우
        ////////////////////////////

        // 이벤트 발행 -> PaymentApproved
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();
    }
    
    ....
}
```

- 예약 시스템에서는 결제 승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
# PolicyHandler.java in RESERVATION Service


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_ConfirmReserve(@Payload PaymentApproved paymentApproved){

        if(paymentApproved.isMe()){

            ///////////////////////////////////////
            // 결제 완료 시 -> Status -> reserved
            ///////////////////////////////////////
            System.out.println("##### listener ConfirmReserve : " + paymentApproved.toJson());

            long rsvId = paymentApproved.getRsvId(); // 결제 완료된 rsvId
            long payId = paymentApproved.getPayId(); // 결제된 payId -> 나중에 취소할때 쓰임

            updateResvationStatus(rsvId, "reserved", payId); // Status Update

        }
    }

```

그 외 메시지 서비스는 예약/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 메시지 서비스가 유지보수로 인해 잠시 내려간 상태 라도 예약을 받는데 문제가 없다.

```
# 메시지 서비스 (message) 를 잠시 내려놓음 (ctrl+c)

# 예약 요청
http POST http://localhost:8088/reservations roomId=1 status=reqReserve   #Success

# 예약 상태 확인
http GET localhost:8088/reservations    #메시지 서비스와 상관없이 예약 상태는 정상 확인

```

# 운영


## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD는 buildspec.yml을 이용한 AWS codebuild를 사용하였습니다.

- CodeBuild 프로젝트를 생성하고 AWS_ACCOUNT_ID, KUBE_URL, KUBE_TOKEN 환경 변수 세팅을 한다
```
SA 생성
kubectl apply -f eks-admin-service-account.yml
```
![codebuild(sa)](https://user-images.githubusercontent.com/38099203/119293259-ff52ec80-bc8c-11eb-8671-b9a226811762.PNG)
```
Role 생성
kubectl apply -f eks-admin-cluster-role-binding.yml
```
![codebuild(role)](https://user-images.githubusercontent.com/38099203/119293300-1abdf780-bc8d-11eb-9b07-ad173237efb1.PNG)
```
Token 확인
kubectl -n kube-system get secret
kubectl -n kube-system describe secret eks-admin-token-rjpmq
```
![codebuild(token)](https://user-images.githubusercontent.com/38099203/119293511-84d69c80-bc8d-11eb-99c7-e8929e6a41e4.PNG)
```
buildspec.yml 파일 
마이크로 서비스 room의 yml 파일 이용하도록 세팅
```
![codebuild(buildspec)](https://user-images.githubusercontent.com/38099203/119283849-30292680-bc79-11eb-9f86-cbb715e74846.PNG)

- codebuild 실행
```
codebuild 프로젝트 및 빌드 이력
```
![codebuild(프로젝트)](https://user-images.githubusercontent.com/38099203/119283851-315a5380-bc79-11eb-9b2a-b4522d22d009.PNG)
![codebuild(로그)](https://user-images.githubusercontent.com/38099203/119283850-30c1bd00-bc79-11eb-9547-1ff1f62e48a4.PNG)

- codebuild 빌드 내역 (Message 서비스 세부)

![image](https://user-images.githubusercontent.com/31723044/119385500-2b0fba00-bd01-11eb-861b-cc31910ff945.png)

- codebuild 빌드 내역 (전체 이력 조회)

![image](https://user-images.githubusercontent.com/31723044/119385401-087da100-bd01-11eb-8b69-ce222e6bb71e.png)




## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: istio 사용하여 구현함

시나리오는 예약(reservation)--> 룸(room) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 예약 요청이 과도할 경우 CB 를 통하여 장애격리.

- DestinationRule 를 생성하여 circuit break 가 발생할 수 있도록 설정
최소 connection pool 설정
```
# destination-rule.yml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-room
  namespace: airbnb
spec:
  host: room
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1
#    outlierDetection:
#      interval: 1s
#      consecutiveErrors: 1
#      baseEjectionTime: 10s
#      maxEjectionPercent: 100
```

* istio-injection 활성화 및 room pod container 확인

```
kubectl get ns -L istio-injection
kubectl label namespace airbnb istio-injection=enabled 
```

![Circuit Breaker(istio-enjection)](https://user-images.githubusercontent.com/38099203/119295450-d6812600-bc91-11eb-8aad-46eeac968a41.PNG)

![Circuit Breaker(pod)](https://user-images.githubusercontent.com/38099203/119295568-0cbea580-bc92-11eb-9d2b-8580f3576b47.PNG)


* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:

siege 실행

```
kubectl run siege --image=apexacme/siege-nginx -n airbnb
kubectl exec -it siege -c siege -n airbnb -- /bin/bash
```


- 동시사용자 1로 부하 생성 시 모두 정상
```
siege -c1 -t10S -v --content-type "application/json" 'http://room:8080/rooms POST {"desc": "Beautiful House3"}'

** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
HTTP/1.1 201     0.49 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.05 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     254 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     256 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     256 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     256 bytes ==> POST http://room:8080/rooms
```

- 동시사용자 2로 부하 생성 시 503 에러 168개 발생
```
siege -c2 -t10S -v --content-type "application/json" 'http://room:8080/rooms POST {"desc": "Beautiful House3"}'

** SIEGE 4.0.4
** Preparing 2 concurrent users for battle.
The server is now under siege...
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 503     0.10 secs:      81 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.04 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.05 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.22 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.08 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.07 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 503     0.01 secs:      81 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 503     0.01 secs:      81 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     258 bytes ==> POST http://room:8080/rooms
HTTP/1.1 503     0.00 secs:      81 bytes ==> POST http://room:8080/rooms

Lifting the server siege...
Transactions:                   1904 hits
Availability:                  91.89 %
Elapsed time:                   9.89 secs
Data transferred:               0.48 MB
Response time:                  0.01 secs
Transaction rate:             192.52 trans/sec
Throughput:                     0.05 MB/sec
Concurrency:                    1.98
Successful transactions:        1904
Failed transactions:             168
Longest transaction:            0.03
Shortest transaction:           0.00
```

- kiali 화면에 서킷 브레이크 확인

![Circuit Breaker(kiali)](https://user-images.githubusercontent.com/38099203/119298194-7f7e4f80-bc97-11eb-8447-678eece29e5c.PNG)


- 다시 최소 Connection pool로 부하 다시 정상 확인

```
** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.03 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.00 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.02 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.00 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.00 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms
HTTP/1.1 201     0.01 secs:     260 bytes ==> POST http://room:8080/rooms

:
:

Lifting the server siege...
Transactions:                   1139 hits
Availability:                 100.00 %
Elapsed time:                   9.19 secs
Data transferred:               0.28 MB
Response time:                  0.01 secs
Transaction rate:             123.94 trans/sec
Throughput:                     0.03 MB/sec
Concurrency:                    0.98
Successful transactions:        1139
Failed transactions:               0
Longest transaction:            0.04
Shortest transaction:           0.00

```

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌.
  virtualhost 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.


### 오토스케일 아웃

오토스케일 아웃을 확인하기 위해 Room 서비스에 resource 설정 및 오토스케일 설정을 한 후 부하 툴을 통해 테스트를 진행함.

- room deployment.yml 파일에 resources 설정을 추가한다   

![image](https://user-images.githubusercontent.com/31723044/121301076-a17fff00-c932-11eb-8753-86d6915ee25b.png)

- Room 서비스에 대해서 오토 스케일 아웃을 테스트 하기 위해 아래와 같이 설정을 한다.   
room 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 50프로를 넘어서면 replica 를 10개까지 늘려준다.   

```
kubectl autoscale deployment room -n airbnb --cpu-percent=50 --min=1 --max=10
```

- 부하를 동시사용자 100명, 1분 동안 걸어준다.

```
siege -c100 -t60S -v --content-type "application/json" 'http://room:8080/rooms POST {"desc": "Beautiful House3"}'
```

![image](https://user-images.githubusercontent.com/31723044/121302101-228bc600-c934-11eb-9108-99f90958291c.png)

- 오토스케일 과정 확인 ( kubectl -n airbnb top pod )

![image](https://user-images.githubusercontent.com/31723044/121302166-3f27fe00-c934-11eb-97a8-ac62d71fcac9.png)

- 오토스케일 과정 확인 ( TOP에서 CPU 사용률 갑자기 증가 확인 )

![image](https://user-images.githubusercontent.com/31723044/121302183-43ecb200-c934-11eb-8ff7-80a94b5b9492.png)

- Room Pod 자동 확장 확인 ( 1개였던 Room 서비스가 최종적으로 4개까지 확장됨)

![image](https://user-images.githubusercontent.com/31723044/121302198-4bac5680-c934-11eb-80db-9e86c5b4a56c.png)
![image](https://user-images.githubusercontent.com/31723044/121302228-5830af00-c934-11eb-8d46-66b8272ca29e.png)


- siege 의 최종 Report 
```
Lifting the server siege...
Transactions:                  26850 hits
Availability:                 100.00 %
Elapsed time:                  59.70 secs
Data transferred:               6.71 MB
Response time:                  0.22 secs
Transaction rate:             449.75 trans/sec
Throughput:                     0.11 MB/sec
Concurrency:                   99.20
Successful transactions:       26850
Failed transactions:               0
Longest transaction:            1.79
Shortest transaction:           0.00
```

## Zero-downtime deploy (Readiness Probe)

* 무정지 재배포가 일부러 되지 않게 구현

deployment.yml에서 readinessProbe 부분 주석 처리   
![image](https://user-images.githubusercontent.com/31723044/121297904-fb31fa80-c92d-11eb-80d8-6f8280ac6c94.png)

Siege 부하 수행   
```
siege -c1 -t180S -v --content-type "application/json" 'http://room:8080/rooms POST {"desc": "Beautiful House3"}'
```

재배포중 서비스 실패됨을 확인(Availability:84.77%)   
![image](https://user-images.githubusercontent.com/31723044/121297912-008f4500-c92e-11eb-8fdf-9d6bc83d0b46.png)


* 무정지 재배포 되도록 구현

deployment.yml에서 readinessProbe 부분 설정   
![image](https://user-images.githubusercontent.com/31723044/121297928-071dbc80-c92e-11eb-9087-920066f89d86.png)

Siege 부하 재수행   
```
siege -c1 -t180S -v --content-type "application/json" 'http://room:8080/rooms POST {"desc": "Beautiful House3"}'
```

readinessProbe 설정 후에는 재수행하면 부하 테스트 결과 100프로로 성공됨(Availability:100%)   
![image](https://user-images.githubusercontent.com/31723044/121297996-23b9f480-c92e-11eb-988d-f9bf2d8aeee3.png)

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


# Config Map/ Persistence Volume

- Config Map

1: cofingmap.yml 파일 생성 ( Message 서비스 )

```
kubectl apply -f configmap.yml

apiVersion: v1
kind: ConfigMap
data:
  cityname: Seoul
metadata:
  name: my-config
  namespace: airbnb

```

2. deployment.yml에 적용하기 ( Message 서비스 )

```
kubectl apply -f deployment.yml

env:
  - name: CITYNAME
    valueFrom:
      configMapKeyRef:
        name: my-config
        key: cityname
```

3. 메시지 서비스 내에서 도시명 호출 용도로 사용

```
String cityStr = System.getenv("CITYNAME");
if(!cityStr.isEmpty()) {
  msgString += ", 도시명["+cityStr+"]";
}
```

4. ConfigMap 적용 수행 결과

![image](https://user-images.githubusercontent.com/31723044/121293009-e05b8800-c925-11eb-8ecd-bb4af264145c.png)


# Self-healing (Liveness Probe)

- room deployment.yml 파일을 임시로 Liveness Probe가 실패하도록 수정
 
![image](https://user-images.githubusercontent.com/31723044/121294448-61b41a00-c928-11eb-8669-17da64893a60.png)

- 수정된 deployment.yml로 재 실행 후 watch kubectl -n airbnb get all로 Room 서비스 Restart 확인

최초 수행 상태   
![image](https://user-images.githubusercontent.com/31723044/121294581-9b852080-c928-11eb-8381-089d8b89dd3b.png)

1회 재시작(RESTART) 확인   
![image](https://user-images.githubusercontent.com/31723044/121294609-a50e8880-c928-11eb-9ac3-b05db9ad636b.png)

2회 재시작(RESTART) 확인   
![image](https://user-images.githubusercontent.com/31723044/121294626-ac359680-c928-11eb-9bcb-50fb4ae24b6d.png)
