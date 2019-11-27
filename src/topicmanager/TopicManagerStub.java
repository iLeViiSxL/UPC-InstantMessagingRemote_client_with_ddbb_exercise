package topicmanager;

import apiREST.apiREST_Message;
import apiREST.apiREST_Publisher;
import apiREST.apiREST_Subscriber;
import apiREST.apiREST_Topic;
import entity.Message;
import util.Subscription_check;
import entity.Topic;
import util.Topic_check;
import entity.User;
import java.util.List;
import publisher.Publisher;
import publisher.PublisherStub;
import subscriber.Subscriber;
import webSocketService.WebSocketClient;

public class TopicManagerStub implements TopicManager {

    public User user;

    public TopicManagerStub(User user) {
        WebSocketClient.newInstance();
        this.user = user;
    }

    public void close() {
        WebSocketClient.close();
    }

    @Override
    public Publisher addPublisherToTopic(Topic topic) {
        PublisherStub p = new PublisherStub(topic);
        entity.Publisher publisher = new entity.Publisher();
        publisher.setTopic(topic);
        publisher.setUser(user);
        apiREST_Publisher.createPublisher(publisher);
        return p;
    }

    @Override
    public void removePublisherFromTopic(Topic topic) {
        entity.Publisher publisher = new entity.Publisher();
        publisher.setTopic(topic);
        publisher.setUser(user);
        apiREST_Publisher.deletePublisher(publisher);
    }

    @Override
    public Topic_check isTopic(Topic topic) {
        return apiREST_Topic.isTopic(topic);
    }

    @Override
    public List<Topic> topics() {
        return apiREST_Topic.allTopics();
    }

    @Override
    public Subscription_check subscribe(Topic topic, Subscriber subscriber) {
        System.out.println("TopicManagerStub : " + topic);
        if (isTopic(topic).isOpen) {
            WebSocketClient.addSubscriber(topic, subscriber);
            entity.Subscriber s = new entity.Subscriber();
            s.setTopic(topic);
            s.setUser(user);
            apiREST_Subscriber.createSubscriber(s);
            return new Subscription_check(topic, Subscription_check.Result.OKAY);
        } else {
            return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
        }
    }

    @Override
    public Subscription_check unsubscribe(Topic topic, Subscriber subscriber) {
        if (isTopic(topic).isOpen) {
            WebSocketClient.removeSubscriber(topic);
            entity.Subscriber s = new entity.Subscriber();
            s.setTopic(topic);
            s.setUser(user);
            apiREST_Subscriber.deleteSubscriber(s);
            return new Subscription_check(topic, Subscription_check.Result.OKAY);
        } else {
            return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
        }
    }

    @Override
    public Publisher publisherOf() {
        entity.Publisher publisher = apiREST_Publisher.PublisherOf(user);
        PublisherStub p = null;
        if(publisher!=null){
            p = new PublisherStub(publisher.getTopic());
        }  
        return p;
    }

    @Override
    public List<entity.Subscriber> mySubscriptions() {
        return apiREST_Subscriber.mySubscriptions(user);
    }

    @Override
    public List<Message> messagesFrom(Topic topic) {
        return apiREST_Message.messagesFromTopic(topic);
    }

}
