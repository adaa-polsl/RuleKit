# Expert-rules
Algorithm for expert-guided induction of decision rules.

## Usage
Expert-rules is distributed as a standalone JAR package (see Release tab for download). To run the analysis, execute
```
java -jar expert-rules experiments.xml
```
where `experiments.xml` is an XML file with a description of experimental setting. It describes parameter sets and datasets to be examined: 
```
</experiment>
  <parameter_sets>
      <parameter_set name="paramset_1">...</parameter_set>
      <parameter_set name="paramset_2">...</parameter_set>
      ...
  </parameter_sets>

  <datasets>
      <dataset name="dataset_1">...</dataset>
      <dataset name="dataset_2">...</dataset>
      ...
  </datasets>
</experiment>
```


## Citing
