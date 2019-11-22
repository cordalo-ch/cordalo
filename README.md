# cordalo
[![Build Status](https://api.travis-ci.org/cordalo-ch/cordalo.svg?branch=master)](https://travis-ci.org/cordalo-ch/cordalo)
[![Coverage Status](https://coveralls.io/repos/github/cordalo-ch/cordalo/badge.svg)](https://coveralls.io/github/cordalo-ch/cordalo)
![GitHub](https://img.shields.io/github/license/cordalo-ch/cordalo?label=Licence)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cordalo-ch_cordalo&metric=alert_status)](https://sonarcloud.io/dashboard?id=cordalo-ch_cordalo)

provider CORDA frameworks to speed up your development in the decentralized space


## verifier
Verification of smart contracts is very important. Most of the template show in details (readable for developers only what is tested)
Use our StateVerifier to simplify test in contracts, flows and testcases

example: test for no input and 1 single output of one specific class a specific command
```
StateVerifier verifier = StateVerifier
      .fromTransaction(tx, ServiceContract.Commands.class);
verifier
  .input()
  .empty("input must be empty");
ServiceState service = verifier
  .output()
  .notEmpty()
  .one()
  .one(ServiceState.class)
  .object();
      
`````

# Developer environment setup

Install in that order:
* Java JDK 1.8
* Gradle https://gradle.org/install/
* Ruby https://www.ruby-lang.org/en/
* rubyGen https://rubygems.org/pages/download should be enough with  
    ```gem update --system```
* https://rubygems.org/gems/travis should be enough with  
    ```gem install travis```

# licenses report

run `gradle generateLicenseReport`
report is available at `build/reports/dependency-license/index.html`

# Annexes
## How to create the release instruction in .travis.yml
To create the release part in your .travis.yml file, run the following command in the root of your project:
  ```travis setup releases```
You will be prompted for your GitHub username and password. This will create the deploy part in your .travis.yml file