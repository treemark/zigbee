# Philips Hue Bridge Autodiscovery

The application now supports automatic discovery of Philips Hue Bridges on your local network using the official Philips Hue Discovery Service.

## How It Works

The autodiscovery feature queries `https://discovery.meethue.com`, which returns a JSON array of all Hue Bridges registered to your account and accessible on your local network.

Example response:
```json
[
  {
    "id": "001788fffe20057c",
    "internalipaddress": "192.168.1.100"
  }
]
```

## Configuration

### Option 1: Enable Autodiscovery (Recommended)

Set the bridge IP to `auto` in `application.yml`:

```yaml
hue:
  bridge:
    ip: auto
    api-key: your-api-key-here
```

### Option 2: Manual IP Configuration

Specify the bridge IP address directly:

```yaml
hue:
  bridge:
    ip: 192.168.86.30
    api-key: your-api-key-here
```

### Option 3: Leave Empty for Autodiscovery

You can also leave the IP empty, and autodiscovery will trigger automatically:

```yaml
hue:
  bridge:
    ip: 
    api-key: your-api-key-here
```

## API Endpoints

The application provides two REST endpoints for manual discovery:

### Discover All Bridges

```bash
GET http://localhost:8080/api/discovery/bridges
```

Returns a list of all discovered bridges:

```json
[
  {
    "id": "001788fffe20057c",
    "internalIpAddress": "192.168.1.100"
  }
]
```

### Discover First Bridge IP

```bash
GET http://localhost:8080/api/discovery/bridge/ip
```

Returns the IP of the first discovered bridge:

```json
{
  "ip": "192.168.1.100"
}
```

## Testing

You can test the discovery using curl:

```bash
# Discover all bridges
curl http://localhost:8080/api/discovery/bridges

# Get first bridge IP
curl http://localhost:8080/api/discovery/bridge/ip
```

Or visit the Swagger UI at `http://localhost:8080/swagger-ui.html` to test interactively.

## Startup Behavior

When the application starts:

1. If `hue.bridge.ip` is set to `auto`, empty, or null, autodiscovery is triggered
2. The service queries `https://discovery.meethue.com`
3. If bridges are found, the first bridge's IP is used
4. If no bridges are found, an exception is thrown with instructions to configure manually
5. If a specific IP is configured, autodiscovery is skipped and that IP is used directly

## Logging

The application logs discovery attempts and results at the INFO level:

```
2025-12-28 20:00:00 [main] INFO  HueBridgeConfig - Bridge IP not configured or set to 'auto', attempting autodiscovery...
2025-12-28 20:00:01 [main] INFO  BridgeDiscoveryService - Attempting to discover Hue Bridge via https://discovery.meethue.com
2025-12-28 20:00:01 [main] INFO  BridgeDiscoveryService - Discovered Hue Bridge: 001788fffe20057c at IP: 192.168.1.100
2025-12-28 20:00:01 [main] INFO  HueBridgeConfig - Successfully autodiscovered bridge at IP: 192.168.1.100
```

## Troubleshooting

### No bridges found during discovery

- Ensure your Hue Bridge is powered on and connected to the network
- Verify you have internet connectivity (required to access discovery.meethue.com)
- Check that your bridge is registered to your Philips Hue account
- Try accessing `https://discovery.meethue.com` directly in a browser

### Multiple bridges detected

If you have multiple bridges, the application will automatically use the first one. To use a specific bridge, configure its IP address manually in `application.yml`.

### Discovery fails but bridge is accessible

If autodiscovery fails but you can reach your bridge locally, configure the IP address manually:

```yaml
hue:
  bridge:
    ip: 192.168.x.x  # Your bridge's IP
    api-key: your-api-key-here
```
