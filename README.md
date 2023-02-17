```
  ____ ____  _   _     ____ _____  _    ____  _           
 | ___|___ \| \ | |   / ___|_   _|/ \  |  _ \| |_   _ ___ 
 |___ \ __) |  \| |   \___ \ | | / _ \ | |_) | | | | / __|
  ___) / __/| |\  |    ___) || |/ ___ \|  __/| | |_| \__ \
 |____/_____|_| \_|   |____/ |_/_/   \_\_|   |_|\__,_|___/               
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

This is an implementation of the `SensorThingsAPI Plus (OGC 22-022)`.

Further documentation:
* STAPlus Specification: [OGC 22-022 Repository](https://github.com/opengeospatial/sensorthings/tree/22-022)
* [OGC Best Practice for using SensorThings API with Citizen Science](https://docs.ogc.org/bp/21-068.pdf)
* STA v1.1 Specification: [OGC SensorThings API Part I: Sensing](https://github.com/opengeospatial/sensorthings)

## Setup
### Docker
The latest Docker Images are available on [DockerHub](https://hub.docker.com/r/52north/sensorweb-server-sta/tags?page=1&name=STAPLUS) (Suffix `-STAPLUS`).

A complete demo setup is provided via a [docker-compose file](docker-compose.yml)

## Conformance Status:

> Note: This is only an abstract overview. For the specific Specification classes that are covered refer to the `conformanceClasses` Section on the STA Landing Page of the individual deployment (accessible via `/v1.1/`)

- Create/Read/Update/Delete
  - Deep-Insert not supported
- Filtering Extension
- Business-Logic
  - ClosedGroupRule --> Observations cannot be added to a group that is closed (runtimeEnd is after current timestamp)

## Support

[Contact us directly](#contact)

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
 

## Credits

The development the 52°North SensorThings API implementations was supported by several organizations and projects. Among other we would like to thank the following organisations and project

| Project/Logo | Description |
| :-------------: | :------------- |
| <a target="_blank" href="https://cos4cloud-eosc.eu/"><img alt="Cos4Cloud - Co-designed citizen observatories for the EOS-Cloud" align="middle" width="172" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/cos4cloud.png" /></a> | The development of this version of the 52&deg;North SensorThings API was supported by the <a target="_blank" href="https://ec.europa.eu/programmes/horizon2020/">European Union’s Horizon 2020</a> research project <a target="_blank" href="https://cos4cloud-eosc.eu/">Cos4Cloud</a> (co-funded by the European Commission under the grant agreement n&deg;863463) |
| <a target="_blank" href="https://www.bmvi.de/"><img alt="BMVI" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/bmvi-logo-en.png"/></a><a target="_blank" href="https://www.bmvi.de/DE/Themen/Digitales/mFund/Ueberblick/ueberblick.html"><img alt="mFund" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/mFund.jpg"/></a><a target="_blank" href="http://wacodis.fbg-hsbo.de/"><img alt="WaCoDis - Water management Copernicus services for the determination of substance inputs into waters and dams within the framework of environmental monitoring" align="middle" width="126" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/wacodis-logo.png"/></a> | The development of this version of the 52&deg;North SensorThings API was supported by the <a target="_blank" href="https://www.bmvi.de/"> German Federal Ministry of of Transport and Digital Infrastructure</a> research project <a target="_blank" href="http://wacodis.fbg-hsbo.de/">WaCoDis</a> (co-funded by the German Federal Ministry of Transport and Digital Infrastructure, programme mFund) |
| <a target="_blank" href="https://bmbf.de/"><img alt="BMBF" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/bmbf_logo_neu_eng.png"/></a><a target="_blank" href="https://www.fona.de/"><img alt="FONA" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/fona.png"/></a><a target="_blank" href="http://www.mudak-wrm.kit.edu/"><img alt="Multidisciplinary data acquisition as the key for a globally applicable water resource management (MuDak-WRM)" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/mudak_wrm_logo.png"/></a> | The development of this version of the 52&deg;North SensorThings API was supported by the <a target="_blank" href="https://www.bmbf.de/"> German Federal Ministry of Education and Research</a> research project <a target="_blank" href="http://www.mudak-wrm.kit.edu/">MuDak-WRM</a> (co-funded by the German Federal Ministry of Education and Research, programme FONA) |
| <a target="_blank" href="http://www.wupperverband.de"><img alt="Wupperverband" align="middle" width="196" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_wv.jpg"/></a> | The <a target="_blank" href="http://www.wupperverband.de/">Wupperverband</a> for water, humans and the environment (Germany) |
