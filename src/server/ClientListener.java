package server;

import java.net.Socket;
import java.util.Map;

import common.Utils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//5
public class ClientListener implements Runnable {

    private String connection_info;
    private Socket connection;
    private Server server;
    private boolean running;

    public ClientListener(String connection_info, Socket connection, Server server)
    {
        this.connection_info = connection_info;
        this.connection = connection;
        this.server = server;
        this.running = false;

    }

    public boolean isRunning()
    {
        return running;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }
   
    @Override
    public void run()
    {
        running = true;
        String message;
        while(running)
        {
            message = Utils.receiveMessage(connection);
            if(message.equals("QUIT"))
            {
                server.getClients().remove(connection_info);
                try 
                {
                    connection.close();
                } catch (IOException ex) 
                {
                    System.out.println("[ClientListener:Run] ->"+ ex.getMessage());
                }
                running = false;
            }
            else if (message.equals("GET_CONNECTED_USERS"))
            {
                System.out.println("Solicitação de atualizar lista de contatos...");
                String response = "";
                for(Map.Entry<String, ClientListener> pair: server.getClients().entrySet())
                {
                    response += (pair.getKey() + ";");
                }
                Utils.sendMessage(connection, response);
            }
            else
            {
                System.out.println("Recebido: "+ message);
            }        

        }

    }
    
}
