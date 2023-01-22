## Hereon Backend

Extension of the STA to use ArcGIS REST API as backend for storing observations.

### Differences to normal STA behaviour

As `Observations` are not stored inside the local database, but requests are passed through to the underlying API, not all STA functionality is available for the `/v1.1/Observations` endpoint.

The following functionality is available:
- Querying via related Datastream (see #Examples)
- Query Parameters:
  - $count
  - $orderby
  - $select
  - $top
  - $skip
  - $filter
    - Comparisons (e.g. `result lt 52`)
    - Spatial functions on `parameters/geometry` (e.g. `st_within(parameters/geometry, geography'POLYGON ((0 0, 0 60, 60 60, 60 0, 0 0))')`)


### Examples
All Observations for Datastream 23:  
`/v1.1/Datastreams(23)/Observations`

Datastream 23 with embedded Observations:  
`/v1.1/Datastreams(23)?$expand=Observations($top=600)`


All Observations for Datastream 23, custom page size:  
`/v1.1/Datastreams(23)/Observations?$top=520`

All Observations for Datastream 23 + total count:  
`/v1.1/Datastreams(23)/Observations?$count=true`

All Observation for Datastream 23, sorted by phenomenonTime:  
`/v1.1/Datastreams(23)/Observations?$orderby=phenomenonTime&$select=id,result,phenomenonTime` 

All Observations for Datastream 23, filtered by phenomenontime
Alle Observations für einen Datastream, gefiltert nach Messzeit:  
`/v1.1/Datastreams(23)/Observations?$filter=phenomenonTime gt 2022-11-16T08:06:00.000Z and phenomenonTime lt 2022-11-17T08:06:00.000Z`

All Observations for Datastream 23, filtered spatially with boundingbox + by time:  
`/v1.1/Datastreams(23)/Observations?$filter=st_within(parameters/geometry, geography'POLYGON ((0 0, 0 60, 60 60, 60 0, 0 0))') and phenomenonTime eq 2022-11-16T08:06:00.000Z`

