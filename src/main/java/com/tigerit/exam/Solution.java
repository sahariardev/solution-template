package com.tigerit.exam;


import sun.misc.GC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigerit.exam.IO.*;
import static com.tigerit.exam.IO.readLineAsInteger;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 */
public class Solution implements Runnable {
    @Override
    public void run() {

        //String string = readLine();
        //queryInputAndParse();
        int numberOfTestCases=readLineAsInteger();
        for(int testCaseNumber=1;testCaseNumber<=numberOfTestCases;testCaseNumber++)
        {
            executeEachTestCase(testCaseNumber);
        }



    }

    public void executeEachTestCase(int testCaseNumber)
    {
        System.out.println("Test: "+testCaseNumber);
        HashMap<String,Table> db  = tableDataInputHelper();
        List<Query>  queries= queriesInputHelper();
        int testNumber=1;
        for (Query query:queries)
        {
            // System.out.println(query);
            List<ColumnNameWithTablePosition> selectedColumn = new ArrayList<>();

            if(query.isAllColumnNeedToBeSelect())
            {
                selectedColumn.addAll(db.get(query.leftTableName).columnNames.stream().map(col->{
                    return new ColumnNameWithTablePosition(col,"left");
                }).collect(Collectors.toList()));
                selectedColumn.addAll(db.get(query.rightTableName).columnNames.stream().map(col->{
                    return new ColumnNameWithTablePosition(col,"right");
                }).collect(Collectors.toList()));
            }
            else
            {
                selectedColumn = query.selectedColumns;
            }
            JoinAndPrint(
                    db.get(query.leftTableName).values,
                    db.get(query.rightTableName).values,
                    query.joinColumnNameleft,
                    query.joinColumnNameRight,
                    selectedColumn
            );
            testNumber++;
            System.out.println();
        }

    }

    public HashMap<String,Table> tableDataInputHelper()
    {

        HashMap<String,Table> db= new HashMap<>();
        int nT=readLineAsInteger();
        while (nT!=0)
        {

            nT--;
            String tableName=readLine();
            String nCnD[]=splitUsingWhiteSpace(readLine());
            int nC = Integer.parseInt(nCnD[0]);
            int nD = Integer.parseInt(nCnD[1]);
            String columnNames[] =splitUsingWhiteSpace(readLine());
            List<Integer> columsValue[]= new ArrayList[nC];
            for(int index=0;index<columsValue.length;index++)
            {
                columsValue[index] = new ArrayList<>();
            }
            while(nD !=0)
            {
                nD--;
                String data[]=splitUsingWhiteSpace(readLine());
                if(data.length != columsValue.length) return null;

                for(int index=0;index<columsValue.length;index++)
                {
                    columsValue[index].add(Integer.parseInt(data[index]));
                }

            }
            Table table = new Table();
            table.columnNames= Arrays.stream(columnNames).map(x->x).collect(Collectors.toList());
            for(int index=0;index<columsValue.length;index++) {
                table.addValueToMap(columnNames[index], columsValue[index]);
            }
            db.put(tableName,table);
        }

        return  db;
    }

    public List<Query>  queriesInputHelper()
    {
        int numberOfQueries = readLineAsInteger();
        List<Query> queries = new ArrayList<>();

        while (numberOfQueries!=0)
        {
            numberOfQueries--;
            queries.add(queryInputAndParse());
            readLine();
        }
        return queries;
    }

    /*
    This methods Handles One Query input and Store query data in query class
     */
    public Query queryInputAndParse()
    {

        String line1[]= splitUsingWhiteSpace(readLine());
        String line2[]= splitUsingWhiteSpace(readLine());
        String line3[]= splitUsingWhiteSpace(readLine());
        String line4[]= splitUsingWhiteSpace(readLine());

        String leftTable="";
        String rightTable="";
        Query query = new Query();
        query.leftTableName=line2[1];
        query.rightTableName=line3[1];


        if(line2.length == 3)
        {
            query.tableHasShortName=true;
            query.leftTableNameShortName=line2[2];
        }
        if(line2.length == 3)
        {

            query.rightTableNameShortName=line3[2];
        }


        if(line4[1].split("\\.")[0].equals(query.leftTableName) || line4[1].split("\\.")[0].equals(query.leftTableNameShortName))
        {
            query.joinColumnNameleft=line4[1].split("\\.")[1];
        }
        else if(line4[1].split("\\.")[0].equals(query.rightTableName) || line4[1].split("\\.")[0].equals(query.rightTableNameShortName))
        {
            query.joinColumnNameRight=line4[1].split("\\.")[1];
        }
        if(line4[3].split("\\.")[0].equals(query.rightTableName) || line4[3].split("\\.")[0].equals(query.rightTableNameShortName))
        {
            query.joinColumnNameRight=line4[3].split("\\.")[1];
        }
        else  if(line4[3].split("\\.")[0].equals(query.leftTableName) || line4[3].split("\\.")[0].equals(query.leftTableNameShortName))
        {
            query.joinColumnNameleft=line4[3].split("\\.")[1];
        }
        if(line1.length == 2)
        {
            query.selectAll=true;
        }
        else
        {
            query.selectAll=false;

            for(int index=1;index<line1.length;index++)
            {
                String tokens[]=line1[index].replace(",","").split("\\.");
                if(tokens[0].equals(query.leftTableName)||tokens[0].equals(query.leftTableNameShortName))
                {
                    ColumnNameWithTablePosition column = new ColumnNameWithTablePosition(tokens[1],"left");
                    query.selectedColumns.add(column);

                }
                else if(tokens[0].equals(query.rightTableName)||tokens[0].equals(query.rightTableNameShortName))
                {
                    ColumnNameWithTablePosition column = new ColumnNameWithTablePosition(tokens[1],"right");
                    query.selectedColumns.add(column);

                }
            }
        }

        return query;
    }

    /*
    This method does the join operation and the print the result
     */
    public void JoinAndPrint(
            HashMap<String, List<Integer>> table1,
            HashMap<String,List<Integer>> table2,
            String joinColumnLeft,
            String joinColumnRight,
            List<ColumnNameWithTablePosition> selectedColumnNames
    )

    {
        HashMap<Integer,JoinIndex> joinInformation  = new HashMap<>();

        List<Integer> left=table1.get(joinColumnLeft);
        for(int c=0;c<left.size();c++)
        {
            if(joinInformation.containsKey(left.get(c)))
            {
                JoinIndex n = joinInformation.get(left.get(c));
                n.addToLeft(c);
                joinInformation.put(left.get(c),n );
            }
            else
            {
                JoinIndex n = new JoinIndex();
                n.addToLeft(c);
                joinInformation.put(left.get(c), n);
            }
        }

        List<Integer> right=table2.get(joinColumnRight);
        for(int c=0;c<right.size();c++)
        {
            if(joinInformation.containsKey(right.get(c)))
            {
                JoinIndex n = joinInformation.get(right.get(c));
                n.addToRight(c);
                joinInformation.put(right.get(c),n );
            }

        }
        List<Pair> pairs= new ArrayList<>();

        for (Integer i:joinInformation.keySet())
        {

            JoinIndex ji = joinInformation.get(i);
            if(ji.isJoinPossible())
            {
                List<Integer> leftList=ji.left;
                List<Integer> rightList=ji.right;
                for(Integer i2: leftList)
                {
                    for(Integer i3: rightList)
                    {
                        Pair p =new Pair(i2,i3);
                        pairs.add(p);

                    }
                }
            }
        }

        HashMap<String,List<Integer>> out = new HashMap<>();

        for(ColumnNameWithTablePosition key:selectedColumnNames)
        {
            System.out.print(key.columnName);
            System.out.print(" ");
        }

        System.out.println();
        List<String> row_values_list= new ArrayList<>();
        for (Pair pair:pairs)
        {
            String row_values="";
            for(ColumnNameWithTablePosition key:selectedColumnNames)
            {
                if(key.tablePosition.equals("left")) {
                    row_values = row_values + " " + table1.get(key.columnName).get(pair.leftIndex);
                }
                else if(key.tablePosition.equals("right"))
                {
                    row_values=row_values+" "+table2.get(key.columnName).get(pair.rightIndex);
                }
            }
            row_values_list.add(row_values.trim());
        }
        Collections.sort(row_values_list, new CompareTwoRow());
        for(String value : row_values_list)
        {
            System.out.println(value);
        }


    }

    public String[] splitUsingWhiteSpace(String s)
    {
      return s.split(" ");
    }

    /*
    Helper classes
     */
    class Table
    {
        HashMap<String, List<Integer>> values = new HashMap<>();
        List<String> columnNames= new ArrayList<>();

        public void addValueToMap(String key,List<Integer> value)
        {
            values.put(key,value);
        }
        public void addValueToList(String value)
        {
            columnNames.add(value);
        }
    }

    class ColumnNameWithTablePosition
    {
        String columnName;
        String tablePosition;

        public ColumnNameWithTablePosition(String columnName, String tablePosition)
        {
            this.columnName = columnName;
            this.tablePosition = tablePosition;
        }
    }

    class Query
    {
        public String leftTableName;
        public String rightTableName;
        public String leftTableNameShortName;
        public String rightTableNameShortName;
        public String joinColumnNameleft;
        public String joinColumnNameRight;
        public boolean tableHasShortName=false;
        public boolean selectAll=false;
        public List<ColumnNameWithTablePosition> selectedColumns = new ArrayList<>();

        public List<String> columnNamesForSelectFromLeft = new ArrayList<>();
        public List<String> columnNamesForSelectFromRight = new ArrayList<>();


        public boolean isAllColumnNeedToBeSelect()
        {
            return selectAll;
        }
        public boolean doesTableHasShortName()
        {
            return tableHasShortName;
        }
        public void addColumNameToLeft(String name)
        {
            columnNamesForSelectFromLeft.add(name);
        }
        public void addColumNameToRight(String name)
        {
            columnNamesForSelectFromRight.add(name);
        }
        public void addSelectedColumnNames(ColumnNameWithTablePosition column)
        {
            selectedColumns.add(column);
        }



        @Override
        public String toString() {
            return "Query{" +
                    "leftTableName='" + leftTableName + '\'' +
                    ", rightTableName='" + rightTableName + '\'' +
                    ", leftTableNameShortName='" + leftTableNameShortName + '\'' +
                    ", rightTableNameShortName='" + rightTableNameShortName + '\'' +
                    ", joinColumnNameleft='" + joinColumnNameleft + '\'' +
                    ", joinColumnNameRight='" + joinColumnNameRight + '\'' +
                    ", tableHasShortName=" + tableHasShortName +
                    ", selectAll=" + selectAll +
                    ", columnNamesForSelectFromLeft=" + columnNamesForSelectFromLeft +
                    ", columnNamesForSelectFromRight=" + columnNamesForSelectFromRight +
                    '}';
        }
    }
    class JoinIndex
    {
        public List<Integer> left= new ArrayList<>();
        public List<Integer> right= new ArrayList<>();
        public boolean isJoinPossible()
        {
            return left.size()!=0 && right.size()!=0;
        }
        public void addToLeft(int val)
        {
            left.add(val);
        }
        public void addToRight(int val)
        {
            right.add(val);
        }

        @Override
        public String toString() {
            return "JoinIndex [left=" + left + ", right=" + right + "]";
        }
    }
    class Pair
    {
        int leftIndex;
        int rightIndex;
        public Pair(int l,int r)
        {
            leftIndex = l;
            rightIndex = r;
        }
        @Override
        public String toString() {
            return "Pair [leftIndex=" + leftIndex + ", rightIndex=" + rightIndex + "]";
        }

    }

    class CompareTwoRow implements Comparator<String>
    {

        @Override
        public int compare(String row1, String row2) {

            String row_one_values []= row1.split(" ");
            String row_two_values []= row2.split(" ");

            for(int index=0;index<row_one_values.length;index++)
            {
                if(intVal(row_one_values[index])>intVal(row_two_values[index]))
                {
                    return 1;
                }
                else if(intVal(row_one_values[index])<intVal(row_two_values[index]))
                {
                    return -1;
                }
            }
            return 0;



        }
        public int intVal(String val)
        {
            return Integer.parseInt(val);
        }
    }
}







