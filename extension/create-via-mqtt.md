
## create via MQTT

This Extension extends the functionality of `http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation` to all Entities.
The functionality equals the functionality available via HTTP, all restrictions from `http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel` and `http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete` apply to the published Entities.

### Structure
The Object extends the JSON structure of `http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation` with a new key `entities`, listing all the Entities that can be posted via MQTT.

### Example
```(type=json)
"serverSettings": {
    "https://github.com/52North/sensorweb-server-sta/extension/create-via-mqtt.md": {
        "entities": [
            "Observations",
            "Locations"
        ]
        "endpoints": [
            "mqtt://localhost:1883",
            "ws://localhost:8883"
        ]
    }
}
```
