
package airbnb;

public class ReservationConfirmed extends AbstractEvent {

    private Long rsvId;
    private Long roomId;
    private String status;


    public ReservationConfirmed(){
        super();
    }
    
    public Long getRsvId() {
        return rsvId;
    }

    public void setRsvId(Long rsvId) {
        this.rsvId = rsvId;
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
}

