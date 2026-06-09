import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        
        int port;
        String serverIP;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the port to connect to: ");
        port = Integer.parseInt(sc.nextLine().trim());

        System.out.println("Enter the server address: ");
        serverIP = sc.nextLine().trim();

        while(true){

            System.out.print("Enter the message you want to send: ");
            String message = sc.nextLine().trim();

            byte[] buffer = message.getBytes();
            
            InetAddress hostIP = InetAddress.getByName(serverIP);

            DatagramPacket UDPpacket = new DatagramPacket(buffer, buffer.length, hostIP, port);
            
            System.out.println("Host at ip: " + hostIP.getHostAddress());
            System.out.println("Connecting to port no. : " + port);
            System.out.println("\nConnecting to Host...\n");
            
            DatagramSocket UDPsocket = new DatagramSocket();

            UDPsocket.send(UDPpacket);

            System.out.println("Data packet Sent!\n");
            

            UDPsocket.close();
        }
    }
}