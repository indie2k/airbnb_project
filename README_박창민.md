  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?


  - API 게이트웨이
      1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함.
      
         ![image](https://user-images.githubusercontent.com/80744273/119316082-7dc18580-bcb1-11eb-83e7-64b6f8130ada.png)
         
      2. Kubernetes용  Deployment.yaml 을 작성하고 Kubernetes에 Deploy를 생성함.
          - Deployment.yaml 예시
          ![image](https://user-images.githubusercontent.com/80744273/119316250-b7928c00-bcb1-11eb-8caa-960c7326603e.png)
          - Kubernetes에 생성된 Deploy. 확인
          ![image](https://user-images.githubusercontent.com/80744273/119315603-f8d66c00-bcb0-11eb-84e2-615134c6f360.png)

      4. Kubernetes용 Service.yaml을 작성하고 Kubernetes에 Service/LoadBalancer을 생성하여 Gateway 엔드포인트를 확인함. 
          - Service.yaml 예시
          ![image](https://user-images.githubusercontent.com/80744273/119316167-97fb6380-bcb1-11eb-8adb-86f945a0f344.png)
          - API GateWay 엔드포인트 확인
          ![image](https://user-images.githubusercontent.com/80744273/119318358-2a046b80-bcb4-11eb-9d46-ef2d498c2cff.png)


## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/80744273/119313027-05a59080-bcae-11eb-88b6-4309e311a295.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

