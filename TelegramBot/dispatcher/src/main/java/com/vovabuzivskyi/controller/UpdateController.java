package com.vovabuzivskyi.controller;


import com.vovabuzivskyi.service.UpdateProducer;
import com.vovabuzivskyi.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.vovabuzivskyi.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {

    private final MessageUtils messageUtils;
    private TelegramBot telegramBot;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void  registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


public void processUpdate(Update update) {
    if (update == null) {
        log.error("Received update is null");
        return;
    }
    if (update.getMessage() != null) {
        distributeMessageByType(update);
    } else {
        log.error("Received unsupported type : " + update);
    }
}

    private void distributeMessageByType(Update update) {
       var message = update.getMessage();
       if(message.getText() != null){
           proccesTextMessage(update);
       }else if(message.getDocument() != null){
           proccedDocumentMessage(update);
       }else if(message.getPhoto() != null){
           proccesPhotoMessage(update);
       }else{
           setUnsupportedMessageTypeView(update);
       }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update, "Unsupported type of message!");
        setVIew(sendMessage);
    }

    private void setVIew(SendMessage sendMessage) {
        telegramBot.sendAnswerMassage(sendMessage);
    }

    private void proccedDocumentMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update, "File received and processed , please wait...");
        setVIew(sendMessage);
    }

    private void proccesPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

    private void proccesTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }

}

