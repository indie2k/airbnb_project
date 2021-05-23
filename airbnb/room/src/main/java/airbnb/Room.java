package airbnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
//import java.util.List;
//import java.util.Date;

@Entity
@Table(name="Room_table")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long roomId;
    private String status;
    private String desc;
    private Long reviewCnt;
    private String lastAction;

    @PostPersist
    public void onPostPersist(){

        //////////////////////////////
        // Room 테이블 Insert 후 수행
        //////////////////////////////

        // 기본값 셋팅
        lastAction = "register";    // Insert는 항상 register
        reviewCnt = 0L;             // 리뷰 건수는 따로 입력이 들어오지 않아도 기본값 0
        status = "available";       // 최초 등록시 항상 이용가능

        // RoomRegistered Event 발생
        RoomRegistered roomRegistered = new RoomRegistered();
        BeanUtils.copyProperties(this, roomRegistered);
        roomRegistered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){

        /////////////////////////////
        // Room 테이블 Update 후 수행
        /////////////////////////////

        System.out.println("lastAction : " + lastAction);

        // RoomModified Event 발생 혹은 리뷰 이벤트 발생시
        if(lastAction.equals("modify") || lastAction.equals("review")) {
            RoomModified roomModified = new RoomModified();
            BeanUtils.copyProperties(this, roomModified);
            roomModified.publishAfterCommit();
        }

        // RoomReserved Event 발생
        if(lastAction.equals("reserved")) {
            RoomReserved roomReserved = new RoomReserved();
            BeanUtils.copyProperties(this, roomReserved);
            roomReserved.publishAfterCommit();
        }

        // RoomCancelled Event 발생
        if(lastAction.equals("cancelled")) {
            RoomCancelled roomCancelled = new RoomCancelled();
            BeanUtils.copyProperties(this, roomCancelled);
            roomCancelled.publishAfterCommit();
        }

        // review 작성/삭제 시 -> Do Nothing
        //if(lastAction.equals("review")) {
            // Do Nothing
        //}
        
    }

    @PreRemove
    public void onPreRemove(){

        ////////////////////////////
        // Room 테이블 Delete 전 수행
        ////////////////////////////

        // RoomDeleted Event 발생
        RoomDeleted roomDeleted = new RoomDeleted();
        BeanUtils.copyProperties(this, roomDeleted);
        roomDeleted.publishAfterCommit();

    }

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
