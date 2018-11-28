
### Experiment definition

STILL UNFINISHED

Definition of the experiment has the following form:

```
<experiment>
     <label>...</label>
     <survival_time>...</survival_time>
     <weight>...</weight>
     

  	<train>
        <in_file>...</in_file>
        <model_binary>...<model_binary>
        <model_text>...<model_text>
        <append_text>...<append_text>
    </train>
    
    <train>
        <in_file>...</in_file>
        <model_binary>...<model_binary>
        <model_text>...<model_text>
        <append_text>...<append_text>
    </train>
    
    ....
    
    <predict>
        <model_binary>...<model_binary>
        <test_file>...<test_file>
        <out_file>...<out_file>
    </predict>
    
    
    <evaluate>
        <predicted_file>...<predicted_file>
        <ref_file>...<ref_file>
        <report_file>...<report_file>
    </evaluate>
    
  	<delete>
      <file>...</file>
      <file>...</file>
    </delete>
  	<type>...</type>
  	<report_path>...</report_path>
</experiment>
```
