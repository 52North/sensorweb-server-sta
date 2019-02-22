# 52°North Sensor Things API (STA)

Implementation of the [OGC SensorThings API Part I: Sensing](https://github.com/opengeospatial/sensorthings).

## Conformance Test Suite Status:

| Conformance Class                     | Reference | Implemented |Test Status |
|:--------------------------------------|:---------:|:-----------:|-----------:|
| Sensing Core                          | A.1       | Yes         |   6 / 6    |
| Create-Update-Delete                  | A.2       | Yes         |   8 / 8    |
| Filtering Extension                   | A.3       | Yes         |   8 / 8    |
| Observation Creation via MQTT         | A.7       | Yes         |   0 / 1    |
| Receiving Updates via MQTT            | A.8       | Yes         |   0 / 5    |


## Notes:

#### MQTT Extension
 - Subscription is only possible on Topics (aka REST-Endpoints) that exist. This is checked on Subscription creation. SUBACK Message is send regardless.
