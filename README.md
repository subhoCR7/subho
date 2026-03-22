# NEM12 Parser - Production Grade

## Overview
Parses NEM12 files and generates SQL insert statements.

## Features
- Streaming processing (handles large files)
- Batch SQL inserts
- Logging + metrics
- Fault-tolerant parsing

## Run
mvn clean package
java -jar target/nem12-parser-1.0.jar input.csv output.sql

## Design Decisions
- O(1) memory usage via streaming
- Separation of concerns
- Batch inserts for performance

## Assumptions
- Input follows NEM12 format
- Invalid values are skipped

## Future Improvements
- Direct DB ingestion
- Parallel processing
- Support NEM13
