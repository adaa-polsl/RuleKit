
### Experiment definition



Definition of the experiment has the following form:

```
<dataset>
     <label>...</label>
     <survival_time>...</survival_time>            # only for survival datasets
     <weight>...</weight>                          # optional
   
    <training>  
         <report_path>...</report_path>           # TXT report (rule sets, KM-estimators, etc.)
         <train>
             <in_file>...</in_file>               # input ARFF file
             <model_binary>...<model_binary>      # output binary model (operator configuration name added automatically)
         </train>
         ...
    </training>
    
    <prediction>
         <predict>
             <model_binary>...<model_binary>      # input binary model (operator configuration name added automatically)
             <test_file>...<test_file>            # input ARFF file
             <out_file>...<out_file>              # output ARFF file with predictions  (operator configuration name added automatically)
         </predict>
         ...
    </prediction>
    
    <evaluation>
         <report_path>                            # CSV report (performance metrics) (operator configuration name added automatically)
         <evaluate>
             <predicted_file>...<predicted_file>  # ARFF file with predictions
             <ref_file>...<ref_file>              # reference ARFF file
         </evaluate>
         ...
     <evaluation>
    
  	<delete>
          <file>...</file>                        
          <file>...</file>
          ...
    </delete>

</dataset>
```
