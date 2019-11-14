package webSocketService;

import apiREST.Cons;
import com.google.gson.Gson;
import entity.Message;
import entity.Topic;
import util.Subscription_close;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import subscriber.Subscriber;
import subscriber.SubscriberImpl;
import util.Subscription_request;

@ClientEndpoint
public class WebSocketClient {

    static Map<Topic, Subscriber> subscriberMap;
    static Session session;

    public static void newInstance() {
        subscriberMap = new HashMap<Topic, Subscriber>();
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(WebSocketClient.class,
                    URI.create(Cons.SERVER_WEBSOCKET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addSubscriber(Topic topic, Subscriber subscriber) {
        try {

            System.out.println("TEST");
            Subscription_request s = new Subscription_request(topic, Subscription_request.Type.ADD);
            String jObj = new Gson().toJson(s);
            session.getBasicRemote().sendText(jObj);
            System.out.println("jObj : " + jObj);
            subscriberMap.put(topic, subscriber);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void removeSubscriber(Topic topic) {
        try {

            Subscription_request s = new Subscription_request(topic, Subscription_request.Type.REMOVE);
            String jObj = new Gson().toJson(s);
            session.getBasicRemote().sendText(jObj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String json) {

        Gson gson = new Gson();
        Subscription_close subs_close = gson.fromJson(json, Subscription_close.class);

        //ordinary message from topic:
        if (subs_close.cause == null) {
            Message message = gson.fromJson(json, Message.class);
            System.out.println("MESSAGE : " + message.content);
            for (Map.Entry<Topic, Subscriber> entry : subscriberMap.entrySet()) {
                Topic key = entry.getKey();
                Subscriber value = entry.getValue();
                if (key.equals(subs_close.topic)) {
                    value.onMessage(message);
                }
            }
        } //ending subscription message:
        else {
            System.out.println("onMessage : " + subs_close.cause);
            SubscriberImpl s = (SubscriberImpl) subscriberMap.get(subs_close.topic);
            s.onClose(subs_close);
            subscriberMap.remove(subs_close.topic);
        }
    }

}
