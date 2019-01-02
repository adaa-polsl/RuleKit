# RuleKit

RuleKit is a comprehensive library for inducing rule-based data models. It has the ability to produce classification, regression, and survival rules. The suite provides user with the possibility to introduce some apriori knowledge.
The analysis can be performed in batch mode, through RapidMiner plugin, or R package. A Java API is also provided for convinience. 

# Table of contents

1. [Batch interface](#1-batch-interface)
    1. [General information](#11-general-information)
    2. [Parameter set definition](#12-parameter-set-definition)
    3. [Dataset definition](#13-dataset-definition)
    4. [Examples](#14-example)
2. [RapidMiner plugin](#2-rapidminer-plugin)
3. [R package](#3-r-package)
4. [Output files](#4-output-files)
5. [User-guided induction](#5-user-guided-induction)
6. [Library API](#6-library-api)

<!-- toc -->

# 1. Batch interface

## 1.1. General information

To run the analysis in the batch mode, use RuleKit JAR package (see Release tab for download):
```
java -jar RuleKit experiments.xml
```
where experiments.xml is an XML file with experimental setting description. The batch mode allows investigating multiple datasets with many induction parameters. The general XML structure is as follows:

```
</experiment>
	<parameter_sets>
		<parameter_set name="paramset_1">...</parameter_set>
		<parameter_set name="paramset_2">...</parameter_set>
		...
	</parameter_sets>

	<datasets>
		<dataset>...</dataset>
        	<dataset>...</dataset>
   		...
  	</datasets>
</experiment>
```

## 1.2. Parameter set definition

This section allows user to specify induction parameters. The package allows testing multiple parameter sets in a single run. Every parameter has its default value, thus, only selected parameters may be specified by the user. In the automatic induction, the following parameters apply:

```
<parameter_set name="paramset_1">
  	<param name="min_rule_covered">...</param>
  	<param name="induction_measure">...</param>
  	<param name="pruning_measure">...</param>
	<param name="voting_measure">...</param>
</parameter_set>
```    
where:
* `min_rule_covered` - minimum number of previously uncovered examples to be covered by a new rule (positive examples for the classification problems),
* `induction_measure` - rule quality measure used during growing,
* `pruning_measure` - rule quality measure used during pruning,
* `voting_measure` - rule quality measure used for voting.

Measure parameters apply only for classification and regression tasks and may have one of the following values: 
*Accuracy*, *BinaryEntropy*, *C1*  *C2*, *CFoil*, *CNSignificnce*, *Correlation*, *Coverage*, *FBayesianConfirmation*, *FMeasure*, *FullCoverage*, *GeoRSS*, *GMeasure*, *InformationGain*, *JMeasure*, *Kappa*, *Klosgen*, *Laplace*, *Lift*, *LogicalSufficiency*, *MEstimate*, *MutualSupport*, *Novelty*, *OddsRatio*, *OneWaySupport*, *PawlakDependencyFactor*, *Q*, *Precision*, *RelativeRisk*, *Ripper*, *RuleInterest*, *RSS*, *SBayesian*, *Sensitivity*, *Specificity*, *TwoWaySupport*, *WeightedLaplace*, *WeightedRelativeAccuracy*, *YAILS* 

In the survival analysis, log-rank statistics is used for induction, pruning, and voting.


## 1.3. Dataset definition

Definition of a dataset has the following form. 

```
<dataset>
     <label>...</label>				# label attribute
     <out_directory>...</out_directory>		# directory where all output files will be placed
     <weight>...</weight>                       # optional weight attribute
     <survival_time>...</survival_time>         # only for survival datasets
    
    <training> 
          <report_file>...</report_file>        # TXT report (rule sets, KM-estimators, etc.) 
	  <train>
             <in_file>...</in_file>            # input data file (ARFF, CSV)
             <model_file>...</model_file>      # output binary model 
         </train>
         ...
    </training>
    
    <prediction>
	 <report_file>...</report_file>   	# CSV report with performance metrics (only when true labels are specified) 
         <predict>
             <model_file>...</model_file>      	# input binary model 
             <test_file>...</test_file>         # input data file (ARFF, CSV)
             <predictions_file>...</predictions_file>  # output data file with predictions  
         </predict>
         ...
    </prediction>
    
</dataset>
```
There are three main parts of the dataset definition: the general properties, the `traning` section, and the `prediction` section. General parameters and at least one of the two latter sections should be specified. 

The general dataset properties are:
* `label` - label attribute,
* `out_directory` - output directory for storing results, subdirectories for all parameter sets are created automatically inside it,
* `weight` - optional weight attribute,
* `survival_time` - name of the survival time attribute, its presence indicates survival analysis problems.

The `training` section allows generating models on specified training sets. It consists of the `report_file` field and any number of `train` subsections. Each `train` subsection is defined by:
* `in_file` - full path to the training file (in ARFF, CSV, XLS format),
* `model_file` - name of the output binary model file (without full path); for each parameter set, a separate model is generated under location *<out_directory>/<parameter_set name>/<model_file>*.
The `report_file` is created for each parameter set under *<out_directory>/<parameter_set name>/<report_file>* location. It contains a common text report for all training files: rule sets, model characteristics, detailed coverage information, training set prediction quality, KM-estimators (for survival problems), etc.   

The `prediction` section allows making predictions on specified testing sets using models generated by the `training` section. It consists of the `report_file` field and any number of `predict` subsections. Each `predict` subsection is defined by:
* `model_file` - name of the input binary model file generated in the `training` part; for each parameter set, a model is searched under location *<out_directory>/<parameter_set name>/<model_file>*, 
* `test_file` - full path to the testing file (in ARFF, CSV, XLS format),
* `predictions_file` - output data file with predictions (without full path); for each parameter set, a prediction is generated under location *<out_directory>/<parameter_set name>/<predictions_file>*.
The `report_file` is created for each parameter set under *<out_directory>/<parameter_set name>/<report_file>* location. It contains a common CSV report for all testing files with values of accuracy measures.
 

## 1.4. Example

Here we present how prepare the XML experiment file for an example classification problem. Let the user be interested in two parameter sets:
* *mincov = 5* with *C2* measure used for growing, pruning, and voting,
* *mincov = 11* with *RSS* measure used for growing and pruning, and *BinaryEntropy* for voting.

The corresponding parameter set definition is as follows:
```
<parameter_sets>
	<parameter_set name="mincov=5, C2">
		<param name="min_rule_covered">5</param>
		<param name="induction_measure">C2</param>
		<param name="pruning_measure">C2</param>
		<param name="voting_measure">C2</param>
	</parameter_set>
	
	<parameter_set name="mincov=11, RSS_RSS_BinaryEntropy">
		<param name="min_rule_covered">5</param>
		<param name="induction_measure">RSS</param>
		<param name="pruning_measure">RSS</param>
		<param name="voting_measure">BinaryEntropy</param>
	</parameter_set>
	
</parameter_sets>
```    

The experiment will be performed on a single dataset in 10-fold cross validation scheme with existing splits:
* name of the label attribute: `class`,
* no weighting,
* output directory: *./result* 
* training files:
    * *./data/seismic-train-1.arff*
    * *./data/seismic-train-2.arff*
    * ...
    * *./data/seismic-train-10.arff*
* training log file: *training-log.txt*
* testing files:
    * *./data/seismic-test-1.arff*
    * *./data/seismic-test-3.arff*
    * ...
    * *./data/seismic-test-10.arff*
* testing report file: *performance.csv*

The corresponding dataset definition is as follows:

```
<dataset>
     <label>class</label>
     <out_directory>./results</out_directory>		
    
    <training>  
         <report_file>training-log.txt</report_file>           		
         <train>
             <in_file>./data/seismic-train-1.arff</in_file>               	
             <model_file>seismic-1.mdl</model_file> 
         </train>
		 
		  <train>
             <in_file>./data/seismic-train-2.arff</in_file>               	
             <model_file>seismic-2.mdl</model_file> 
         </train>
         ...
		<train>
             <in_file>/data/seismic-train-10.arff</in_file>               		
             <model_file>seismic-10.mdl</model_file>  
         </train>
	 

    </training>
    
    <prediction>
     	<report_file>performance.csv</report_file>  
         <predict>
             <model_file>seismic-1.mdl</model_file>      	
             <test_file>./data/seismic-test-1.arff</test_file>            			
             <predictions_file>seismic-pred-1.arff</predictions_file>  	  
         </predict>
		 
		  <predict>
             <model_file>seismic-2.mdl</model_file>      	
             <test_file>./data/seismic-test-2.arff</test_file>            			
             <predictions_file>seismic-pred-2.arff</predictions_file>  	  
         </predict>
		 ...
	 
		<predict>
             <model_file>seismic-10.mdl</model_file>      	
             <test_file>./data/seismic-test-10.arff</test_file>            			
             <predictions_file>seismic-pred-10.arff</predictions_file>   	
         </predict>

	 
    </prediction>
    
</dataset>
```

In the training phase, for every investigated parameter set RuleKit generates a subdirectory in the output directory. 
Each of these subdirectories contains models for all training files and a common text report. 
Therefore, the following files are produced as a result of training:
* *./results/mincov=5, C2/seismic-1.mdl*
* *./results/mincov=5, C2/seismic-2.mdl*
* *...*
* *./results/mincov=5, C2/seismic-10.mdl*
* *./results/mincov=5, C2/training-log.txt*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-1.mdl*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-2.mdl*
* *...*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-10.mdl*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/training-log.txt*

In the prediction phase, previously-generated models are applied on specified    

# 2. RapidMiner plugin

# 3. R package

# 4. Output files

# 5. User-guided induction


Expert knowledge is also specified through parameters:
```
<parameter_set name="paramset_1">
  	<param name="min_rule_covered">...</param>
  	<param name="induction_measure">...</param>
  	<param name="pruning_measure">...</param>
	<param name="voting_measure">...</param>
  	<param name="use_expert">true</param>
  	<param name="extend_using_preferred">...</param>
  	<param name="extend_using_automatic">...</param>
  	<param name="induce_using_preferred">...</param>
  	<param name="induce_using_automatic">...</param>
  	<param name="preferred_conditions_per_rule">...</param>
  	<param name="preferred_attributes_per_rule>...</param>
   	<param name="consider_other_classes">...</param>
  	<param name ="expert_rules">
		<entry name="rule-0">...</entry>
		<entry name="rule-1">...</entry>
		...
  	</param>
  	<param name ="expert_preferred_conditions">
		<entry name="preferred-condition-0">...</entry>
		<entry name="preferred-condition-1">...</entry>
		...
  	</param>
  	<param name ="expert_forbidden_conditions">
		<entry name="forbidden-condition-0">...</entry>
		<entry name="forbidden-condition-1">...</entry>
		...
  	</param>
</parameter_set>
``` 

Parameter meaning (symbols from the paper are given in parentheses):
* `use_expert` - boolean indicating whether user's knowledge should be used,
* `expert_rules`(R<sub>&oplus;</sub>) - set of initial rules,
* `expert_preferred_conditions`(C<sub>&oplus;</sub>, A<sub>&oplus;</sub>) - multiset of preferred conditions (used also for specifying preferred attributes by using special value `Any`),
* `expert_forbidden_conditions`(C<sub>&ominus;</sub>, A<sub>&ominus;</sub>) - set of forbidden conditions (used also for specifying forbidden attributes by using special valye `Any`),
* `extend_using_preferred`(&Sigma;<sub>pref</sub>)/`extend_using_automatic`(&Sigma;<sub>auto</sub>) - boolean indicating whether initial rules should be extended with a use of preferred/automatic conditions and attributes,
* `induce_using_preferred`(&Upsilon;<sub>pref</sub>)/`induce_using_automatic`(&Upsilon;<sub>auto</sub>) - boolean indicating whether new rules should be induced with a use of preferred/automatic conditions and attributes,
* `preferred_conditions_per_rule`(K<sub>C</sub>)/`preferred_attributes_per_rule`(K<sub>A</sub>) - maximum number of preferred conditions/attributes per rule,
* `consider_other_classes` - boolean indicating whether automatic induction should be performed for classes for which no user's knowledge has been defined (classification only).

Let us consider the following user's knowledge (superscripts next to C<sub>&oplus;</sub>, A<sub>&oplus;</sub>, C<sub>&ominus;</sub>, and A<sub>&ominus;</sub> symbols indicate class label):
* R<sub>&oplus;</sub> = { (**IF** gimpuls < 750 **THEN** class = 0), (**IF** gimpuls >= 750 **THEN** class = 1)},
* C<sub>&oplus;</sub><sup>0</sup> = { (seismic = a) }, 
* C<sub>&oplus;</sub><sup>1</sup> = { (seismic = b &wedge; seismoacoustic = c)<sup>5</sup> }, 
* A<sub>&oplus;</sub><sup>1</sup> = { gimpuls<sup>inf</sup> },
* C<sub>&ominus;</sub><sup>0</sup> = { seismoacoustic = b },
* A<sub>&ominus;</sub><sup>1</sup> = { ghazard }.
The XML definition of this knowledge is presented below.
```
<param name ="expert_rules">
	<entry name="rule-1">IF [[gimpuls = (-inf, 750)]] THEN class = {0}</entry>
	<entry name="rule-2">IF [[gimpuls = &lt;750, inf)]] THEN class = {1}</entry>
</param>
<param name ="expert_preferred_conditions">
	<entry name="preferred-condition-1">1: IF [[seismic = {a}]] THEN class = {0}</entry>
	<entry name="preferred-condition-2">5: IF [[seismic = {b} AND seismoacoustic = {c}]] THEN class = {1}</entry>
	<entry name="preferred-attribute-1">inf: IF [[gimpuls = Any]] THEN class = {1}</entry>
</param>
<param name ="expert_forbidden_conditions">
	<entry name="forbidden-condition-1">IF [[seismoacoustic = b]] THEN class = {0}</entry>
	<entry name="forbidden-attribute-1">IF [[ghazard = Any]] THEN class = {1}</entry>
</param>
```
Please note several remarks:
* Inifinity is represented as `inf` string (`rule-1`, `preferred-attribute-1` ).
* Conditions based on continuous attributes are represented as intervals. Left-closed intervals are specified using `&lt;` symbol as `<` is reserved by XML syntax (`rule-2`).
* Multiplicity is specified before multiset element (`preferred-condition-1` and `preferred-condition-2`),
* Preferred/forbidden attributes are defined as conditions with special value `Any` (`preferred-attribute-1`, `forbidden-attribute-1`).

# 6. Library API
