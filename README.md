# cordalo
provider CORDA frameworks to speed up your development in the decentralized space


## verifier
Verification of smart contracts is very important. Most of the template show in details (readable for developers only what is tested)
Use our StateVerifier to simplify test in contracts, flows and testcases

example: test for no input and 1 single output of one specific class a specific command
```
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ServiceContract.Commands.class);
        verifier.input().empty("input must be empty");
        ServiceState service = verifier
              .output().notEmpty().one().one(ServiceState.class)
              .object();
      
`````

