## Extended Sensor EncodingType

By default the STA only allows `Sensor->encodingType` to have the values `http://www.opengis.net/doc/IS/SensorML/2.0` or `application/pdf` as of Section 8.2.5 of the Specification.
This extension removes this limitation and allows arbitrary encodingTypes to be used, e.g. `plain/text`.

### Example:
```(type=json)
{
	"name": "DS18B20",
	"description": "DS18B20 is an air temperature sensor",
	"encodingType": "plain/text",
	"metadata": "This is plaintext metadata"
}
```
