
# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/77129832/119316165-96ca3680-bcb1-11eb-9a91-f2b627890bab.png)

## TO-BE 조직 (Vertically-Aligned)  
  ![image](https://user-images.githubusercontent.com/77129832/119315258-a09f6a00-bcb0-11eb-9940-c2a82f2f7d09.png)



# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

## CQRS

숙소(Room) 의 사용가능 여부, 리뷰 및 예약/결재 등 총 Status 에 대하여 고객(Customer)이 조회 할 수 있도록 CQRS 로 구현하였다.
- room, review, reservation, payment 개별 Aggregate Status 를 통합조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링 (ROOMVIEW)

  ![image](https://user-images.githubusercontent.com/77129832/119319352-4b198c00-bcb5-11eb-93bc-ff0657feeb9f.png)
- viewpage MSA ViewHandler 를 통해 구현 ("RoomRegistered" 이벤트 발생 시, Pub/Sub 기반으로 별도 Roomview 테이블에 저장)
  ![image](https://user-images.githubusercontent.com/77129832/119321162-4d7ce580-bcb7-11eb-9030-29ee6272c40d.png)
  
