package net;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class Interfaces {
    /**
     * List broadcast addresses
     * @throws SocketException
     */
    public static List<InetAddress> GetBroadcast() throws SocketException {
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
                }
            }
        }
        return Arrays.asList( set.toArray( new InetAddress[0] ) );
    }

    /**
     * List public ip addresses
     * @throws SocketException
     */
    public static List<InetAddress> GetPublic() throws SocketException {
        Set<InetAddress> set = new LinkedHashSet<>();
        Enumeration<NetworkInterface> nicList = NetworkInterface.
                getNetworkInterfaces();
        for( ; nicList.hasMoreElements(); ) {
            NetworkInterface nic = nicList.nextElement();
            if( nic.isUp() && !nic.isLoopback() )  {
                for( InterfaceAddress ia : nic.getInterfaceAddresses() ) {
                    if (ia.getAddress().isSiteLocalAddress())
                        set.add(ia.getAddress());
                }
            }
        }
        return Arrays.asList( set.toArray( new InetAddress[0] ) );
    }
}
