

## Server Properties Extension

Provides information about any configurable Properties of the Server, e.g. for features that extend the standard or define previously undefined behaviour (e.g. interaction of `$expand` with `$select` in STA 1.0)
### Example:
```(type=json)
"serverSettings": {
    "https://github.com/52North/sensorweb-server-sta/extension/server-properties.md": {
        "escapeId":true,
        "implicitExpand":false,
        "updateFOI":false,
        "variableEncodingType":false
    }
}
```
