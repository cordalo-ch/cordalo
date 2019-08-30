# cordalo
[![Build Status](https://api.travis-ci.org/cordalo-ch/cordalo.svg?branch=master)](https://travis-ci.org/cordalo-ch/cordalo)
[![Coverage Status](https://coveralls.io/repos/github/cordalo-ch/cordalo/badge.svg)](https://coveralls.io/github/cordalo-ch/cordalo)

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

