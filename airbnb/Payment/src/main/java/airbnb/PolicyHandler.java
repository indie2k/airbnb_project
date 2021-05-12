package airbnb;

import airbnb.config.kafka.KafkaProcessor;

import java.util.Optional;

//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    private PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelRequested_CancelPayment(@Payload ReservationCancelRequested reservationCancelRequested){

        if(reservationCancelRequested.isMe()){

            /////////////////////////////////////////////
            // 취소 요청이 왔을 때 -> status -> cancelled 
            /////////////////////////////////////////////
            System.out.println("##### listener CancelPayment : " + reservationCancelRequested.toJson());
            
            // 취소시킬 payId 추출
            long payId = reservationCancelRequested.getPayId(); // 취소시킬 payId

            Optional<Payment> res = paymentRepository.findById(payId);
            Payment payment = res.get();

            payment.setStatus("cancelled"); // 취소 상태로 

            // DB Update
            paymentRepository.save(payment);

        }
    }

}
