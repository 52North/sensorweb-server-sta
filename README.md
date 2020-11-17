```
  _____ ___  _   _    _____                            _______ _     _                            _____ _____ 
 | ____|__ \| \ | |  / ____|                         |__   __| |   (_)                     /\   |  __ \_   _|
 | |__    ) |  \| | | (___   ___ _ __  ___  ___  _ __   | |  | |__  _ _ __   __ _ ___     /  \  | |__) || |  
 |___ \  / /| . ` |  \___ \ / _ \ '_ \/ __|/ _ \| '__|  | |  | '_ \| | '_ \ / _` / __|   / /\ \ |  ___/ | |  
  ___) |/ /_| |\  |  ____) |  __/ | | \__ \ (_) | |     | |  | | | | | | | | (_| \__ \  / ____ \| |    _| |_ 
 |____/|____|_| \_| |_____/ \___|_| |_|___/\___/|_|     |_|  |_| |_|_|_| |_|\__, |___/ /_/    \_\_|   |_____|
                                                                            __/ |                           
                                                                           |___/                            
```

## Table of Contents

* [About the Project](#about-the-project)
* [Roadmap](#roadmap)
* [Setup](#setup)
* [Conformance Status](#conformance-status)
* [Support](#support)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)

## About The Project

This is an implementation of the `OGC SensorThings API Part I: Sensing`.

Primary features:
* Interoperability with the [52°North SOS](https://github.com/52North/SOS/) and [52°North Helgoland API](https://github.com/52North/sensorweb-server-helgoland) based on a shared data model (Contact us for further information about this feature)
* Several convenience extensions
  * User-defined @iot.id
  * Extended MQTT Capabilities
  * Automatic synchronization of Datastream->phenomenonTime with linked Observation->phenomenonTime
  * etc.

Further documentation:
* Standard: [OGC SensorThings API Part I: Sensing](https://github.com/opengeospatial/sensorthings)
* Additional Features: [Github Wiki](https://github.com/52North/sensorweb-server-sta/wiki)
* [Example Requests](https://github.com/52North/sensorweb-server-sta/wiki/Example-Requests)

## Roadmap
All development is tracked via GitHub Projects [here](https://github.com/52North/sensorweb-server-sta/projects/4).

Next Key Milestones:
 - Integration of HiveMQ MQTT Broker (Q4 2020)
 - Implementation of STA Extensions Multidatastream,DataArray,Batch-Request (Q1 2021)

## Setup
### Docker
The latest Docker Images are available on [DockerHub](https://hub.docker.com/r/52north/sensorweb-server-sta)
A complete demo setup is provided via a [docker-compose file](https://github.com/52North/sensorweb-server-sta/docker-compose.yml)

### JAR/WAR Packaging
The latest jar/war packages are available via the GitHub [Releases](https://github.com/52North/sensorweb-server-sta/releases)

## Conformance Status:

| Conformance Class                     | Reference | Implemented |Test Status |
|:--------------------------------------|:---------:|:-----------:|-----------:|
| Sensing Core                          | A.1       | Yes         |   5 / 6    |
| Filtering Extension                   | A.2       | Yes         |   0 / 8    |
| Create-Update-Delete                  | A.3       | Yes         |   8 / 8    |
| Batch Requests                        | A.4       | No          |   0 / ?    |
| MultiDatastream Extension             | A.5       | No          |   0 / ?    |
| DataArray Extension                   | A.6       | No          |   0 / ?    |
| Observation Creation via MQTT         | A.7       | Yes         |   1 / 1    |
| Receiving Updates via MQTT            | A.8       | Yes         |   5 / 5    |


## Support

You can get support via the community mailing list:

https://list.52north.org/mailman/listinfo/sensorweb/

or [Contact us directly](#contact)

## Contributing

Are you are interested in contributing to the 52°North STA and you want to pull your changes to the 52N repository to make it available to all?

In that case we need your official permission. For this purpose we have a so-called contributors license agreement (CLA) in place. With this agreement you grant us the rights to use and publish your code under an open source license.

A link to the contributors license agreement and further explanations are available here:

https://52north.org/software/licensing/guidelines/

## License

See `LICENSE` and `NOTICE` for more information.

## Contact
 - Simon Jirka <s.jirka@52North.org>
 - Jan Speckamp <j.speckamp@52North.org>
