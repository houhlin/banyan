package com.freedom.messagebus.client.handler.publish;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class RealPublisher extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealPublisher.class);

    @Override
    public void init(HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        try {
            for (Message msg : context.getMessages()) {
                IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());
                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      Constants.PUBSUB_ROUTING_KEY,
                                      msgBody,
                                      properties);
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
