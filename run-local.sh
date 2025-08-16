#!/bin/bash

# Load environment variables from .env.local
set -a
source .env.local
set +a

# Run the application with local profile
./gradlew bootRun --args='--spring.profiles.active=local'