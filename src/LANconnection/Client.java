import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the message you want to send: ");
        String message = sc.nextLine().trim();
        sc.close();

        byte[] buffer = message.getBytes();
        int port = 1234;

        InetAddress hostIP = InetAddress.getLocalHost();

        DatagramPacket UDPpacket = new DatagramPacket(buffer, buffer.length, hostIP, port);

        System.out.println("Host at ip: " + hostIP);
        System.out.println("Connecting to port no. : " + port);
        System.out.println("\nConnecting to Host...\n");
        
        DatagramSocket UDPsocket = new DatagramSocket();
        UDPsocket.send(UDPpacket);

        System.out.println("Data packet Sent!\n");

        UDPsocket.close();
    }
}