import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

public class ZigbeeMqttClient implements MqttCallback {
    private MqttClient client;

    public void start(String brokerUrl) throws MqttException {
        client = new MqttClient(brokerUrl, MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.setCallback(this);
        client.connect(options);

        // Subscribe to device topic to receive state updates
        client.subscribe("zigbee2mqtt/myDevice");
        client.subscribe("zigbee2mqtt/bridge/event");
    }

    public void sendCommand(String deviceTopic, String commandJson) throws MqttException {
        MqttMessage message = new MqttMessage(commandJson.getBytes());
        message.setQos(1);
        client.publish(deviceTopic + "/set", message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost! " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Message arrived on topic " + topic + ": " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery complete: " + token.getResponse());
    }

    public static void main(String[] args) throws MqttException, InterruptedException {
        ZigbeeMqttClient zigbeeClient = new ZigbeeMqttClient();
//        zigbeeClient.start("tcp://192.168.86.212:1883");  // MQTT broker IP and port here
        zigbeeClient.start("tcp://192.168.86.212:6682");  // MQTT broker IP and port here
//        PORT     STATE SERVICE
//        6668/tcp open  irc
//        6682/tcp open  unknown

        // Example: toggle device state
//        zigbeeClient.sendCommand("zigbee2mqtt/myDevice", "{\"state\":\"TOGGLE\"}");
    }
}
