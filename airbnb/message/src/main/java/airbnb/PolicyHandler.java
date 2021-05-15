package airbnb;

import airbnb.config.kafka.KafkaProcessor;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{


    @Autowired
    private MessageRepository messageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationConfirmed_SendConfirmMsg(@Payload ReservationConfirmed reservationConfirmed){

        if(reservationConfirmed.isMe()){

            /////////////////
            // 예약 확정 시 
            /////////////////
            System.out.println("##### listener SendConfirmMsg : " + reservationConfirmed.toJson());

            // roomId 추출
            long roomId = reservationConfirmed.getRoomId(); // 예약 확정된 RoomId
            String msgString = "예약이 완료 되었습니다. 방 번호 : [" + roomId +"]";

            // 메시지 전송
            sendMsg(roomId, msgString);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_SendCancelMsg(@Payload ReservationCancelled reservationCancelled){

        if(reservationCancelled.isMe()){

            //////////////////
            // 예약 취소 시
            /////////////////
            System.out.println("##### listener SendCancelMsg : " + reservationCancelled.toJson());

            // roomId 추출
            long roomId = reservationCancelled.getRoomId(); // 취소된 RoomId
            String msgString = "예약이 취소 되었습니다. 방 번호 : [" + roomId +"]";

            // 메시지 전송
            sendMsg(roomId, msgString);

        }
    }

    private void sendMsg(long roomId, String msgString)     {

        //////////////////////////////////////////////
        // roomId 룸에 대해 msgString으로 SMS를 쌓는다
        //////////////////////////////////////////////
        Message msg = new Message();
        msg.setRoomId(roomId);
        msg.setContent(msgString);

        // DB Insert
        messageRepository.save(msg);
    }
}
