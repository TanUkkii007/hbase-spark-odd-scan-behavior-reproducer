name := "hbase-spark-odd-scan-behavior-reproducer"

scalaVersion := "2.10.5"

resolvers ++= Seq(
  Classpaths.typesafeReleases
  , Resolver.bintrayRepo("typesafe", "maven-releases")
  , "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"
  , "Cloudera Repos"  at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".xsd" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".dtd" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".so" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val hbaseVersion = "1.2.0-cdh5.8.2"
val sparkVersion = "1.6.0-cdh5.8.2"

val hbaseServer = "org.apache.hbase" % "hbase-server" % hbaseVersion
val hbaseCommon = "org.apache.hbase" % "hbase-common" % hbaseVersion
val hbaseSpark = "org.apache.hbase" % "hbase-spark" % hbaseVersion
val hbaseHadoopCompat = "org.apache.hbase" % "hbase-hadoop-compat" % hbaseVersion
val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion

libraryDependencies ++= Seq(
  hbaseServer,
  hbaseCommon,
  hbaseSpark,
  hbaseHadoopCompat,
  sparkCore,
  sparkStreaming,
  sparkSql
)