#!$loc = $PWD."Path"
& docker run --rm -i -v ${loc}:/data test:latest java -jar ac-rules-1.3.6-all.jar $args