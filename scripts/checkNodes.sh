#!/bin/bash
sshpass -p test ssh user1@localhost -p 10103 -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
sshpass -p test ssh user1@localhost -p 10106 -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
sshpass -p test ssh user1@localhost -p 10109 -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
sshpass -p test ssh user1@localhost -p 10112 -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
sshpass -p test ssh user1@localhost -p 10115 -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
