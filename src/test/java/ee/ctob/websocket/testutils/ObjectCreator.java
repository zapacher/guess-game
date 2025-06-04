package ee.ctob.websocket.testutils;

import ee.ctob.websocket.data.Request;

import java.util.List;
import java.util.UUID;

public class ObjectCreator {

    public static List<Request> nonValidRequests(UUID validationUUID) {
        Request request1 = new Request();
        request1.setValidationUUID(null);
        request1.setNumber(7);
        request1.setAmount(10);

        Request request2 = new Request();
        request2.setValidationUUID(validationUUID);
        request2.setNumber(0);
        request2.setAmount(10);

        Request request3 = new Request();
        request3.setValidationUUID(validationUUID);
        request3.setNumber(11);
        request3.setAmount(10);

        Request request4 = new Request();
        request4.setValidationUUID(validationUUID);
        request4.setNumber(7);
        request4.setAmount(0);
        return List.of(request1, request2, request3, request4);
    }

    public static Request validRequests(UUID validationUUID, String nickname) {
        Request request = new Request();
        request.setValidationUUID(validationUUID);
        request.setNumber(7);
        request.setAmount(10.00);
        request.setNickname(nickname);
        return request;
    }
}
