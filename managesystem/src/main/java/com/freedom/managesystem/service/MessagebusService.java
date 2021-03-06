package com.freedom.managesystem.service;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class MessagebusService {

    private static final Log logger = LogFactory.getLog(MessagebusService.class);
    private Messagebus client;

    public MessagebusService() {
        client = new Messagebus(Constants.MESSAGEBUS_WEB_APP_ID);
        client.setPubsuberHost(Constants.ZK_HOST);
        client.setPubsuberPort(Constants.ZK_PORT);
    }

    public void produceMessage(String queueName, Message msg) {
        try {
            client.open();
            client.produce(msg, queueName);
        } catch (MessagebusConnectedFailedException e) {
            logger.error("[produceMessage] occurs a MessagebusConnectedFailedException : " + e.getMessage());
        } catch (MessagebusUnOpenException e) {
            logger.error("[MessagebusUnOpenException] occurs a MessagebusUnOpenException : " + e.getMessage());
        } finally {
            client.close();
        }
    }

    public void produceDBOperate(String cmd, String tbName) {
        QueueMessage operateMsg = (QueueMessage) MessageFactory.createMessage(MessageType.QueueMessage);
        Map<String, Object> headerMap = new HashMap<>(2);
        headerMap.put("COMMAND", cmd);
        headerMap.put("TABLE", tbName);
        operateMsg.getMessageHeader().setHeaders(headerMap);
        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent(new byte[0]);
        operateMsg.setMessageBody(body);

        this.produceMessage(Constants.SERVER_QUEUE_NAME, operateMsg);
    }

}
