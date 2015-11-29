#!/bin/bash

mvn clean package

java -jar target/benchmarks.jar ConcurrentSkipListSetBenchmark -wi 5 -i 5 -f 1 -t 4
