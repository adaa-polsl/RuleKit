# Batch interface

## Overall XML structure

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

## Parameter set definition

As each algorithm parameter has its default value, only selected parameters may specified by the user. In automatic mode, following parameters apply:

```
<parameter_set name="paramset_1">
  	<param name="min_rule_covered">...</param>
  	<param name="induction_measure">...</param>
  	<param name="pruning_measure">...</param>
	<param name="voting_measure">...</param>
</parameter_set>
```    
where:
* `min_rule_covered` - minimum number of previously uncovered examples a new rule has to cover,
* `induction_measure` - rule quality measure used during growing; one of the following: *Accuracy*, *C2*, *Correlation*, 		*Lift*,	*LogicalSufficiency*,	*Precision*, *RSS*,	*GeoRSS*, *SBayesian*, *BinaryEntropy*,
* `pruning_measure` - rule quality measure used during pruning; one of the aforementioned measures,
* `voting_measure` - rule quality measure used for voting; one of the aforementioned measures.

The measure parameters apply only for classification and regression problems - in survival datasets log-rank statistics is always used.

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


## Dataset definition

Definition of a dataset has the following form. All fields having `prefix` in their name are completed with parameter set name.

```
<dataset>
     <label>...</label>
     <survival_time>...</survival_time>            # only for survival datasets
     <weight>...</weight>                          # optional
   
    <training>  
         <report_prefix>...</report_prefix>          	 	# TXT report (rule sets, KM-estimators, etc.)
         <train>
             <in_file>...</in_file>               		# input ARFF file
             <model_binary_prefix>...<model_binary_prefix>      # output binary model 
         </train>
         ...
    </training>
    
    <prediction>
         <predict>
             <model_binary_prefix>...<model_binary_prefix>      # input binary model 
             <test_file>...<test_file>            		# input ARFF file
             <predicted_file_prefix>...<predicted_file_prefix>  # output ARFF file with predictions  
         </predict>
         ...
    </prediction>
    
    <evaluation>
         <report_prefix>...</report_prefix>       		# CSV report (performance metrics) 
         <evaluate>
             <predicted_file_prefix>...<predicted_file_prefix>  # input ARFF file with predictions 
             <ref_file>...<ref_file>              		# reference ARFF file
         </evaluate>
         ...
    <evaluation>
    
    <delete>
          <file_prefix>...</file_prefix>                        
          <file_prefix>...</file_prefix>
          ...
    </delete>

</dataset>
```

Example:

```
<dataset>
     <label>class</label>
    
    <training>  
         <report_prefix>seismic</report_prefix>           		# TXT report (rule sets, KM-estimators, etc.)
         <train>
             <in_file>seismic-train-1</in_file>               		# input ARFF file
             <model_binary_prefix>seismic-model-1<model_binary_prefix>  # output binary model 
         </train>
         
	  <train>
             <in_file>seismic-train-2</in_file>               		# input ARFF file
             <model_binary_prefix>seismic-model-2<model_binary_prefix>  # output binary model
         </train>
	 
	 ...
	 
    </training>
    
    <prediction>
         <predict>
             <model_binary_prefix>seismic-model-1<model_binary_prefix>      	# input binary model
             <test_file>seismic-train-1<test_file>            			# input ARFF file
             <predicted_file_prefix>seismic-pred-1<predicted_file_prefix>  	# output ARFF file with predictions  
         </predict>
	 
	 <predict>
             <model_binary_prefix>seismic-model-2<model_binary_prefix>      	# input binary model
             <test_file>seismic-train-2<test_file>            			# input ARFF file
             <predicted_file_prefix>seismic-pred-2<predicted_file_prefix>   	# output ARFF file with predictions  
         </predict>
	 
         ...
	 
    </prediction>
    
    <evaluation>
         <report_prefix>seismic</report_prefix>             			# CSV report (performance metrics) 
         <evaluate>
             <predicted_file_prefix>seismic-pred-1<predicted_file_prefix>  	# input ARFF file with predictions 
             <ref_file>seismic-test-1<ref_file>              			# reference ARFF file
         </evaluate>
	 
	  <evaluate>
             <predicted_file_prefix>seismic-pred-2<predicted_file_prefix>  	# input ARFF file with predictions 
             <ref_file>seismic-test-2<ref_file>              			# reference ARFF file
         </evaluate>
	 
         ...
	 
    <evaluation>
    
    <delete>
          <file_prefix>seismic-model-1</file_prefix>                        
          <file_prefix>seismic-pred-1</file_prefix>
	  
	  <file_prefix>seismic-model-2</file_prefix>                        
          <file_prefix>seismic-pred-2</file_prefix>
          
	  ...
	  
    </delete>
</dataset>
```
