# What is happing?

HBaseRDD scan result is not equal to HBase scan when `scan.setStartRow` and `scan.setStopRow` are set.

# How to run

```
sbt assembly

bin/spark-submit --class Reproducer --master local[*] --name "test" hbase-spark-odd-scan-behavior-reproducer/target/scala-2.10/hbase-spark-odd-scan-behavior-reproducer-assembly-0.1-SNAPSHOT.jar
```

This results in following assertion error.

```
Exception in thread "main" java.lang.AssertionError: assertion failed: HBase RDD: 7 != 10
	at scala.Predef$.assert(Predef.scala:179)
	at Reproducer$.main(Reproducer.scala:67)
	at Reproducer.main(Reproducer.scala)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:731)
	at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:181)
	at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:206)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:121)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
```