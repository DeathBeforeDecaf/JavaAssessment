# Tier00 Minimal System Dependencies

This collection of test cases verify that all of the required dependencies are available at the start of a test run.  If test execution fails here it should be easy to troubleshoot and diagnose the problem before the automated test system has any substantial time and effort on test case execution.

## Test System Requirements

* Java
* TestNG
* [api.weather.gov](https://www.weather.gov/documentation/services-web-api)
* tcp connectivity from test system to system under test