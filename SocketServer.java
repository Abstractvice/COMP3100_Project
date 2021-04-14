import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer{
    
	// Here is our static ServerSocket variable
    private static ServerSocket server;
    // Here is our socket server port on which it will listen
    private static int port = 50000;
    
    public static void main(String args[]) throws IOException, ClassNotFoundException {
    	// Create the socket server object
        ServerSocket server = new ServerSocket(port);
        // Here, we keep listening indefinitely until we receive 'exit' call or the program
        // terminates
        while (true) {
            System.out.println("Waiting for client request");
            // Creating socket and waiting for client connection
            Socket socket = server.accept();
            // Read from socket to ObjectInputStream object
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // Convert ObjectInputStream object to a String
            String message = (String)ois.readObject();
            System.out.println("Message Received from client: " + message);
            
            // Create ObjectOutputStream object
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // Write object to Socket
            oos.writeObject("G'DAY");
            
            message = (String)ois.readObject();
            System.out.println("Message received from client: " + message);
            
            oos.writeObject("BYE");
            
            ois.close();
            oos.close();
            socket.close();
            // Terminate the server if client sends exit request
            if (message.equalsIgnoreCase("BYE")) break;
        }
        System.out.println("Shutting down Socket server!!");
        
        server.close();
    }

}