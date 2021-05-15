package airbnb;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class RoomController {

        @Autowired
        RoomRepository roomRepository;

        @RequestMapping(value = "/check/chkAndReqReserve",
                        method = RequestMethod.GET,
                        produces = "application/json;charset=UTF-8")

        public boolean chkAndReqReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
                System.out.println("##### /check/chkAndReqReserve  called #####");

                // Parameter로 받은 RoomID 추출
                long roomId = Long.valueOf(request.getParameter("roomId"));
                System.out.println("######################## chkAndReqReserve roomId : " + roomId);

                // RoomID 데이터 조회
                Optional<Room> res = roomRepository.findById(roomId);
                Room room = res.get(); // 조회한 ROOM 데이터
                System.out.println("######################## chkAndReqReserve room.getStatus() : " + room.getStatus());

                // room의 상태가 available이면 true
                boolean result = false;
                if(room.getStatus().equals("available")) {
                        result = true;
                } 

                System.out.println("######################## chkAndReqReserve Return : " + result);
                return result;
        }
 }
