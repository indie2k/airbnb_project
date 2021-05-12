package airbnb;

public class PaymentApproved extends AbstractEvent {

    private Long payId;
    private Long rsvId;
    private String status;

    public PaymentApproved(){
        super();
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
