[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=bugs)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tjlatehjr_thingifier&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=tjlatehjr_thingifier)

# Thingifier

An experiment in Model Based API Development.

## Download

Thingifier comes preconfigured for download as:

- [a simple todo list](https://github.com/eviltester/thingifier/releases/download/v1.5.2/runTodoListRestAPI-1.5.2.jar)
- [a more complex todo list manager](https://github.com/eviltester/thingifier/releases/download/v1.5.2/runTodoManagerRestAPI-1.5.2.jar)

When you download one of the `.jar` files then use:

- `java -jar runTodoListRestAPI-1.5.2.jar`

Where you replace the name of the `.jar` file with the name of the file you downloaded.

- then visit http://localhost:4567 and you will see the welcome gui.
- there are links to the API documentation, and the View GUI

Command line options are:

- `-port=1234` to change the port to `1234`
- `-version=1` to start with a different version.
    - Each api comes preconfigured with multiple versions, by default the 'best' version is used, so if you switch to an earlier version you might find more bugs.

## Cloud Deploy

An online version of the application can be found at:

- https://apithingifier.herokuapp.com

This version will reset all data every 10 minutes.

If you want to practice seriously then I suggest downloading the `.jar` and running it locally.

We are not responsible for any data that you find on the cloud deploy.

We reserve the right to remove the cloud deploy or change the reset time if we discover it is being misused.

## Usage

Currently Thingifier is suitable for using as a Practice Test App for API Testing.
    
## Details

Based on my [Compendium-TA](https://www.compendiumdev.co.uk/page.php?title=compendiumta) tool from 2003/2004.

If you actually want an ER Based tool then check out:

- [Jeddict](https://jeddict.github.io/)
- [Evolutility](http://www.evolutility.org/index.aspx)

If you want to generate test data for an api, investigate:

- [next.json-generator.com](https://next.json-generator.com/)
- [Java-Faker](https://github.com/DiUS/java-faker)

---

- Alan Richardson
- https://www.eviltester.com
- https://www.compendiumdev.co.uk
- https://uk.linkedin.com/in/eviltester
- https://twitter.com/eviltester

---

