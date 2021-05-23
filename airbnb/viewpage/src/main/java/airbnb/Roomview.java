package airbnb;

import javax.persistence.*;
//import java.util.List;

@Entity
@Table(name="Roomview_table")
public class Roomview {

        @Id
        private Long roomId;
        private String desc;
        private Long reviewCnt;
        private String roomStatus;
        private Long rsvId;
        private String rsvStatus;
        private Long payId;
        private String payStatus;


        public Long getRoomId() {
            return roomId;
        }

        public void setId(Long roomId) {
            this.roomId = roomId;
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

        public String getRoomStatus() {
            return roomStatus;
        }

        public void setRoomStatus(String roomStatus) {
            this.roomStatus = roomStatus;
        }

        public Long getRsvId() {
            return rsvId;
        }

        public void setRsvId(Long rsvId) {
            this.rsvId = rsvId;
        }

        public String getRsvStatus() {
            return rsvStatus;
        }

        public void setRsvStatus(String rsvStatus) {
            this.rsvStatus = rsvStatus;
        }

        public Long getPayId() {
            return payId;
        }

        public void setPayId(Long payId) {
            this.payId = payId;
        }

        public String getPayStatus() {
            return payStatus;
        }

        public void setPayStatus(String payStatus) {
            this.payStatus = payStatus;
        }

}
