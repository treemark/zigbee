import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.dongle.cc2531.ZigBeeDongleTiCc2531;
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.transport.ZigBeeTransportTransmit;
//import com.zsmartsystems.zigbee.zcl.protocol.ZclEndpoint;


public class ListZigbeeLights {
    public static void main(String[] args) throws Exception {
        String name="";
        int baud=19200;
        ZigBeePort.FlowControl flow=ZigBeePort.FlowControl.FLOWCONTROL_OUT_RTSCTS;
        ZigBeePort port=new ZigBeeSerialPort(name,baud,flow);
        ZigBeeTransportTransmit transport= new ZigBeeDongleTiCc2531(port);
        ZigBeeNetworkManager manager = new ZigBeeNetworkManager(transport);
        manager.startup(true);

//        ZigBeeDongle dongle = new ZigBeeDongle("/dev/ttyUSB0"); // open dongle on port
//        ZigBeeNetwork network = dongle.getNetwork();
        // Wait for network initialization or discovery if needed

        for (ZigBeeNode node : manager.getNodes()) {
            for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
                // Check for the OnOff (0x0006) or LevelControl (0x0008) clusters typical for bulbs
//                if (endpoint.getInputCluster(ZclClusterType.ON_OFF) != null ||
//                    endpoint.getInputCluster(ZclClusterType.LEVEL_CONTROL) != null) {
//                    System.out.printf(
//                      "Found lightbulb: Node IEEE Address=%s, Endpoint=%d, ProfileId=%04X, DeviceId=%04X\n",
//                      node.getIeeeAddress(), endpoint.getEndpointId(),
//                      endpoint.getProfileId(), endpoint.getDeviceId()
//                    );
//                }
            }
        }

        manager.shutdown();
    }
}
