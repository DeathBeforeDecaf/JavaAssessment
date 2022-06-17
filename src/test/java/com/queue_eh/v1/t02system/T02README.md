# Tier02 System Tests

Collection of test cases that verify the minimal terms of service are met by the system under test.  If a system must
process a specified number of test cases per amount of time, that requirement should be verified here.  This is also a
great place to verify that all of the API methods are available for the system under test.


## System Requirements

The following methods must be available:

* https://api.weather.gov/gridpoints/{office}/{grid X},{grid Y}/forecast
* https://api.weather.gov/points/{latitude},{longitude}
* https://api.weather.gov/alerts/active?area={state}

> **Note:** This is merely sample data based on speculation and is not intended to be accurate.

* [please see complete list is at weather.gov, click on Specification tab](https://www.weather.gov/documentation/services-web-api)

