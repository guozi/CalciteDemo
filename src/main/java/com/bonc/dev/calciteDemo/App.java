package com.bonc.dev.calciteDemo;

import com.bonc.dev.calciteDemo.function.TimeOperator;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;

import java.sql.*;
import java.util.Properties;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws NoSuchMethodException, SecurityException {
        try {
            Class.forName("org.apache.calcite.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        Properties info = new Properties();
        try {
            /**
             * 本步骤中，使用DriverManager 建立连接，步骤如下：
             *
             */
            CalciteConnection calciteConn = (CalciteConnection) DriverManager.getConnection("jdbc:calcite:model=" + json() + ";caseSensitive=false", info);
            calciteConn.getRootSchema().add("YEAR", ScalarFunctionImpl.create(TimeOperator.class.getMethod("YEAR", Date.class)));
            calciteConn.getRootSchema().add("COM", ScalarFunctionImpl.create(TimeOperator.class.getMethod("COM", String.class, String.class)));
            /**
             * getTables 操作会将数据源原始的table概念转换为calcite中的Table概念，还要将操作函数类中的所有操作函数
             * 读取出来。
             */
            ResultSet result = calciteConn.getMetaData().getTables(null, null, null, null);
            while (result.next()) {
                System.out.println("Catalog : " + result.getString(1) + ",Database : " + result.getString(2) + ",Table : " + result.getString(3));
            }
            result.close();
            /**
             * getColumns 操作会将原始数据源中的Column概念(列数据类型等)转换为Calcite中的Column概念(RelDataType)
             */
            result = calciteConn.getMetaData().getColumns(null, null, "Student", null);
            while (result.next()) {
                System.out.println("name : " + result.getString(4) + ", type : " + result.getString(5) + ", typename : " + result.getString(6));
            }
            result.close();

            Statement st = calciteConn.createStatement();
            /**
             * 调用scan函数读取数据库，在执行query的过程中，会调用Calcite的MemoryTable概念的scan来获取表的迭代器，这个迭代器是我们自己定义的
             * 用于对表数据进行迭代处理。这个处理过程就是实现从原始数据源的数据到我们所需要的数据之间的转换过程，要调用迭代器的功能来完成实现。
             */
            result = st.executeQuery("select S.\"id\", SUM(S.\"classId\") from \"Student\" as S group by S.\"id\"");
            while (result.next()) {
                System.out.println(result.getString(1) + "\t" + result.getString(2));
            }
            result.close();
            calciteConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String json() {
        return "inline:{\n" +
                "  version: '1.0',\n" +
                "  defaultSchema: 'school',\n" +
                "  schemas: [\n" +
                "    {      \n" +
                "      type: 'custom',\n" +
                "      name: 'school',\n" +
                "      factory: 'com.bonc.dev.calciteDemo.MemorySchemaFactory',\n" +
                "      operand: {\n" +
                "        param1: 'hello',\n" +
                "        param2: 'world'\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
