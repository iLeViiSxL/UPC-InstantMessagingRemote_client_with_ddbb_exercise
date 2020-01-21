/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subscriber;

import util.Subscription_close;
import entity.Message;
import entity.Topic;
import java.awt.Color;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import main.SwingClient;

/**
 *
 * @author juanluis
 */
public class SubscriberImpl implements Subscriber {

    private JTextArea messages_TextArea;
    private JTextArea my_subscriptions_TextArea;
    private Map<Topic, Subscriber> my_subscriptions;

    public SubscriberImpl(SwingClient clientSwing) {
        this.messages_TextArea = clientSwing.messages_TextArea;
        this.my_subscriptions_TextArea = clientSwing.my_subscriptions_TextArea;
        this.my_subscriptions = clientSwing.my_subscriptions;
    }

    public void onClose(Subscription_close subs_close) {
        if (subs_close.cause == Subscription_close.Cause.PUBLISHER) {
            messages_TextArea.append("Topic: " + subs_close.topic.name
                    + " has been closed, no publishers left on that topic.\n");
        } else if (subs_close.cause == Subscription_close.Cause.SUBSCRIBER) {
            messages_TextArea.append("subscription on topic: " + subs_close.topic.name
                    + " has ended.\n");
        }
        my_subscriptions.remove(subs_close.topic);
        my_subscriptions_TextArea.setText("");
        for (Topic topic : my_subscriptions.keySet()) {
            my_subscriptions_TextArea.append(topic.name + "\n");
        }
    }

    public void onMessage(Message message) {
        SwingClient.ColorArea(messages_TextArea, message);
    }
}
