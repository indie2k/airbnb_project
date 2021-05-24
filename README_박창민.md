  - API 게이트웨이
      1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
       
          - application.yaml 예시
       
            ![image](https://user-images.githubusercontent.com/80744273/119316082-7dc18580-bcb1-11eb-83e7-64b6f8130ada.png)
         
      2. Kubernetes용 Deployment.yaml 을 작성하고 Kubernetes에 Deploy를 생성함
          - Deployment.yaml 예시
          
            ![image](https://user-images.githubusercontent.com/80744273/119316250-b7928c00-bcb1-11eb-8caa-960c7326603e.png)
            
          - Kubernetes에 생성된 Deploy. 확인
            
            ![image](https://user-images.githubusercontent.com/80744273/119321943-1d821200-bcb8-11eb-98d7-bf8def9ebf80.png)
            
      3. Kubernetes용 Service.yaml을 작성하고 Kubernetes에 Service/LoadBalancer을 생성하여 Gateway 엔드포인트를 확인함. 
          - Service.yaml 예시
          
            ![image](https://user-images.githubusercontent.com/80744273/119316167-97fb6380-bcb1-11eb-8adb-86f945a0f344.png)
            
          - API Gateay 엔드포인트 확인
          
            ![image](https://user-images.githubusercontent.com/80744273/119318358-2a046b80-bcb4-11eb-9d46-ef2d498c2cff.png)


## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/80744273/119319091-fc6bf200-bcb4-11eb-9dac-0995c84a82e0.png)


