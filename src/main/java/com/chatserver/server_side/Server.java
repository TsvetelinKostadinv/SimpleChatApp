package com.chatserver.server_side;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.chatserver.client_side.Client;
import com.chatserver.commands.SendMessageCommand;


public class Server implements Serializable , Closeable
{

    /**
     * 
     */
    private static final long    serialVersionUID    = 1L;

    public static final int      DEFAULT_SERVER_PORT = 5050;

    private final ServerSocket   socket;

    private boolean              running             = false;

    private Set< ClientHandler > handlers            = new LinkedHashSet<>();

    public Server () throws IOException
    {
        this.socket = new ServerSocket( DEFAULT_SERVER_PORT );
    }

    public Server ( int port ) throws IOException
    {
        this.socket = new ServerSocket( port );
    }

    public void start ()
    {
        this.running = true;

        final Runnable receiver = () -> {
            print( "Starting server" );
            while ( running )
            {
                try
                {
                    Socket client = this.socket.accept();
                    print( "Got new user!" );

                    InputStream receiving = client.getInputStream();

                    char next = (char) receiving.read();
                    final StringBuilder commandSB = new StringBuilder();

                    if ( receiving.available() > 0 )
                    {
                        while (
                            next != '\r'
                                    && next != '\0'
                        )
                        {
                            commandSB.append( next );
                            next = (char) receiving.read();
                        }
                        print(
                                "Received command: "
                                        + commandSB.toString()
                        );
                    } else
                    {
                        System.err.println( "No available input!" );
                        byte [] msg = ( "We need your username in the form of a command:"
                                + " register <username>"
                                + ", to connect please input it" ).getBytes();
                        client.getOutputStream().write( msg );
                    }

                    final String [] command = commandSB.toString().split( " " );

                    Client clientCredentials = null;

                    if (
                        command[0].equalsIgnoreCase( "register" )
                                && clientIsNotRegisteredAlready( command[1] )
                    )
                    {
                        clientCredentials = new Client( command[1] , client );
                    }

                    print( "Creating the handler..." );
                    ClientHandler ch = new ClientHandler(
                            clientCredentials , this
                    );

                    this.handlers.add( ch );

                    Thread receivingMessgages = new Thread( () -> {
                        try
                        {
                            ch.startReceiving();
                        } catch ( IOException e )
                        {
                            e.printStackTrace();
                        }
                    } );

                    receivingMessgages.start();

                } catch ( IOException e )
                {
                    // e.printStackTrace();
                }
            }
        };

        new Thread( receiver ).start();
    }

    private boolean clientIsNotRegisteredAlready ( String name )
    {
        for ( ClientHandler clientHandler : handlers )
        {
            if ( clientHandler.getAssociatedClient().getName().equals( name ) )
                return false;
        }
        return true;
    }

    public void stop () throws IOException
    {
        print( "Stopping server" );
        this.running = false;

        for ( ClientHandler clientHandler : handlers )
        {
            clientHandler.stopReceiving();
        }

        this.socket.close();
    }

    @Override
    public void close () throws IOException
    {
        this.stop();
    }

    public ServerSocket getSocket ()
    {
        return socket;
    }

    public Set< ClientHandler > getHandlers ()
    {
        return Collections.unmodifiableSet( this.handlers );
    }

    public boolean isRunning ()
    {
        return running;
    }

    public void send ( SendMessageCommand sendCommand )
    {
        for ( ClientHandler clientHandler : handlers )
        {
            if (
                clientHandler.getAssociatedClient().getName()
                        .equals( sendCommand.getReceiver().getName() )
            )
            {
                print(
                        "Sending message from " + sendCommand.getSender()
                                + " to " + sendCommand.getReceiver()
                );
                clientHandler.receiveMessage( sendCommand );
                break;
            }
        }
    }

    public void disconnectUser ( ClientHandler client )
    {
        client.receiveMessage(
                new SendMessageCommand(
                        "Sucessfully disconnected" , new Client(
                                "Server" , null
                        ) , client.getAssociatedClient()
                )
        );
        client.stopReceiving();
        this.handlers.remove( client );
    }

    public static final void print ( String str )
    {
        System.out.println( "<Server>: " + str );
    }
}
