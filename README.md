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


# Build environment travis
we are using travis to build and deploy our solution to OSS nexus
here are some tips for other that want to do this.


- install travis-cli local
```
sudo gem install travis
```

- go to your git rep
```
cd git/cordalo/
```

- get your key from gpg 
- export your GPG secret keys that had been used for OSS

```
$ gpg --list-secret-keys
/Users/Lolo/.gnupg/pubring.kbx
------------------------------
sec   rsa4096 2019-08-30 [SC] [expires: 2023-08-30]
      5FDC.......
uid           [ultimate] Lorenz Hanggi <build@cordalo.ch>
```

replace value for LONG_ID with your ID from gpg
add the key and iv values directly in your travis online configuration
and add the code to your .travis.yml
```
LONG_ID=5FDC....
gpg --armor --export-secret-keys $LONG_ID > secret-ring.gpg
travis encrypt-file secret-ring.gpg —add
rm secret-ring.gpg
```
