import io.github.zeroone3010.yahueapi.HueBridgeConnectionBuilder;
import io.github.zeroone3010.yahueapi.v2.Hue;
import io.github.zeroone3010.yahueapi.v2.Light;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args)  {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        try {
            list();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // API key xMPVYUdfWO7knNcobBxqmfAKNVPwoSCa7ARhUCJ8

    private static void init() throws InterruptedException, ExecutionException {
        final String bridgeIp = "192.168.86.29"; // Fill in the IP address of your Bridge
        final String appName = "MyFirstHueApp"; // Fill in the name of your application
        final CompletableFuture<String> apiKey = new HueBridgeConnectionBuilder(bridgeIp).initializeApiConnection(appName);
// Push the button on your Hue Bridge to resolve the apiKey future:
        final String key = apiKey.get();
        System.out.println("Store this API key for future use: " + key);
        final Hue hue = new Hue(bridgeIp, key);
    }


        public static void list() throws Exception {
            String bridgeIp = "192.168.86.29";
            String apiKey = "xMPVYUdfWO7knNcobBxqmfAKNVPwoSCa7ARhUCJ8";
            Hue hue = new Hue(bridgeIp, apiKey);

            for (Map.Entry<UUID, Light> e: hue.getLights().entrySet()) {
                System.out.println(e.getValue().getName());
                e.getValue().turnOn();
            }
//            hue.getRooms().forEach(room ->
//                    hue.getLights().forEach(id,l->{
//
//                    })
//                    System.out.println(room.getName() + ": " + room.isLightOn())
//            );

            // Turn all lights off
//            hue.getLights().forEach(light -> light.setOn(false));
        }

}