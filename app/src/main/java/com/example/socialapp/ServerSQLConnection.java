package com.example.socialapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

public class ServerSQLConnection implements Runnable{
    private boolean executed = false;
    private boolean select;
    private String sql;

    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
    private ArrayList<String> results = new ArrayList<>();

    public void Select(String sql){
        this.select = true;
        this.sql = sql;
    }

    public void Insert(String sql){
        this.select = false;
        this.sql = sql;
    }

    public void Update(String sql){
        this.select = false;
        this.sql = sql;
    }

    public void run() {
        Connection con;
        try{
            Class.forName(MainActivity.className);
            con = DriverManager.getConnection("jdbc:"+MainActivity.jdbcName+"://"+MainActivity.serverIp+":"+MainActivity.port+"/"+MainActivity.database+"?allowMultiQueries=true",MainActivity.user,MainActivity.password);
            Statement statement = con.createStatement();
            System.out.println(sql);
            if (!select){ // INSERT OR UPDATE
                statement.executeUpdate(sql);
            }else { // SELECT
                results.clear();
                ResultSet rs = statement.executeQuery(sql);

                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();

                if (rs.next()) {
                    do {
                        int i = 1;
                        while (i <= columnCount) {
                            results.add(rs.getString(i++));
                        }
                    } while (rs.next());
                }
            }
            executed = true;
            con.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error: SQL command not executed!");
        }
    }

    public String getResult() {return results.get(0);}

    public ArrayList<String> getResults() {return results;}

    public boolean isExecuted() {return executed;}
}

