# RuleKit

RuleKit is a comprehensive library for inducing rule-based data models [X]. It has the ability to produce classification [X], regression [X], and survival rules [X]. The suite provides user with the possibility to introduce a priori knowledge [X]. The powerful and flexible experimental environment allows straightforward investigation of different induction schemes. The analysis can be performed in batch mode, through RapidMiner plugin, or R package. A Java API is also provided for convinience. 

# Table of contents

1. [Batch interface](#1-batch-interface)
    1. [General information](#11-general-information)
    2. [Parameter set definition](#12-parameter-set-definition)
    3. [Dataset definition](#13-dataset-definition)
    4. [Examples](#14-example)
2. [RapidMiner plugin](#2-rapidminer-plugin)
3. [R package](#3-r-package)
4. [Quality and evaluation](#4-quality-and-evaluation)
    1. [Rule quality](#41-rule-quality)
	2. [Performance metrices](#42-performance-metices)
5. [Output files](#4-output-files)
    1. [Training report](#51-training-report)
    2. [Prediction performance report](#52-prediction-performance-report)    	
6. [User-guided induction](#6-user-guided-induction)
7. [Library API](#7-library-api)
[References](#references)

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

This section allows user to specify induction parameters. The package enables testing multiple parameter sets in a single run. Every parameter has its default value, thus, only selected parameters may be explicitly given. In the automatic induction, the following parameters apply:

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

Measure parameters apply only for classification and regression tasks and may have one of the values described in 4.1 section. In the survival analysis, log-rank statistics is used for induction, pruning, and voting.


## 1.3. Dataset definition

Definition of a dataset has the following form. 

```
<dataset>
     <label>...</label>							
     <out_directory>...</out_directory>			
     <weight>...</weight>                       
     <survival_time>...</survival_time>         
    
    <training> 
          <report_file>...</report_file>         
	  <train>
             <in_file>...</in_file>            
             <model_file>...</model_file>      
         </train>
         ...
    </training>
    
    <prediction>
	 <report_file>...</report_file>   	 
         <predict>
             <model_file>...</model_file>      	
             <test_file>...</test_file>         
             <predictions_file>...</predictions_file>  
         </predict>
         ...
    </prediction>
    
</dataset>
```
There are three main parts of the dataset definition: the general properties, the `traning` section, and the `prediction` section. General parameters and at least one of the two latter sections must be specified. 

### General properties

The general dataset properties are:
* `label` - label attribute,
* `out_directory` - output directory for storing results, subdirectories for all parameter sets are created automatically inside it,
* `weight` - optional weight attribute,
* `survival_time` - name of the survival time attribute, its presence indicates survival analysis problems.

### Training section

The `training` section allows generating models on specified training sets. It consists of the `report_file` field and any number of `train` subsections. Each `train` subsection is defined by:
* `in_file` - full path to the training file (in ARFF, CSV, XLS format),
* `model_file` - name of the output binary model file (without full path); for each parameter set, a separate model is generated under location *<out_directory>/<parameter_set name>/<model_file>*.

The `report_file` is created for each parameter set under *<out_directory>/<parameter_set name>/<report_file>* location. It contains a common text report for all training files: rule sets, model characteristics, detailed coverage information, training set prediction quality, KM-estimators (for survival problems), etc.   

### Prediction section

The `prediction` section allows making predictions on specified testing sets using models generated by the `training` section. It consists of the `performance_file` field and any number of `predict` subsections. Each `predict` subsection is defined by:
* `model_file` - name of the input binary model file generated in the `training` part; for each parameter set, a model is searched under location *<out_directory>/<parameter_set name>/<model_file>*, 
* `test_file` - full path to the testing file (in ARFF, CSV, XLS format),
* `predictions_file` - output data file with predictions (without full path); for each parameter set, a prediction is generated under location *<out_directory>/<parameter_set name>/<predictions_file>*.

The `performance_file` is created for each parameter set under *<out_directory>/<parameter_set name>/<performance_file>* location. It contains a common CSV report for all testing files with values of performance measures.
 

## 1.4. Example

Here we present how to prepare the XML experiment file for an example classification problem. Let the user be interested in two parameter sets:
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
* testing performance file: *performance.csv*

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
     	<performance_file>performance.csv</performance_file>  
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

In the training phase, RuleKit generates a subdirectory in the output directory for every investigated parameter set. 
Each of these subdirectories contains the models (one per training file) and a common text report. 
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

In the prediction phase, previously-generated models are applied on the specified testing sets producing the following files:
* *./results/mincov=5, C2/seismic-pred-1.arff*
* *./results/mincov=5, C2/seismic-pred-2.arff*
* *...*
* *./results/mincov=5, C2/seismic-pred-10.arff*
* *./results/mincov=5, C2/performance.csv*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-pred-1.arff*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-pred-2.arff*
* *...*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/seismic-pred-10.arff*
* *./results/mincov=11, RSS_RSS_BinaryEntropy/performance.csv*   

# 2. RapidMiner plugin

# 3. R package

# 4. Quality and evaluation

## 4.1. Rule quality


| Quality measure 			| Formula |
| :--- 						| :---|
| Accuracy 					| ![](https://chart.googleapis.com/chart?cht=tx&chl=p-n)| 
| BinaryEntropy				| ![](https://chart.googleapis.com/chart?cht=tx&chl=-\sum_{x{\in}X}P(x)\sum_{y{\in}Y}P(y\|x)\log_2{P(y\|x)}), where <br> ![](https://chart.googleapis.com/chart?cht=tx&chl=X=\left{\textrm{covered},\textrm{uncovered}\right},{\quad}Y=\left{\textrm{positive},\textrm{negative}}) <br> the probabilities can be calculated straightforwardly from confusion matrix [X]
| C1						| ![](https://chart.googleapis.com/chart?cht=tx&chl=Coleman\cdot\frac{2%2BKappa}{3})  |  
| C2						| ![](https://chart.googleapis.com/chart?cht=tx&chl=Coleman\cdot\frac{P%2Bp}{2P})|  
| CFoil						| ![](https://chart.googleapis.com/chart?cht=tx&chl=p\left({{\log}_{2}}\left(\frac{p}{p%2Bn}\right)-{{\log}_{2}}\left(\frac{P}{P%2BN}\right)\right))|  
| CNSignificnce				| ![](https://chart.googleapis.com/chart?cht=tx&chl=2\left(p\ln\left(\frac{p}{\left(p%2Bn\right)\frac{P}{P%2BN}}\right)%2Bn\ln\left(\frac{n}{\left(p%2Bn\right)\frac{N}{P%2BN}}\right)\right))| 
| Coleman					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{Np-Pn}{N(p%2Bn)})|  
| Correlation				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{pN-Pn}{\sqrt{PN(p%2Bn)(P-p%2BN-n)}})|  
| Coverage					| ![](https://chart.googleapis.com/chart?cht=tx&chl=p/P) |  
| FBayesianConfirmation		| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{pN-nP}{pN%2BnP})|  
| FMeasure					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{(\beta^2%2D1)\left(\frac{p}{p%2Dn}\right)\left(\frac{p}{P}\right)}{\beta^2\left(\frac{p}{p%2Dn}\right)%2B\frac{p}{P}})|  
| FullCoverage				| ![](https://chart.googleapis.com/chart?cht=tx&chl=(p%2Bn)/(P%2BN))|  
| GeoRSS					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\sqrt{\frac{p}{P}\left(1-\frac{n}{N}\right)})|  
| GMeasure					| ![](https://chart.googleapis.com/chart?cht=tx&chl=p/(p%2Bn%2Bg),g=2) | 
| InformationGain			| ![](https://chart.googleapis.com/chart?cht=tx&chl=Info\left(P,N\right)-Inf{{o}_{pn}}\left(P,N\right),Info\left(P,N\right)=-\left[\frac{P}{P+N}{{\log}_{2}}\frac{P}{P+N}+\frac{N}{P+N}{{\log}_{2}}\frac{N}{P+N}\right])Inf{{o}_{pn}}\left(P,N\right)=\frac{p+n}{P+N}Info\left( p,n \right)+\frac{P+N-p-n}{P+N}Info\left(P-p,N-n\right))|  
| JMeasure					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{1}{P%2BN}\left(p\ln\left(\frac{p\left(P%2BN\right)}{\left(p%2Bn\right)P}\right)%2Bn\ln\left(\frac{n\left(P%2BN\right)}{\left(p%2Bn\right)N}\right)\right))|  
| Kappa						| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{\left(P%2BN\right)\left(\frac{p}{p+n}\right)-P}{\left(\frac{P+N}{2}\right)\left(\frac{p+n+P}{p+n}\right)-P})|  
| Klosgen					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\left(\frac{p%2Bn}{P%2BN}\right)^{\omega}\left(\frac{p}{p%2Bn}-\frac{P}{P%2BN}\right),\omega=1)|  
| Laplace					| ![](https://chart.googleapis.com/chart?cht=tx&chl=(p%2B1)/(p%2Bn%2B2)) | 
| Lift						| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p\left(P+N\right)}{\left(p+n\right)P})|  
| LogicalSufficiency		| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{pN}{nP})|  
| MEstimate					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p%2Bm\frac{P}{P%2BN}}{p%2Bn%2Bm} )|  
| MutualSupport				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{n%2BP})|  
| Novelty					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{P%2BN}-\left(\frac{P\left(p%2Bn\right)}{{{\left(P%2BN\right)}^{2}}}\right))|  
| OddsRatio					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p\left(N-n\right)}{n\left(P-p\right)})|  
| OneWaySupport				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{p%2Bn}\ln\left(\frac{p\left(P%2BN\right)}{\left(p%2Bn\right)P}\right))|  
| PawlakDependencyFactor	| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p\left(P%2BN\right)-P\left(p%2Bn\right)}{p\left(P%2BN\right)%2BP\left(p%2Bn\right)})|  
| Q2						| ![](https://chart.googleapis.com/chart?cht=tx&chl=\left(\frac{p}{P}-\frac{n}{N}\right)\left(1-\frac{n}{N}\right))|  
| Precision					| ![](https://chart.googleapis.com/chart?cht=tx&chl=p/(p%2Bn))| 
| RelativeRisk				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{p%2Bn}\left(\frac{P%2BN-p-n}{P-p}\right))|  
| Ripper					| ![](https://chart.googleapis.com/chart?cht=tx&chl=(p-n)/(p%2Bn))|  
| RuleInterest				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p\left(P%2BN\right)-\left(p%2Bn\right)P}{P%2BN})|  
| RSS						| ![](https://chart.googleapis.com/chart?cht=tx&chl=p/P-n/N)|  
| SBayesian					| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{p%2Bn}-\frac{P-p}{P-p%2BN-n})|  
| Sensitivity				| ![](https://chart.googleapis.com/chart?cht=tx&chl=p/P)|  
| Specificity				| ![](https://chart.googleapis.com/chart?cht=tx&chl=(N-n)/n)|  
| TwoWaySupport				| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p}{P%2BN}\ln\left(\frac{p\left(P%2BN\right)}{\left(p%2Bn\right)P}\right))|  
| WeightedLaplace			| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{\left(p%2B1\right)\left(P%2BN\right)}{\left(p%2Bn%2B2\right)P})|  
| WeightedRelativeAccuracy	| ![](https://chart.googleapis.com/chart?cht=tx&chl=\frac{p%2Bn}{P%2BN}\left(\frac{p}{p%2Bn}-\frac{P}{P%2BN}\right))|  
| YAILS						| ![](https://chart.googleapis.com/chart?cht=tx&chl=(0.5%2B0.25{\cdot}Precision)\frac{p}{p%2Bn}%2B{(0.5-0.25{\cdot}Precision)}\frac{p}{P}) |




## 4.2. Performance metrices

### Common metrices

* `time_total_s` - algorithm execution time,
* `time_growing_s` - growing time, 
* `time_pruning_s` - pruning time,
* `#rules` - number of rules,
* `#conditions_per_rule` - average number of conditions per rule,
* `#induced_conditions_per_rule` - average number of induced conditions per rule (before pruning),
* `avg_rule_coverage` - average rule full coverage defined as (*p* + *n*) / (*P* + *N*),
* `avg_rule_precision` -average rule precision defined as *p* / (*p* + *n*),
* `avg_rule_quality` - average value of voting measure,
* `avg_pvalue` - average rule *p*-value
`avg_FDR_pvalue: 3.71317511237688E-4
`avg_FWER_pvalue: 3.726949446670513E-4
`fraction_0.05_significant: 1.0
`fraction_0.05_FDR_significant: 1.0
`fraction_0.05_FWER_significant: 1.0

### Classification
### Regression
### Survival

# 5. Output files

During training phase, RuleKit produces following types of files:
* a binary model (one per each training set) that can be applied in the prediction stage,
* a text report (common for all training files).
The result of the prediction phase are:
* a prediction file (one per each testing set), 
* a performance report (common for all testing files).


In the following subsections, a detailed description of training and performance reports are given. 


## 5.1. Training report

The report consists of separated sections, each corresponding to a single traning file:

```
================================================================================
bone-marrow-test-fold0.arff

... # content

================================================================================
bone-marrow-test-fold1.arff

... # content

```

At the beginning of a section, a rule model is given:

```
r1: IF Relapse = {0} AND Donorage = (-inf, 45.526027) AND Recipientage = (-inf, 17.45) THEN survival_status = {NaN} (p=119.0, n=0.0, P=168.0, N=0.0, weight=0.9999992726837377, pvalue=7.27316262327804E-7)
r2: IF HLAmismatch = {0} AND Relapse = {1} THEN survival_status = {NaN} (p=21.0, n=0.0, P=168.0, N=0.0, weight=0.9981544870337137, pvalue=0.0018455129662863223)
r3: IF Relapse = {0} AND Rbodymass = (-inf, 69.0) AND Recipientage = (-inf, 18.0) THEN survival_status = {NaN} (p=127.0, n=0.0, P=168.0, N=0.0, weight=0.9999999653103507, pvalue=3.468964926423013E-8)
r4: IF aGvHDIIIIV = {1} AND ANCrecovery = (-inf, 19.5) AND Stemcellsource = {1} AND Txpostrelapse = {0} THEN survival_status = {NaN} (p=82.0, n=0.0, P=168.0, N=0.0, weight=0.999992179496458, pvalue=7.820503541977608E-6)
r5: IF Donorage = <28.028767000000002, inf) AND CD34kgx10d6 = <1.2650000000000001, 6.720000000000001) AND CD3dCD34 = <0.8878985, inf) AND Rbodymass = <31.5, inf) AND Recipientage = <11.55, inf) THEN survival_status = {NaN} (p=20.0, n=0.0, P=168.0, N=0.0, weight=0.9999999999914838, pvalue=8.516187754992188E-12)
```
For each rule, additional statistics are given in the parentheses:
* elements of confusion matrix *p*, *n*, *P*, *N* (note that for classification *P* and *N* are fixed for each analyzed class, for regression *P* and *N* are determined for each rule on the basis of covered examples, for survival analysis all examples are considered positive, thus *N* and *n* equal to 0),
* weight - value of the voting quality measure,
* *p*-value - rule significance (classification: Fisher's exact test for for comparing confusion matrices; regression: &Chi;<sup>2</sup>- test for comparing label variance of covered vs. uncovered examples; survival: log-rank for comparing survival  functions  of  covered vs. uncovered examples).

Rules are followed by the detailed information about training set coverage:
```
Rules covering examples from training set (1-based):
2,4*;2*;3,5*;1,3*,4;1,3*;1,3*;1,3*;1,3*,4;1,3*;1,3*,4;3*,4;1,3,5*;1,3*;2*;1,3*,4;1,3*,4;2*;1,3*,4;1,3,5*;1,3*;1*;1,3*,4;3*,4;4,5*;3*,4;1,3*,4;1,3*,4;1,3*,4;1,3*,4;1,3*;1*,4;1,3*;1,3*;3*;1,3*,4;1,3*;1,3*,4;5*;1,3*,4;2*;3,5*;1,3*,4;1,3*;2,5*;1,3*,4;1,3*,4;3*,4;1,3*;1,3*;1,3*,4;2,4*;1,3*,4;4,5*;1,3*;2*;1,3*;2*;1,3*,4;1,3*;1,3*,4;3*;5*;1,3*,4;1,3*,4;1,3*;1,3*;1,3*,4;3,4,5*;4*;1,3*,4;1,3*;1,3*;1,3*,4;5*;4*;1,3*,4;1,3*,4;2*;1,3*;1,3*;1,3*;1,3*;1,3*;1,3*;1,3*,4;1,3*,4;1,3*;1,3*;1,3,5*;1,3*;1,3*,4;5*;1,3*;2*;1,3*;1*,4;1,3*,4;1,3*,4;1,3*,4;1,5*;1,3*;1,3*;4*;1,3*;3,5*;3*;1,3,4,5*;1,3*;5*;2*;1,3*;1,3*;1*,4;1,3*;1,3*,4;1,3*;1,3*,4;3*,4;1,3*;1,3*,4;4*;1,3*,4;1,3*;1,3*,4;4*;1,3*;1,3*;1,3*;1,3*,4;1*;1,3*,4;1,3*,4;2*;1,3*,4;1,3*,4;1,3,5*;2,4*;1,3*;1,3*,4;1,3*,4;1,3*;2,4*;1,3*,4;3*,4;1,3*,4;1,3*;1,3*;1,3*;1,3*,4;1,3*,4;1,3*,4;1,3*;1,3*;5*;2,4*;2*;1,3,5*;2,4*;3*;1,3*;2,4*;1,3*,4;1,3*,4;1,3*,4;1,3*,4;2*;4*;2,4*
```
For each example from the training set, a comma-separated list of rules covering that example is specified. Best rule (one with highest weight is marked with asterisk, lists corresponding to consecutive examples are separated with semicolon. The record `2,4*;2*;` at the beginning indicates that the first training example was covered by rules `r2` and `r4` (of which `r4` was the best), while the second training example was covered by `r2` only.  

Another section of the training report applies to survival problems only and contains tabular representation of survival curves. The first column represents time, then there are survival estimates of the entire training set and induced rules. This can be used for visualization of the algorithm results.
```
Estimator:
time, entire-set, r1, r2, r3, r4, r5, 
6.0, 0.9940476190476191, 0.9915966386554622,1.0,0.9921259842519685,1.0,1.0,
10.0, 0.988095238095238, 0.9915966386554622,1.0,0.984251968503937,1.0,1.0,
11.0, 0.9821428571428571, 0.9831932773109244,1.0,0.9763779527559056,1.0,1.0,
15.0, 0.976190476190476, 0.9831932773109244,1.0,0.9763779527559056,1.0,0.95,
19.0, 0.9702380952380951, 0.9831932773109244,1.0,0.9763779527559056,1.0,0.8999999999999999,
26.0, 0.9642857142857142, 0.9747899159663866,1.0,0.9685039370078741,1.0,0.8999999999999999,
28.0, 0.9523809523809522, 0.957983193277311,1.0,0.9527559055118111,0.9878048780487805,0.8999999999999999,
31.0, 0.9464285714285713, 0.9495798319327731,1.0,0.9448818897637796,0.9878048780487805,0.8999999999999999,
35.0, 0.9404761904761904, 0.9411764705882353,1.0,0.9370078740157481,0.9878048780487805,0.8999999999999999,
41.0, 0.9285714285714285, 0.9327731092436975,1.0,0.9291338582677167,0.975609756097561,0.8499999999999999,
42.0, 0.9226190476190476, 0.9243697478991597,1.0,0.9212598425196852,0.975609756097561,0.7999999999999998,
48.0, 0.9166666666666666, 0.9159663865546219,1.0,0.9212598425196852,0.975609756097561,0.7499999999999998,
53.0, 0.9107142857142857, 0.9159663865546219,1.0,0.9212598425196852,0.975609756097561,0.6999999999999998,
55.0, 0.9047619047619048, 0.9159663865546219,1.0,0.9133858267716537,0.975609756097561,0.6499999999999999,
56.0, 0.8988095238095238, 0.9159663865546219,1.0,0.9133858267716537,0.9634146341463414,0.6499999999999999,
```
The last element of the report are performance metrics evaluated on the training set. The contents of this section depends on the investigated problem. The detailed discussion of available metrices is presented in 4.2.  
```
time_total_s: 13.829900798
time_growing_s: 11.434164728999999
time_pruning_s: 2.3417725849999997
#rules: 5.0
#conditions_per_rule: 3.6
#induced_conditions_per_rule: 73.6
avg_rule_coverage: 0.43928571428571433
avg_rule_precision: 1.0
avg_rule_quality: 0.9996291809031488
avg_pvalue: 3.7081909685121595E-4
avg_FDR_pvalue: 3.71317511237688E-4
avg_FWER_pvalue: 3.726949446670513E-4
fraction_0.05_significant: 1.0
fraction_0.05_FDR_significant: 1.0
fraction_0.05_FWER_significant: 1.0
integrated_brier_score: 0.20866685101468796
```

## 5.2. Prediction performance report

The prediction performance report has the form of comma-separated table with rows corresponding to testing sets and columns representing performance metrices. 


# 6. User-guided induction


Expert knowledge is also specified through parameters:
```
<parameter_set name="paramset_1">
  	<param name="min_rule_cotvered">...</param>
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

# 7. Library API

# References

[Sikora, M, Wróbel, Ł, Gudyś, A (2018) GuideR: a guided separate-and-conquer rule learning in classification, regression, and survival settings, arXiv:1806.01579](https://arxiv.org/abs/1806.01579)

[Wróbel, Ł, Gudyś, A, Sikora, M (2017) Learning rule sets from survival data, BMC Bioinformatics, 18(1):285.](https://bmcbioinformatics.biomedcentral.com/articles/10.1186/s12859-017-1693-x) 

