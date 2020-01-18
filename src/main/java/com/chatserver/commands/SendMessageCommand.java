package com.chatserver.commands;

import com.chatserver.client_side.Client;

public class SendMessageCommand
{
    private String message;
    private Client sender;
    private Client receiver;
    
    
    public SendMessageCommand (
            String message ,
            Client sender ,
            Client receiver
    )
    {
        super();
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }


    
    public String getMessage ()
    {
        return message;
    }


    
    public Client getSender ()
    {
        return sender;
    }


    
    public Client getReceiver ()
    {
        return receiver;
    }
    
}
