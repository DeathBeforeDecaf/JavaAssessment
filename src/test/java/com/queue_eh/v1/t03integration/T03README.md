# Tier03 Integration Test Cases

Test cases that systematically enumerate all permutations of the methods for a given API (and ideally cover all code
paths for the system under test).  Populating this set of test cases may be an NP-complete problem for non-trivial systems,
so it's beneficial for test to work with development and management to identify a list of integration test cases ranked
by descending risk and populate the highest risk permutations first.  Where risk is defined by defect impact times defect probability.

This is also the appropriate test section to check for pathological cases that immediately cause system errors (typically
in the form of unhandled exceptions for java).


## Integration Scope

### Input Methods x Output Content Types

For the scope of weather.gov API, I'd start with outlining permutations containing:
* properly loaded satellite data
* all latitude/longitude pairs within the US
* all subsequent gridpoints within the US
* check alerts from each US state
    * check that each response returns correct json or XML content

Then continue outlining permutations, isolating marginal data for:
* properly loaded satellite data
    * lat/long pairs in US territories and any subsequent gridpoints from response
    * lat/long pairs in continents outside US and any subsequent gridpoints from response
    * lat/long pairs in ocean/arctic and any subsequent gridpoints from response

Investigate pathological cases:
* improperly loaded satellite data
* missing values from request
* domain of valid latitude/longitude values
* domain of valid states
* unsupported content types
* missing user agent
