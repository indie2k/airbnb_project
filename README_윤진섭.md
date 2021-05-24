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

