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
    private RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReviewCreated_UpdateReviewCnt(@Payload ReviewCreated reviewCreated){

        if(reviewCreated.isMe()){

            ////////////////////////////////////////
            // 리뷰 등록 시 -> Room의 리뷰 카운트 증가
            ////////////////////////////////////////
            System.out.println("##### listener UpdateReviewCnt : " + reviewCreated.toJson());

            long roomIdOfReview = reviewCreated.getRoomId(); // 등록된 리뷰의 RoomID

            updateReviewCnt(roomIdOfReview, +1); // 리뷰건수 증가
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReviewDeleted_UpdateReviewCnt(@Payload ReviewDeleted reviewDeleted){
        if(reviewDeleted.isMe()){
            ////////////////////////////////////////
            // 리뷰 삭제 시 -> Room의 리뷰 카운트 감소
            ////////////////////////////////////////
            System.out.println("##### listener UpdateReviewCnt : " + reviewDeleted.toJson());

            long roomIdOfReview = reviewDeleted.getRoomId(); // 삭제된 리뷰의 RoomID

            updateReviewCnt(roomIdOfReview, -1); // 리뷰건수 감소
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationConfirmed_ConfirmReserve(@Payload ReservationConfirmed reservationConfirmed){

        if(reservationConfirmed.isMe()){

            ////////////////////////////////////////////////////////////////////
            // 예약 확정 시 -> Room의 status => reserved, lastAction => reserved
            ////////////////////////////////////////////////////////////////////

            System.out.println("##### listener ConfirmReserve : " + reservationConfirmed.toJson());

            long roomId = reservationConfirmed.getRoomId(); // 삭제된 리뷰의 RoomID

            updateRoomStatus(roomId, "reserved", "reserved"); // Status Update

        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled){

        if(reservationCancelled.isMe()){
            //////////////////////////////////////////////////////////////////////
            // 예약 취소 시 -> Room의 status => available, lastAction => cancelled
            //////////////////////////////////////////////////////////////////////
            System.out.println("##### listener Cancel : " + reservationCancelled.toJson());

            long roomId = reservationCancelled.getRoomId(); // 삭제된 리뷰의 RoomID

            updateRoomStatus(roomId, "available", "cancelled"); // Status Update
        }
    }

    
    private void updateReviewCnt(long roomId, long num)     {

        //////////////////////////////////////////////
        // roomId 룸 데이터의 ReviewCnt를 num 만큼 가감
        //////////////////////////////////////////////

        // Room 테이블에서 roomId의 Data 조회 -> room
        Optional<Room> res = roomRepository.findById(roomId);
        Room room = res.get();

        System.out.println("roomId    : " + room.getRoomId());
        System.out.println("reviewCnt : " + room.getReviewCnt());

        // room 값 수정
        room.setReviewCnt(room.getReviewCnt() + num); // 리뷰건수 증가/감소
        room.setLastAction("review");  // lastAction 값 셋팅

        System.out.println("Edited reviewCnt : " + room.getReviewCnt());
        System.out.println("Edited lastAction : " + room.getLastAction());

        /////////////
        // DB Update
        /////////////
        roomRepository.save(room);

    }

        
    private void updateRoomStatus(long roomId, String status, String lastAction)     {

        //////////////////////////////////////////////
        // roomId 룸 데이터의 status, lastAction 수정
        //////////////////////////////////////////////

        // Room 테이블에서 roomId의 Data 조회 -> room
        Optional<Room> res = roomRepository.findById(roomId);
        Room room = res.get();

        System.out.println("roomId      : " + room.getRoomId());
        System.out.println("status      : " + room.getStatus());
        System.out.println("lastAction  : " + room.getLastAction());

        // room 값 수정
        room.setStatus(status); // status 수정 
        room.setLastAction(lastAction);  // lastAction 값 셋팅

        System.out.println("Edited status     : " + room.getStatus());
        System.out.println("Edited lastAction : " + room.getLastAction());

        /////////////
        // DB Update
        /////////////
        roomRepository.save(room);

    }

}
