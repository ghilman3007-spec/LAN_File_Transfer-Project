import LANconnection.LANServer;
import LANconnection.LANClient;

import java.util.Scanner;

/**
 * Main entry point for the LAN File Transfer System.
 * A Peer-Peer connection system that Prompts the user 
 * to run as Server/Receiver or Client/Sender depending
 * on the context of File Sharing.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        String choice;
        Scanner scanner = new Scanner(System.in);

        System.out.print("\033[H\033[J");
        System.out.println("<:=== LAN File Transfer System ===:>");
        System.out.println("Run as: \n\t1. Receiver (Server) \n\t2. Sender(Client) \n\t3. Exit application");

        do{

            System.out.print("Enter choice: ");
            choice = scanner.nextLine().trim();
            
            // Act as a Server
            if (choice.equals("1")) {

                System.out.print("Enter port to listen on (default 5000): ");
                String portStr = scanner.nextLine().trim();

                int port = portStr.isEmpty() ? 5000 : Integer.parseInt(portStr);

                LANServer server = new LANServer(port);
                server.start();
            
            } 
            // Act as a Client
            else if (choice.equals("2")) {
                
                System.out.print("Enter server IP address: ");
                String ip = scanner.nextLine().trim();
                
                System.out.print("Enter server port (default 5000): ");
                String portStr = scanner.nextLine().trim();
                int port = portStr.isEmpty() ? 5000 : Integer.parseInt(portStr);
                
                System.out.print("Enter path of file to send: ");
                String filePath = scanner.nextLine().trim();

                LANClient client = new LANClient(ip, port);
                client.sendFile(filePath);

            } 
            // ignore the choice
            else if(!choice.equals("3")){
                System.out.println("Invalid choice. Exiting.");
            }

        }while(!choice.equals("3"));
        
        scanner.close();
    }
}
