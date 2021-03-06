package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 2/23/15.
 */
public class ProduceConsume {

    private static final Log logger = LogFactory.getLog(ProduceConsume.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        produce();

        //sleep
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        ConsumeWithPushStyle();

        //or
//        consumeWithPullStyle();

        //or async consume
        asyncConsume();
    }

    private static void produce() {
        //crm
        String appid = "djB5l1n7PbFsszF5817JOon2895El1KP";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        client.produce(msg, "erp");

        client.close();
    }

    private static void consumeWithPullStyle() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        List<Message> msgs = client.consume(1);

        client.close();

        for (Message msg : msgs) {
            logger.info(msg.getMessageHeader().getMessageId());
        }
    }

    private static void ConsumeWithPushStyle() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        client.consume(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 5, TimeUnit.SECONDS);

        client.close();
    }

    private static void asyncConsume() {
        AsyncConsumeThread asyncConsumeThread = new AsyncConsumeThread();
        asyncConsumeThread.startup();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncConsumeThread.shutdown();
    }

    private static class AsyncConsumeThread implements Runnable {

        private Thread currentThread;

        public AsyncConsumeThread() {
            this.currentThread = new Thread(this);
            this.currentThread.setName("AsyncConsumeThread");
            this.currentThread.setDaemon(true);
        }

        @Override
        public void run() {
            //erp
            String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
            Messagebus client = new Messagebus(appid);
            client.setPubsuberHost(host);
            client.setPubsuberPort(port);

            try {
                client.open();

                //long long time
                client.consume(new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message) {
                        logger.info(message.getMessageHeader().getMessageId());
                    }
                }, Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (MessagebusConnectedFailedException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        public void startup() {
            this.currentThread.start();
        }

        public void shutdown() {
            this.currentThread.interrupt();
        }
    }

}
