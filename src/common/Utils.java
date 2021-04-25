package common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Utils {
    
    public static boolean sendMessage(Socket connection, String message)
    {
        try
        {
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            output.writeObject(message);
            return true;
        }
        catch (IOException ex)
        {
            System.err.println("[ERROR:sendmessage] ->" + ex.getMessage());
        }        
        return false;
    }

    public static String receiveMessage(Socket connection)
    {
        String response = null;
        try
        {
            ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
            response = (String) input.readObject();
        }
        catch(IOException ex)
        {
            System.err.println("[ERROR:receivemessage] ->" + ex.getMessage());
        }
        catch(ClassNotFoundException ex)
        {
            System.err.println("[ERROR:receivemessage] ->" + ex.getMessage());
        }

        return response;
    }
}
