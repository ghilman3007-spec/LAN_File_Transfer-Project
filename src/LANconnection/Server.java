import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {

    public static void main(String[] args) throws SocketException, IOException{
        
        int port = 1234;
        
        while(true){
            
            // server socket
            DatagramSocket UDPsocket = new DatagramSocket(port);

            // data packet buffer
            byte[] PacketBuffer = new byte[8192];
            
            // data packet initialization
            DatagramPacket UDPpacket = new DatagramPacket(PacketBuffer, PacketBuffer.length);
            
            System.out.println("Listening from local address: " + UDPsocket.getLocalAddress());
            System.out.println("Using local port no. : " + UDPsocket.getLocalPort());
            System.out.println("Sniffing the network...\n");
            UDPsocket.receive(UDPpacket);

            InetAddress remoteClient = UDPpacket.getAddress();
            int remotePort = UDPpacket.getPort();
            String message = new String(UDPpacket.getData(), 0, UDPpacket.getLength());

            System.out.println("Received the following data from a remote host:\n");
            System.out.println("Client Address (Guest IP): " + remoteClient);
            System.out.println("Client Port: " + remotePort);
            System.out.println("Data Received: " + message);
            System.out.println("\n\n");
            UDPsocket.close();
        }

    }
}
