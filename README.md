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
### Parameter set description

### Dataset definition

Definition of the dataset has the following form:

```
<dataset name="dataset_1">
  <path>...</path>
  <label>...</label>
  <type>...</type>
  <report_path>...</report_path>
</dataset>
```

The meaning of the tags:
 * `path` - directory with training and testing files in ARFF format. A model is learned on every file containing *train* phrase in its name, and then validated on a file with *train* phrase replaced by *test*. 
 * `label` - name of a label attribute.
 * `type` - experiment type, one of the following: *BinaryClassification*, *Classification*, *Regression*, *Survival*. In the last case, the dataset must contain an attribute named *survival_time*. 
 * `report_path` - directory where experiment reports are to be stored. For each parameter set, the tool generates two files named: *<dataset name>, <parameter_set name>.csv* and *<dataset name>, <parameter_set name>.res*. The former contains table with numerical characteristics for all investigated train-test pairs (row per pair). The latter contains corresponding models in text form (rule sets) and survival function estimators for all rules (applies to survival problems only).
 
An example dataset definition have the following form:
```
<dataset name="seismic-bumps">
  <path>./datasets/seismic-bumps</path>
  <label>class</label>
  <type>BinaryClassification</type>
  <report_path>./reports/seismic-bumps</report_path>
</dataset>
```


## Citing
