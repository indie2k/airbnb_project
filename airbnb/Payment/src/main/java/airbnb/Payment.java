package airbnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long payId;
    private Long rsvId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        ////////////////////////////
        // 결제 승인 된 경우
        ////////////////////////////

        // 이벤트 발행 -> PaymentApproved
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){

        //////////////////////
        // 결제 취소 된 경우
        //////////////////////

        // 이벤트 발행 -> PaymentCancelled
        PaymentCancelled paymentCancelled = new PaymentCancelled();
        BeanUtils.copyProperties(this, paymentCancelled);
        paymentCancelled.publishAfterCommit();

    }

    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
    public Long getRsvId() {
        return rsvId;
    }

    public void setRsvId(Long rsvId) {
        this.rsvId = rsvId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
