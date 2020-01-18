package com.chatserver.client_side;


import java.net.Socket;


public class Client
{

    private final String name;

    private final Socket socket;

    public Client ( String name , Socket socket )
    {
        super();
        this.name = name;
        this.socket = socket;
    }

    public String getName ()
    {
        return name;
    }

    public Socket getSocket ()
    {
        return socket;
    }

    @Override
    public boolean equals ( Object obj )
    {
        return this.name.equals( ( (Client) obj ).name );
    }
    
    @Override
    public String toString ()
    {
        return this.name;
    }
}
