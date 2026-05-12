import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
    
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        
        String message = "Hello from Client!";
        byte[] buffer = message.getBytes();
        int port = 1234;

        InetAddress host = InetAddress.getLocalHost();

        DatagramPacket UDPpacket = new DatagramPacket(buffer, buffer.length, host, port);

        System.out.println("\nConnecting to Host...\n");
        
        DatagramSocket UDPsocket = new DatagramSocket();
        UDPsocket.send(UDPpacket);

        System.out.println("Data packet Sent!\n");

        UDPsocket.close();
    }
}