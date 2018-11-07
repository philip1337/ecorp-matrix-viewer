package app;


import java.net.*;
import java.util.*;

public class Main {
    private static final int RANDOM_PORT = 4444;

    public static void main( String[] args )
            throws Exception
    {
        InetAddress addr = getBroadcastAddrs().get(0);
        if (addr == null)
            return;
        DatagramSocket dsock = new DatagramSocket();
        dsock.setBroadcast(true);
        byte[] send = "Hello World".getBytes( "UTF-8" );
        DatagramPacket data = new DatagramPacket( send, send.length, addr, 59685 );
        dsock.send( data );
    }

    public static List<InetAddress> getBroadcastAddrs() throws SocketException {
        Set<InetAddress> set = new LinkedHashSet<>();
        Enumeration<NetworkInterface> nicList = NetworkInterface.
                getNetworkInterfaces();
        for( ; nicList.hasMoreElements(); ) {
            NetworkInterface nic = nicList.nextElement();
            if( nic.isUp() && !nic.isLoopback() )  {
                for( InterfaceAddress ia : nic.getInterfaceAddresses() ) {
                    if (ia.getBroadcast() == null)
                        continue;
                    set.add(ia.getBroadcast());
                    System.out.printf("broadcast: %s\n", ia.getBroadcast());
                }
            }
        }
        return Arrays.asList( set.toArray( new InetAddress[0] ) );

    }
}
