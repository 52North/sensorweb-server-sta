## Extended Sensor EncodingType

By default the STA only allows `Datastream->encodingType` to have the values `OM_CategoryObservation`, `OM_CountObservation`, `OM_Measurement`, `OM_Observation`, `OM_TruthObservation` as of Section 8.2.4 of the Specification.
To handle use-cases where the `Observation->result` is a XML-encoded SensorML20 document, e.g. if the Observation is an SensorML20 `swe:event` this Extension adds an additional ObservationType `OM_SensorML20Observation` with valueCode `http://www.52north.org/def/observationType/OGC-OM/2.0/OM_SensorML20Observation`.

Seperating these Observations from other observations allows for special handling to be applied when returning these Observations.

Specifically, as the result is guaranteed to be SensorML 2.0, it can be transcoded to JSON encoding in accordance with [OGC Best Practice 17-011r2: JSON Encoding Rules SWE Common / SensorML](https://docs.opengeospatial.org/bp/17-011r2/17-011r2.html).

Note: This transcoding only applies to returned entities! When `POST`ing or `PATCH`ing entities the result is exspected as an (escaped) XML String.

### Example:
TODO
