# Digital Object Identifier Server

A Digital Object Identifier (DOI) is an alphanumeric string assigned to uniquely identify an object. 
It is tied to a metadata description of the object as well as to a digital location, such as a URL, 
where all the details about the object are accessible.

### Synopsis

This document provides the motivation of the project and the different instructions to both install
and use the DOI-Server. 

### Motivation

To create a DOI, a user needs to be connected to DATACITE so that he sends the metadata and the URL 
of the landing page. Within an organization, the same password to DATACITE cannot be shared for all 
users of the organization.That's why DOI-Server has been created. It allows an user (human or 
programmatic client) to connect to a GUI (or a web service) in order to to a DOI. Each project has 
its own password to connect to DOI-Server and DOI-Server does the rest.
With the DOI-Server, some clients are also provided : a python client, a Java client and a IHM. Each 
client uses the DOI-Server web services to handle the DOI.
DOI-Server is also generic because he can host several plugins to handle the databases (projects, 
role and DOI number generation). 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for 
development and testing purposes. See deployment for notes on how to deploy the project on a 
live system.

### Prerequisities

What things you need to install the software and how to install them

```
Openjdk version 1.8
Apache Maven 3.5.2
Git version 2.17.1
```

### Installing

Clone the repository

```
git clone https://github.com/CNES/DOI-server.git && cd DOI-server
```

Compile and run the tests

```
mvn install
```

End with an example of getting some data out of the system or using it for a little demo

## Example Use

Show what the library does as concisely as possible, developers should be able to figure out **how** your project solves their problem by looking at the code example. Make sure the API you are showing off is obvious, and that your code is short and concise.

## API Reference

Depending on the size of the project, if it is small and simple enough the reference docs can be added to the README. For medium size to larger projects it is important to at least provide a link to where the API reference docs live.

## Running the tests

Run the unit tests 

```
mvn test
```

Run the integration tests

```
mvn verify -P integration-test
```

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With
* Maven

## Contributing

Please read [CONTRIBUTING.md](http://github.com/J-Christophe/DOI/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/J-Christophe/DOI/tags). 

## Authors

* **Jean-Christophe Malapert** - *Initial work* - [Jean-Christophe Malapert](https://github.com/J-Christophe)

* **Claire Caillet** - *Initial work* - [Claire Caillet](https://github.com/ClaireCaillet)

See also the list of [contributors](https://github.com/CNES/DOI/graphs/contributors) who participated in this project.

## License

This project is licensed under the **LGPLV3** - see the [LICENSE.md](https://github.com/CNES/DOI-server/blob/master/COPYING.LESSER) file for details.

