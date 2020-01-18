package com.chatserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class StartClient
{

    public static final int    port    = StartServer.port;

    public static final String address = "127.0.1";

    public static void main ( String [] args ) throws Exception
    {
        Scanner in = new Scanner( System.in );

        System.out.print( "Your name is: " );
        String name = in.nextLine();

        Socket client = new Socket(
                address , port
        );

        InputStream receiver = client.getInputStream();
        OutputStream outputter = client.getOutputStream();

        byte [] connectionMsg = ( "register " + name + "\r" ).getBytes();
        outputter.write( connectionMsg );

        Receiver receivingFromServer = new Receiver( receiver );

        receivingFromServer.start();

        String command;
        System.out.print( ">" );
        while (
            ! ( command = in.nextLine() ).equalsIgnoreCase( "exit" )
                    && !command.equalsIgnoreCase( "disconnect" )
        )
        {
            System.out.println( "Sending..." );
            outputter.write( ( command + '\r' ).getBytes() );
            System.out.println( "Sent" );
            Thread.sleep( 10 );
            System.out.print( ">" );
        }

        byte [] disconnectMsg = ( "disconnect " + name + "\r" ).getBytes();
        try
        {
            outputter.write( disconnectMsg );
        } catch ( SocketException e )
        {
        }
        receivingFromServer.stopReceiving();
        receivingFromServer.join();

        in.close();
        client.close();
    }

    private static class Receiver extends Thread
    {

        boolean     receive = false;

        InputStream receiver;

        public Receiver ( InputStream receiver )
        {
            this.receiver = receiver;
        }

        @Override
        public void run ()
        {
            this.receive = true;
            
            while ( receive )
            {
                try
                {
                    StringBuilder response = new StringBuilder( "From server> " );
                    char next = (char) receiver.read();
                    while (
                        next != '\r'
                                && next != '\0' 
                                && receive
                    )
                    {
                        response.append( next );
                        next = (char) receiver.read();
                    }
                    System.out.println( response.toString() );
                } catch ( IOException e )
                {
                }
            }
        }

        public void stopReceiving ()
        {
            this.receive = false;
            
        }
    }
}
