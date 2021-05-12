package airbnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Review_table")
public class Review {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long reviewId;
    private Long roomId;
    private String content;

    @PostPersist
    public void onPostPersist(){

        ///////////////////////////////
        // Review 테이블 Insert 후 수행
        ///////////////////////////////

        // ReviewCreated Event 발생
        ReviewCreated reviewCreated = new ReviewCreated();
        BeanUtils.copyProperties(this, reviewCreated);
        reviewCreated.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){

        ///////////////////////////////
        // Review 테이블 Update 후 수행
        ///////////////////////////////

        // ReviewModified Event 발생
        ReviewModified reviewModified = new ReviewModified();
        BeanUtils.copyProperties(this, reviewModified);
        reviewModified.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){

        ////////////////////////////////
        // Review 테이블 Delete 전 수행
        ////////////////////////////////

        // ReviewDeleted Event 발생
        ReviewDeleted reviewDeleted = new ReviewDeleted();
        BeanUtils.copyProperties(this, reviewDeleted);
        reviewDeleted.publishAfterCommit();
    }


    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }




}
