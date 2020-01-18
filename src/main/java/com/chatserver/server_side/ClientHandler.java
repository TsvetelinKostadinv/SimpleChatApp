package com.chatserver.server_side;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.chatserver.client_side.Client;
import com.chatserver.commands.SendMessageCommand;


public class ClientHandler
{

    private final Client associatedClient;

    private final Server server;

    private boolean      running = false;

    public ClientHandler ( Client associatedClient , Server server )
    {
        super();
        this.associatedClient = associatedClient;
        this.server = server;
    }

    public Client getAssociatedClient ()
    {
        return associatedClient;
    }

    @Override
    public boolean equals ( Object obj )
    {
        return this.associatedClient
                .equals( ( (ClientHandler) obj ).associatedClient );
    }

    @Override
    public String toString ()
    {
        return this.associatedClient.toString();
    }

    public void startReceiving () throws IOException
    {
        Server.print( "Thread of " + associatedClient.getName() + " started " );
        this.running = true;

        InputStream input = this.associatedClient.getSocket().getInputStream();

        while ( running )
        {
            StringBuilder commandBuffer = new StringBuilder();

            char next = (char) input.read();

            while (
                next != '\r'
                        && next != '\0'
            )
            {
                commandBuffer.append( next );
                next = (char) input.read();
            }

            Server.print(
                    "Received(" + this.associatedClient.getName() + ")"
                            + commandBuffer.toString()
            );

            String [] command = commandBuffer.toString().split( " " );

            switch ( command[0] )
                {
                    case "send" :
                    {

                        SendMessageCommand sendCommand = new SendMessageCommand(
                                command[2] , associatedClient ,
                                new Client( command[1] , null )
                        );
                        server.send( sendCommand );
                    }
                        break;
                    case "disconnect" :
                    {
                        server.disconnectUser( this );
                    }
                }
        }

    }

    public void stopReceiving ()
    {
        this.running = false;
    }

    public void receiveMessage ( SendMessageCommand sendCommand )
    {
        try
        {
            associatedClient.getSocket().getOutputStream()
                    .write(
                            ( sendCommand.getSender().getName() + " : "
                                    + sendCommand.getMessage() + '\r' )
                                            .getBytes()
                    );
            Server.print(
                    "Sent to user " + sendCommand.getReceiver().getName()
            );
        } catch ( IOException e )
        {
            // e.printStackTrace();
        }

    }

}
