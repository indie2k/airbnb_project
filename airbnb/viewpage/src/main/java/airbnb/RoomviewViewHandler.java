package airbnb;

import airbnb.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class RoomviewViewHandler {


    @Autowired
    private RoomviewRepository roomviewRepository;

    //////////////////////////////////////////
    // 방이 등록되었을 때 Insert -> RoomView TABLE
    //////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomRegistered_then_CREATE_1 (@Payload RoomRegistered roomRegistered) {
        try {

            if (!roomRegistered.validate()) return;

            // view 객체 생성
            Roomview roomview = new Roomview();
            // view 객체에 이벤트의 Value 를 set 함
            roomview.setId(roomRegistered.getRoomId());
            roomview.setDesc(roomRegistered.getDesc());
            roomview.setReviewCnt(roomRegistered.getReviewCnt());
            roomview.setRoomStatus(roomRegistered.getStatus());
            // view 레파지 토리에 save
            roomviewRepository.save(roomview);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //////////////////////////////////////////////////
    // 방이 수정되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomModified_then_UPDATE_1(@Payload RoomModified roomModified) {
        try {
            if (!roomModified.validate()) return;
                // view 객체 조회
            Optional<Roomview> roomviewOptional = roomviewRepository.findById(roomModified.getRoomId());
            if( roomviewOptional.isPresent()) {
                Roomview roomview = roomviewOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                    roomview.setDesc(roomModified.getDesc());
                    roomview.setReviewCnt(roomModified.getReviewCnt());
                    roomview.setRoomStatus(roomModified.getStatus());
                // view 레파지 토리에 save
                roomviewRepository.save(roomview);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 예약이 확정되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationConfirmed_then_UPDATE_2(@Payload ReservationConfirmed reservationConfirmed) {
        try {
            if (!reservationConfirmed.validate()) return;
                // view 객체 조회
            Optional<Roomview> roomviewOptional = roomviewRepository.findById(reservationConfirmed.getRoomId());
            if( roomviewOptional.isPresent()) {
                Roomview roomview = roomviewOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                    roomview.setRsvId(reservationConfirmed.getRsvId());
                    roomview.setRsvStatus(reservationConfirmed.getStatus());
                // view 레파지 토리에 save
                roomviewRepository.save(roomview);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 결제가 승인 되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_UPDATE_3(@Payload PaymentApproved paymentApproved) {
        try {
            if (!paymentApproved.validate()) return;
                // view 객체 조회
            System.out.println("#######################");
            System.out.println("###PAYMENT APPROVED ###" + paymentApproved.getRsvId());
            System.out.println("#######################");

            //List<Roomview> roomviewList = roomviewRepository.findByRsvId(paymentApproved.getRsvId());
            //for(Roomview roomview : roomviewList){
            //    // view 객체에 이벤트의 eventDirectValue 를 set 함
            //    roomview.setPayId(paymentApproved.getPayId());
            //    roomview.setPayStatus(paymentApproved.getStatus());
            //    // view 레파지 토리에 save
            //    roomviewRepository.save(roomview);
            //}

            // RoomId로 변경
            Optional<Roomview> roomviewOptional = roomviewRepository.findById(paymentApproved.getRoomId());
            if( roomviewOptional.isPresent()) {
                Roomview roomview = roomviewOptional.get();
                roomview.setPayId(paymentApproved.getPayId());
                roomview.setPayStatus(paymentApproved.getStatus());
                roomviewRepository.save(roomview);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 예약이 취소 되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCancelled_then_UPDATE_4(@Payload ReservationCancelled reservationCancelled) {
        try {
            if (!reservationCancelled.validate()) return;
                // view 객체 조회
            List<Roomview> roomviewList = roomviewRepository.findByRsvId(reservationCancelled.getRsvId());
            for(Roomview roomview : roomviewList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                roomview.setRsvStatus(reservationCancelled.getStatus());
                // view 레파지 토리에 save
                roomviewRepository.save(roomview);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 결제가 취소 되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCancelled_then_UPDATE_5(@Payload PaymentCancelled paymentCancelled) {
        try {
            if (!paymentCancelled.validate()) return;
                // view 객체 조회
            List<Roomview> roomviewList = roomviewRepository.findByPayId(paymentCancelled.getPayId());
            for(Roomview roomview : roomviewList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                roomview.setPayStatus(paymentCancelled.getStatus());
                // view 레파지 토리에 save
                roomviewRepository.save(roomview);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    
    //////////////////////////////////////////////////
    // 방이 예약 되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomReserved_then_UPDATE_6(@Payload RoomReserved roomReserved) {
    
        try {
            if (!roomReserved.validate()) return;
    
            System.out.println("#######################");
            System.out.println("###ROOM RESERVED ###" + roomReserved.getRoomId());
            System.out.println("#######################");

            Optional<Roomview> roomviewOptional = roomviewRepository.findById(roomReserved.getRoomId());
            if( roomviewOptional.isPresent()) {
                Roomview roomview = roomviewOptional.get();
                roomview.setRoomStatus(roomReserved.getStatus());
                roomviewRepository.save(roomview);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 방이 취소 되었을 때 Update -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomCancelled_then_UPDATE_6(@Payload RoomCancelled roomCancelled) {
    
        try {
            if (!roomCancelled.validate()) return;
    
            System.out.println("#######################");
            System.out.println("###ROOM CANCELLED ###" + roomCancelled.getRoomId());
            System.out.println("#######################");

            Optional<Roomview> roomviewOptional = roomviewRepository.findById(roomCancelled.getRoomId());
            if( roomviewOptional.isPresent()) {
                Roomview roomview = roomviewOptional.get();
                roomview.setRoomStatus(roomCancelled.getStatus());
                roomviewRepository.save(roomview);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////
    // 방이 삭제 되었을 때 Delete -> RoomView TABLE
    //////////////////////////////////////////////////
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoomDeleted_then_DELETE_1(@Payload RoomDeleted roomDeleted) {
        try {
            if (!roomDeleted.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            roomviewRepository.deleteById(roomDeleted.getRoomId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}