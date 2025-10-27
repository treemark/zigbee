//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        final String bridgeIp = "192.168.86.29"; // Fill in the IP address of your Bridge
        final String appName = "MyFirstHueApp"; // Fill in the name of your application
        final CompletableFuture<String> apiKey = new HueBridgeConnectionBuilder(bridgeIp).initializeApiConnection(appName);
// Push the button on your Hue Bridge to resolve the apiKey future:
        final String key = apiKey.get();
        System.out.println("Store this API key for future use: " + key);
        final Hue hue = new Hue(bridgeIp, key);
    }
}