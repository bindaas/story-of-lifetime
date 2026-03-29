#!/bin/bash
set -e
echo "=== Story of a Lifetime ==="
mvn clean package && mvn spring-boot:run
