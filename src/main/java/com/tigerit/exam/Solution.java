package com.tigerit.exam;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tigerit.exam.IO.*;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 */
public class Solution implements Runnable {
    @Override
    public void run() {

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
                    query.addColumNameToLeft(tokens[1]);
                }
                else if(tokens[0].equals(query.rightTableName)||tokens[0].equals(query.rightTableNameShortName))
                {
                    query.addColumNameToRight(tokens[1]);
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
            List<String> leftColumnNames,
            List<String> rightColumnNames
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

        for(String key:leftColumnNames)
        {
            System.out.print(key);
            System.out.print(" ");
        }
        for(String key:rightColumnNames)
        {
            System.out.print(key);
            System.out.print(" ");
        }
        System.out.println();
        for (Pair pair:pairs)
        {

            for(String key:leftColumnNames)
            {
                System.out.print(table1.get(key).get(pair.leftIndex));
                System.out.print(" ");
            }
            for(String key:rightColumnNames)
            {
                System.out.print(table2.get(key).get(pair.rightIndex));
                System.out.print(" ");
            }
            System.out.println();
        }


    }
    /*
    Helper methods

   */

    public String[] splitUsingWhiteSpace(String s)
    {
        String out[]=s.trim().split(" ");
        if(out.length == 1)
        {
            out = s.split(" ");
        }
        return out;
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
}
