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
    private ReservationRepository reservationRepository;


    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

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
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_ConfirmCancel(@Payload PaymentCancelled paymentCancelled){

        if(paymentCancelled.isMe()){
            ///////////////////////////////////////////
            // 결제 취소 완료 시 -> Status -> Cancelled
            ///////////////////////////////////////////
            System.out.println("##### listener ConfirmCancel : " + paymentCancelled.toJson());

            long rsvId = paymentCancelled.getRsvId(); // 취소된 rsvId
            long payId = paymentCancelled.getPayId(); // 결제된 payId -> 나중에 취소할때 쓰임

            updateResvationStatus(rsvId, "cancelled", payId ); // Status Update

        }
    }


    private void updateResvationStatus(long rsvId, String status, long payId)     {

        //////////////////////////////////////////////
        // roomId 룸 데이터의 status, lastAction 수정
        //////////////////////////////////////////////

        // Room 테이블에서 roomId의 Data 조회 -> room
        Optional<Reservation> res = reservationRepository.findById(rsvId);
        Reservation reservation = res.get();

        // room 값 수정
        reservation.setStatus(status); // status 수정 
        reservation.setPayId(payId); // payId 수정

        System.out.println("Edited status     : " + reservation.getStatus());
        System.out.println("Edited payId     : " + reservation.getPayId());

        /////////////
        // DB Update
        /////////////
        reservationRepository.save(reservation);

    }
}
