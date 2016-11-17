
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{ Logging, SparkConf, SparkContext }

import scala.collection.JavaConverters._

case class HBaseMessageRowKey(roomId: Long, messageId: Long) {
  lazy val rowKey: Array[Byte] = Bytes.toBytes(roomId).reverse ++ Bytes.toBytes(messageId)
}

object Reproducer extends Logging {

  def main(args: Array[String]): Unit = {
    val n = 10
    val sparkConf = new SparkConf().setAppName("RDBSupportSpec")
    val sc = new SparkContext(sparkConf)
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", "localhost:2181")
    val hbaseContext = new HBaseContext(sc, hbaseConf)

    val conn: Connection = ConnectionFactory.createConnection()
    val table = TableName.valueOf("messages")
    val family = Bytes.toBytes("m")
    val q = Bytes.toBytes("message")

    val admin = conn.getAdmin
    try {
      admin.disableTable(table)
      admin.deleteTable(table)
    } catch {
      case e: Throwable =>
    }
    try {
      val tdesc = new HTableDescriptor(table)
      tdesc.addFamily(new HColumnDescriptor(family))
      admin.createTable(tdesc)
    } catch {
      case e: TableExistsException =>
    } finally {
      admin.close()
    }

    val t = conn.getTable(table)
    val puts = (1 to n).map { i =>
      val roomId = (i % 3).toLong
      HBaseMessageRowKey(roomId, i)
    }.map { key =>
      val put = new Put(key.rowKey)
      put.addColumn(family, q, Bytes.toBytes("message text"))
      put
    }
    t.put(puts.asJava)
    t.close()


    val scan = new Scan()
    scan.setStartRow(HBaseMessageRowKey(1L, 1L).rowKey)
    scan.setStopRow(HBaseMessageRowKey(Long.MaxValue, Long.MaxValue).rowKey)

    val hbaseMessageCount = countHBase(conn, table, family)
    assert(hbaseMessageCount == n, s"HBase Scan: $hbaseMessageCount != $n")

    val count = hbaseContext.hbaseRDD(table, scan).count()
    assert(count == n, s"HBase RDD: $count != $n")
// Above assertion fails with following error.
//    Exception in thread "main" java.lang.AssertionError: assertion failed: HBase RDD: 7 != 10
//    at scala.Predef$.assert(Predef.scala:179)
//    at Reproducer$.main(Reproducer.scala:67)
//    at Reproducer.main(Reproducer.scala)
//    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
//    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//    at java.lang.reflect.Method.invoke(Method.java:497)
//    at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:731)
//    at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:181)
//    at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:206)
//    at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:121)
//    at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)

    sc.stop()
  }

  def countHBase(conn: Connection, table: TableName, family: Array[Byte]): Int = {
    val t = conn.getTable(table)
    val s = t.getScanner(family)
    var i = 0
    var f = true
    try {
      while (f) {
        val v = s.next()
        if (v != null) {
          i += 1
        } else {
          f = false
        }
      }
      i
    } finally {
      s.close()
      t.close()
    }
  }

}
