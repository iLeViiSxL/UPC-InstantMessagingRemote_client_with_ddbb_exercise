package main;

import entity.Message;
import util.Subscription_check;
import entity.Topic;
import subscriber.SubscriberImpl;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import publisher.Publisher;
import subscriber.Subscriber;
import topicmanager.TopicManager;
import topicmanager.TopicManagerStub;
import webSocketService.WebSocketClient;

public class SwingClient {

    TopicManager topicManager;
    public Map<Topic, Subscriber> my_subscriptions;
    Publisher publisher;
    Topic publisherTopic;
    String login;

    JFrame frame;
    JTextArea topic_list_TextArea;
    public JTextArea messages_TextArea;
    public JTextArea my_subscriptions_TextArea;
    JTextArea publisher_TextArea;
    JTextField argument_TextField;

    public SwingClient(TopicManager topicManager) {
        this.topicManager = topicManager;
        my_subscriptions = new HashMap<Topic, Subscriber>();
        publisher = null;
        publisherTopic = null;
    }

    public void createAndShowGUI() {

        login = ((TopicManagerStub) topicManager).user.getLogin();
        frame = new JFrame("Publisher/Subscriber demo, user : " + login);
        frame.setSize(300, 300);
        frame.addWindowListener(new CloseWindowHandler());

        topic_list_TextArea = new JTextArea(5, 10);
        messages_TextArea = new JTextArea(10, 20);
        my_subscriptions_TextArea = new JTextArea(5, 10);
        publisher_TextArea = new JTextArea(1, 10);
        argument_TextField = new JTextField(20);

        JButton show_topics_button = new JButton("show Topics");
        JButton new_publisher_button = new JButton("new Publisher");
        JButton new_subscriber_button = new JButton("new Subscriber");
        JButton to_unsubscribe_button = new JButton("to unsubscribe");
        JButton to_post_an_event_button = new JButton("post an event");
        JButton to_close_the_app = new JButton("close app.");

        show_topics_button.addActionListener(new showTopicsHandler());
        new_publisher_button.addActionListener(new newPublisherHandler());
        new_subscriber_button.addActionListener(new newSubscriberHandler());
        to_unsubscribe_button.addActionListener(new UnsubscribeHandler());
        to_post_an_event_button.addActionListener(new postEventHandler());
        to_close_the_app.addActionListener(new CloseAppHandler());

        JPanel buttonsPannel = new JPanel(new FlowLayout());
        buttonsPannel.add(show_topics_button);
        buttonsPannel.add(new_publisher_button);
        buttonsPannel.add(new_subscriber_button);
        buttonsPannel.add(to_unsubscribe_button);
        buttonsPannel.add(to_post_an_event_button);
        buttonsPannel.add(to_close_the_app);

        JPanel argumentP = new JPanel(new FlowLayout());
        argumentP.add(new JLabel("Write content to set a new_publisher / new_subscriber / unsubscribe / post_event:"));
        argumentP.add(argument_TextField);

        JPanel topicsP = new JPanel();
        topicsP.setLayout(new BoxLayout(topicsP, BoxLayout.PAGE_AXIS));
        topicsP.add(new JLabel("Topics:"));
        topicsP.add(topic_list_TextArea);
        topicsP.add(new JScrollPane(topic_list_TextArea));
        topicsP.add(new JLabel("My Subscriptions:"));
        topicsP.add(my_subscriptions_TextArea);
        topicsP.add(new JScrollPane(my_subscriptions_TextArea));
        topicsP.add(new JLabel("I'm Publisher of topic:"));
        topicsP.add(publisher_TextArea);
        topicsP.add(new JScrollPane(publisher_TextArea));

        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
        messagesPanel.add(new JLabel("Messages:"));
        messagesPanel.add(messages_TextArea);
        messagesPanel.add(new JScrollPane(messages_TextArea));

        Container mainPanel = frame.getContentPane();
        mainPanel.add(buttonsPannel, BorderLayout.PAGE_START);
        mainPanel.add(messagesPanel, BorderLayout.CENTER);
        mainPanel.add(argumentP, BorderLayout.PAGE_END);
        mainPanel.add(topicsP, BorderLayout.LINE_START);

        //this is where you restore the user profile:
        clientSetup();

        frame.pack();
        frame.setVisible(true);
    }

    private void clientSetup() {
        //Hints:
        // Use the topicManager to:
        // - Restore publisher
        // - Restore susbscriptions
        // - For each subscription AMONG other stuff
        // - Don't forget to
        // - WebSocketClient.addSubscriber(topic, my_subscriptions.get(topic));
        // - Retrieve list of messages and show them on screen

        // - Restore publisher
        if (topicManager != null) {

        }
        publisher = topicManager.publisherOf();

        // - Restore susbscriptions
        List<entity.Subscriber> initSubscribers = topicManager.mySubscriptions();

        initSubscribers.forEach(sub -> {
            SubscriberImpl subscriberImpl = new SubscriberImpl(SwingClient.this);
            my_subscriptions.put(sub.getTopic(), subscriberImpl);
            WebSocketClient.addSubscriber(sub.getTopic(), subscriberImpl);
            java.util.List<Message> arMessages = apiREST.apiREST_Message.messagesFromTopic(sub.getTopic());
            for (Message m : arMessages) {
                messages_TextArea.append(m.topic + " : " + m.content + "\n");
            }
        });

    }

    class showTopicsHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            java.util.List<Topic> arrayListTopics = topicManager.topics();
            topic_list_TextArea.setText("");
            for (Topic t : arrayListTopics) {
                topic_list_TextArea.append(t.name + "\r\n");
            }
        }
    }

    class newPublisherHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String publisherTopicText = argument_TextField.getText();
            Topic t = new Topic(publisherTopicText);
            if (publisherTopic != null) {
                topicManager.removePublisherFromTopic(publisherTopic);
            }
            publisher = topicManager.addPublisherToTopic(t);
            publisherTopic = new Topic(argument_TextField.getText());
            publisher_TextArea.setText(publisherTopicText);
        }
    }

    class newSubscriberHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            SubscriberImpl subscriberImpl = new SubscriberImpl(SwingClient.this);
            String subscriberTopicText = argument_TextField.getText();
            Subscription_check subscription_check = topicManager.subscribe(new Topic(subscriberTopicText), subscriberImpl);
            if (subscription_check.result == Subscription_check.Result.OKAY) {
                my_subscriptions.put(subscription_check.topic, subscriberImpl);
                messages_TextArea.append("Subscribe successful to: " + subscriberTopicText + "\r\n");
                publisher.publish(new Message(subscription_check.topic, "New subscriber [" + login + "]"));
            } else {
                messages_TextArea.append("Impossible to subscribe to: " + subscriberTopicText + " Cause : " + subscription_check.result + "\r\n");
            }
            Iterator hmIterator = my_subscriptions.entrySet().iterator();
            my_subscriptions_TextArea.setText("");
            while (hmIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) hmIterator.next();
                my_subscriptions_TextArea.append(((Topic) mapElement.getKey()).name + "\r\n");
            }

        }
    }

    class UnsubscribeHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String subscriberTopicText = argument_TextField.getText();
            Topic t = new Topic(subscriberTopicText);

            SubscriberImpl subscriberImpl = (SubscriberImpl) my_subscriptions.get(t);
//            System.out.println("subscriberImpl : "+ subscriberImpl.toString());
//            if (subscriberImpl != null) {
//                subscriberImpl.onClose(new Subscription_close(new Topic(subscriberTopicText), Subscription_close.Cause.SUBSCRIBER));
//            }
            publisher.publish(new Message(t, "Subscriber [" + login + "] is leaving.. "));
            Subscription_check subscription_check = topicManager.unsubscribe(t, subscriberImpl);

            my_subscriptions.remove(t);

        }
    }

    class postEventHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (publisherTopic == null) {
                messages_TextArea.append("Sorry you're not a publisher");
            } else {
                String subscriberTopicText = argument_TextField.getText();
                System.out.println("");
                Message msg = new Message(publisherTopic, subscriberTopicText);
                publisher.publish(msg);
                ColorArea(messages_TextArea, msg);
            }

        }
    }

    public static void ColorArea(JTextArea j, Message message) {
        try {

            boolean color = false;

            j.append(message.topic.name + ": ");
            String lastWord = message.content.substring(message.content.lastIndexOf(" ") + 1);
            System.out.println("lastWord : " + lastWord);

            int lenghtTextArea = j.getText().length();

            System.out.println("lenghtTextArea : " + lenghtTextArea);
            System.out.println("message.content : " + message.content.length());
            
            Highlighter.HighlightPainter Painter = new DefaultHighlighter.DefaultHighlightPainter(Color.white);
            
            switch (lastWord) {
                case "/red":
                    Painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
                    color = true;
                    break;
                case "/yellow":
                    Painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                    color = true;
                    break;
                case "/green":
                    Painter = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
                    color = true;
                    break;
                default:

            }

            if (color) {
                String newString = message.content.substring(0, message.content.length() - lastWord.length());
                j.append(newString + "\n");
                j.getHighlighter().addHighlight(lenghtTextArea, lenghtTextArea + newString.length()-1 , Painter);

            } else {
                j.append(message.content + "\n");
                j.getHighlighter().addHighlight(lenghtTextArea, lenghtTextArea + message.content.length()-1 , Painter);
            }

        } catch (BadLocationException ex) {
            Logger.getLogger(SubscriberImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class CloseAppHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println("all users closed");
            System.exit(0);
        }
    }

    class CloseWindowHandler implements WindowListener {

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {

            //...
            System.out.println("one user closed");
            System.exit(0);
        }
    }
}
