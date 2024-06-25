# CompareScans CLI

This project provides a command-line interface (CLI) for comparing two build scans and applying custom rules to analyze the metrics. The CLI supports fetching metrics from the Develocity API or a file and applying default or custom rules to these metrics.

By comparing two build scans, you can infer information such as differences in the number of modules, tasks, or even explore detailed percentiles of durations for specific task types. Additionally, the CLI can provide analysis of cache artifact sizes between builds. It is important to note that this type of analysis does not necessarily imply that the builds are close in terms of execution time. Comparing builds from different time ranges can offer valuable insights into how the project has grown over time.
## Table of Contents


- [Usage](#usage)
- [Metrics](#metrics)
- [Rules](#rules)
- [Dependency](#dependency)
- [Notes](#notes)
- [Libraries](#libraries)

## Usage

### Download the CLI
```sh

 curl -L https://github.com/cdsap/CompareScans/releases/download/v0.1.0/comparescans --output comparescans
 chmod 0757 comparescans

# Generate Metrics for two build scans using the DV API
./comparescans --from api --first-build-scan $BUILD_SCAN_ID_1 --second-build-scan $BUILD_SCAN_ID_2 --api-key $DV_KEY --url $DV_URL

# Generate Metrics and apply default rules two build scans using the DV API
./comparescans --from api --first-build-scan $BUILD_SCAN_ID_1 --second-build-scan $BUILD_SCAN_ID_2 --api-key $DV_KEY --url $DV_URL --with-default-rules

# Generate Metrics and apply default rules from an existing file
./comparescans --from file --with-default-rules

# Generate Metrics and apply custom rules from an existing file
./comparescans --from file --custom-rules rules.yaml

```
### From sources
```sh

 ./gradlew :cli:fatBinary
cd cli
# Generate Metrics for two build scans using the DV API
./comparescans --from api --first-build-scan $BUILD_SCAN_ID_1 --second-build-scan $BUILD_SCAN_ID_2 --api-key $DV_KEY --url $DV_URL

# Generate Metrics and apply default rules two build scans using the DV API
./comparescans --from api --first-build-scan $BUILD_SCAN_ID_1 --second-build-scan $BUILD_SCAN_ID_2 --api-key $DV_KEY --url $DV_URL --with-default-rules

# Generate Metrics and apply default rules from an existing file
./comparescans --from file --with-default-rules

# Generate Metrics and apply custom rules from an existing file
./comparescans --from file --custom-rules rules.yaml

```


## Options

The CLI supports the following options:

| Option                 | Description                                                                                           |
|------------------------|-------------------------------------------------------------------------------------------------------|
| `--from`               | Source of the metrics. Choices: `api`, `file`. Required.                                              |
| `--api-key`            | API key for accessing the build scan API. Required if `--from` is `api`.                              |
| `--url`                | URL of the build scan API. Required if `--from` is `api`.                                             |
| `--first-build-scan`   | Identifier for the first build scan. Required if `--from` is `api`.                                   |
| `--second-build-scan`  | Identifier for the second build scan. Required if `--from` is `api`.                                  |
| `--with-default-rules` | Flag to use default rules. Optional.                                                                  |
| `--custom-rules`       | File containing custom rules in YAML format. Optional.                                                |
| `--metrics`            | File containing existing metrics in CSV format. Required if `--from` is `file`.                       |

### API Mode

When `--from` is set to `api`, the CLI fetches metrics from a build scan API. The following options are required:

| Option                 | Description                                                    |
|------------------------|----------------------------------------------------------------|
| `--api-key`            | API key for accessing the build scan API.                      |
| `--url`                | URL of the build scan API.                                     |
| `--first-build-scan`   | Identifier for the first build scan.                           |
| `--second-build-scan`  | Identifier for the second build scan.                          |

Example command:

```sh
./comparescans --from api --apiKey yourApiKey --url yourUrl --firstBuildScan yourFirstBuildScan --secondBuildScan yourSecondBuildScan
```

When using the CLI with the `--from api` option a file with the name `compare-$BUILD_SCAN_ID_1-$BUILD_SCAN_ID_2.csv` will be generated in the current directory. This file contains the metrics for the two build scans. For instance:
```csv
entity,name,outcome,type,4n4obiokjzzs4,4n4obiokjzzs4
Module,:build-logic-commons:gradle-plugin,all tasks,CacheSize,173642,173642
Module,:build-logic-commons:gradle-plugin,all tasks,Duration,24206,24206
Module,:build-logic-commons:gradle-plugin,all tasks,Counter,12,12
Module,:build-logic-commons:gradle-plugin,all tasks,Fingerprinting,5762,5762
...
```
Complete example of a metrics output comparing two builds of the Kotlin project available [here](resources/compare-pxt3zsp52gdoy-7ya7rlpa3qqco.csv).

Check the [Metrics](#metrics) section for more information about the metrics generated.

If [Rules](#rules) are applied, the CLI will output the metrics that match the rules. The output will contain the rules that matched and the summary of the rules. For instance:
```csv
rule entity,rule type, name, category, diff,pxt3zsp52gdoy,7ya7rlpa3qqco,pxt3zsp52gdoy raw,7ya7rlpa3qqco raw, description
TaskType,CacheSize,all outcomes,JavaCompile,7.18%,14.32 KB,15.38 KB,14660,15752,Threshold: 10240 Value > 5%
Module,CacheSize,All tasks,:kotlin-compiler-embeddable,35.2%,55.74 MB,79.56 MB,58449627,83422358,Threshold: 102400 Value > 20%
Module,CacheSize,All tasks,:compiler:ir.tree:tree-generator,23.22%,279.56 KB,353.00 KB,286265,361467,Threshold: 102400 Value > 20%
...
```

### File Mode

When `--from` is set to `file`, the CLI reads metrics from a CSV file. The following option is required:

| Option      | Description                                          |
|-------------|------------------------------------------------------|
| `--metrics` | File containing existing metrics in CSV format.      |

```sh
./comparescans --from file --metrics yourMetricsFile.csv
```


Using the mode `--from file` we apply rules over an existing metrics comparison file from a previous execution. This mode also generates
a file with the name `compare-metrics.csv` in the current directory.

<b>Note</b>: This mode is required to be used together with the rules functionality that is explained in the Rules section.


## Metrics
A `Metric` represents a measurement or statistic related to different aspects of a project, module, task, or task type within a build scan. Each metric is categorized by its entity type, metric type, and includes values from two builds for comparison.

| Attribute    | Description                                                                                                                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `entity`     | The entity that this metric is associated with. It can be one of: `Project`, `Module`, `Task`, `TaskType`.                                                                                                           |
| `type`       | The type of measurement. It can be one of: `Duration`, `DurationMedian`, `DurationMean`, `DurationP90`, `Counter`, `Fingerprinting`, `FingerprintingMedian`, `FingerprintingMean`, `FingerprintingP90`, `CacheSize`. |
| `subcategory`| A string representing a subcategory or additional classification for the metric.                                                                                                                                     |
| `name`       | The name of the metric.                                                                                                                                                                                              |
| `firstBuild` | The value of the metric for the first build.                                                                                                                                                                         |
| `secondBuild`| The value of the metric for the second build.                                                                                                                                                                        |


## Rules
Once metrics are provided by the modes (API or File), you can apply rules configured in a YAML file to filter and analyze
these metrics. The rules functionality allows you to specify conditions that metrics must meet to be considered significant.
This is useful for identifying performance regressions or improvements between build scans.
For instance:
```yaml
rules:
  - entity: Module
    type: Duration
    threshold: 10000
    value: 40
```
In this example, we are defining a rule that will match with metrics containing:
* Entity: Look for metrics associated with the Module entity.
* Type: Filter metrics of the type Duration.
* Threshold: Only consider metrics where the duration exceeds 10,000 milliseconds (10 seconds).
* Value: Check if the difference between the two scans is equal to or greater than 40%.

The CLI contains the following default rules:

| Entity     | Type           | Threshold | Value |
|------------|----------------|-----------|-------|
| Task       | Duration       | 5000      | 10    |
| TaskType   | Duration       | 5000      | 10    |
| Module     | Duration       | 10000     | 20    |
| Project    | Counter        | N/A       | N/A   |
| TaskType   | Counter        | N/A       | N/A   |

<b>Note</b>: `threshold` and `value` are not considered for type `Counter`.

Additional to the csv file, If rules are matching the metrics, the CLI will output:
```kotlin
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                         Rules matching                                                         │
├─────────────┬───────────┬────────────────────────┬────────────────────────┬────────┬─────────────────────┬─────────────────────┤
│ Rule Entity │ Rule Type │ Name                   │ Category               │ Diff   │ Build pxt3zsp52gdoy │ Build 7ya7rlpa3qqco │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :kotlin-build-common   │ All tasks              │ 78.02% │             46.855s │             20.558s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :kotlin-compiler       │ All tasks              │ 43.87% │             15.796s │             24.673s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :js:js.tests           │ All tasks              │ 56.78% │             12.781s │             22.916s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :compiler:fir:checkers │ All tasks              │ 43.05% │             26.243s │             40.638s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :kotlin-build-common   │ executed_cacheable     │ 78.35% │             46.378s │              20.27s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :kotlin-compiler       │ executed_not_cacheable │ 43.85% │             15.788s │             24.654s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :js:js.tests           │ executed_cacheable     │ 56.62% │             12.199s │             21.833s │
├─────────────┼───────────┼────────────────────────┼────────────────────────┼────────┼─────────────────────┼─────────────────────┤
│ Module      │ Duration  │ :compiler:fir:checkers │ executed_cacheable     │  48.4% │             22.648s │              37.11s │
└─────────────┴───────────┴────────────────────────┴────────────────────────┴────────┴─────────────────────┴─────────────────────┘
┌─────────────────────────────────────────────────────┐
│                      Summary.                       │
│               Total rules matched: 8                │
├────────┬──────────┬───────────┬───────┬─────────────┤
│ Entity │ Type     │ Threshold │ Value │ Occurrences │
├────────┼──────────┼───────────┼───────┼─────────────┤
│ Module │ Duration │       10s │   40% │           8 │
└────────┴──────────┴───────────┴───────┴─────────────┘
```
## Dependency
If you don't want to use the CLI, the project can be used as a dependency. The project is available in the Maven Central Repository. You can add the dependency to your project using the following coordinates:

```kotlin
implementation("io.github.cdsap:comparescans:0.1.0")
```

## Notes
* CompareScans CLI only supports Gradle builds.
* The source of metrics can be either the Develocity API or a file. The API mode requires an API key and the URL of the Develocity API, along with the build scan IDs. If you need to generate the `Build` models from a Develocity instance you don't have the access token, you can use the [TimelineParser](https://github.com/cdsap/TimelineParser).

## Libraries
* [clikt](https://github.com/ajalt/clikt)
* [picnic](https://github.com/JakeWharton/picnic)
* [geApiData](https://github.com/cdsap/GEApiData)
* [fatBinary](https://github.com/cdsap/FatBinary)
* [kotlin-statistics](https://github.com/thomasnield/kotlin-statistics)
