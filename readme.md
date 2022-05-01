This is a COSC-580 course project.

Authors:
- Rui Chen
- Haoyang Zhang
- Kevin Qi

# Preloaded Data

```SQL
select * from TABLES;
select * from COLUMNS;
```

# Data Definition Language

## CREATE table

- constraints(keys)
  - primary key
  ```SQL
  create table company(compName char(255), compNo int, primary key(compNo));
  create table company(compName char(255), compNo int, primary key(compNo)); -- table already exists
  ```
  - foreign key
  ```SQL
  create table employee(empName char(255), empNo int, empCNo char(255),  
  primary key(empNo), foreign key (empCNo) references company(compName));
  drop table company; -- cannot drop
  ```
### insert not valid
```SQL
insert into company values('GU',1);
insert into company values('GWU',2);

insert into employee values('Amy',1,'GU');
insert into employee values('Tom',2,'GWUUU'); -- insert not valid

select * from employee;
```
### update cascade
```SQL
update company set compName = 'Georgetown' where compNo = 1;
select * from employee;
```


### delete set null
```SQL
delete from company where compNo = 1;
select * from employee;
```





## CREATE index

Before indexing:

```sql
select * from table3i where col2=75828;
```

Create index:

```sql
create index index3i on table3i(col2);
```

Select again:

```sql
select * from table3i where col2=75828;
```

Select non-existing condition with index:

```sql
select * from table3i where col2=100001;
```

Create duplicate index:

```sql
create index index3i on table4i(col2);
```

## DROP index

Drop index:

```sql
drop index table3i.index3i;
```

Drop non-existing index:

```sql
drop index table3i.index3i;
```

Select again:

```sql
select * from table3i where col2=75828;
```

# Data Manipulation Language

## SELECT

```SQL
select * from table1i;

select * from table2i;

select * from table3i;

select col1 from table1i;

select col3 from table1i; -- col3 doesn't exists in table3i
```

### WHERE

```sql
select * from table1i where col1=1 or col1=10 or col1=100;
select * from table1i where col1=1 or col1=10 or col1=100 and col1=5;
select * from table1i where (col1=1 or col1=10 or col1=100) and col1=5;
select * from table1i where col1<100 and col1>50;
select * from table1i where col1<100 and col1>50 and col2<70;

select * from table11 where 10>=col1+col2;
select * from table1i where col1*col2<10;
select * from table1i where col1*col2<10 and col1!=1;
select * from table1i where col1+col1*col2-col1/col2<12;
```

**will recursively searching for:**

#### operands

- Column
- Integer Value
- String Value

#### operators

- And Expression `and`
- Or Expression `or`
- Equal to (optimized with hashed indexes) `=`
- Not Equal to `!=`/`<>`
- Greater Than `>`
- GreaterEqual Than `>=`
- Minor Than `<`
- MinorEqual Than `<=`
- Parenthesis `()`
- Addition `+`
- Substraction `-`
- Multiplication `*`
- Division `/`

### JOIN

```sql
create table test (col1 int, col2 int NOT NULL, col3 int NOT NULL, PRIMARY KEY (col1));
insert into test values (1,1,1);
insert into test values (2,2,2);
insert into test values (3,3,3);
insert into test values (-5,-5,-5);
```

#### (INNER) JOIN

```sql
select * from table31 join test on table31.col1=test.col1;
select * from table31 inner join test on table31.col1=test.col1;

select * from table31 join test on table3i.col1=test.foobar; -- on statement with column name that doesn't exist!
select * from table31 join test on test.col1=test.col1; -- on statement with column names both from same table with same column
select * from table31 join test on table31.col1=table31.col2; -- on statement with column names both from same table only
```

#### LEFT (OUTER) JOIN

```sql
select * from table31 left join test on test.col1=table31.col1 order by table31.col1 limit 10;
```

#### RIGHT (OUTER) JOIN

```sql
select * from table31 right join test on test.col1=table31.col1;
```

#### FULL (OUTER) JOIN

```sql
select * from table31 full join test on test.col1=table31.col1 order by table31.col1 limit 10;
```

#### Optimization

##### COST-BASED

```sql
select * from table11 join table1i on table11.col1=table1i.col1;
```

Normally, without `indexing`, this statement would have the same speed as below:

```sql
select * from table11 join table1i;
```

And it's slow. But the first one is fast in our program. That's because our program can dynamically select join algorithm.

First it will detect whether there exists `indexing` for the current column in `on` statement. If there is one, it will directly use that `indexing`. So picking a record from this table would only cost constant time.

If there's no `indexing`,  it will then detect whether the total searching space is too large (we've set it to be 10k right now). So if the searching space is too large, it will temporarily build a `indexing` and then speed up the searching. 

On the other hand, if the searching space is not that large, we will just do `Cartesian product` for the two table and it won't be too slow.

We can try out this 100k joining 100k data to test it out.

```sql
select * from table31 join table3i on table31.col1=table3i.col1;
```

##### RULE-BASED

###### Inside WHERE Sub-clause

Cartesian product:

```sql
select table1i.col1, table21.col1, table21.col2 from table1i, table21 where table1i.col1=1 and table1i.col2=table1i.col1 limit 5;
```

Practical join condition:

```sql
select table2i.col1, table31.col1, table31.col2 from table2i, table31 where table2i.col2=table31.col1 order by table2i.col1 limit 5;
```

More complex condition expressions:

```sql
select table2i.col1, table31.col1, table31.col2 from table2i, table31 where table2i.col1>=5001 and table2i.col2=table31.col1 order by table2i.col1 limit 5;
```





### ORDER BY

#### Default

```sql
select * from table1i order by col1;
```

#### ASC

```sql
select * from table1i order by col1 asc;
```

#### DESC

```sql
select * from table1i order by col1 desc;
```

### LIMIT

```sql
select * from table3i order by col1 limit 10;
```

```sql
select * from table3i order by col1 desc limit 10;
```

```sql
select * from table3i where col1<=2000 order by col1 desc limit 10;
```

### DISTINCT

ALL-field

```sql
select distinct col2 from table31;
```

Limit larger than actual results:

```sql
select distinct col2 from table31 limit 10;
```

## UPDATE

Update with value:

```sql
update table31 set col2=2 where col1=1;
```

```sql
select * from table31 order by col1 limit 5;
```

Updating non-existing row:

```sql
update table31 set col2=2 where col1=0;
```

Update with expression:

```sql
update table3i set col2=col1/col2;
```

```sql
select distinct col2 from table3i;
```

```sql
update table3i set col2=col1/col2;
```

```sql
select distinct col2 from table3i limit 5;
```



## 1 MILLION Records!

```sql
select col1 from table4i where col2=75828;
```

```sql
create index index4i on table4i(col2);
```

```sql
select col1 from table4i where col2=75828;
```

```sql
select col1 from table4i where col2>500000 order by col1 limit 5;
```

Update the index as well:

```sql
update table4i set col2=col1/col2;
```

```sql
select distinct col2 from table4i;
```

Drop the index:

```sql
stmts.add("drop index table4i.index4i;");
```

Update again:

```sql
update table4i set col2=col1/col2;
```

```sql
select * from table4i order by col1 limit 5;
```

# Introduction

Good afternoon, professor. Today we are going to show the project3 - a DBMS project.

Firstly, I'd like to introduce the overall structure of our project.

1. Programming language: Java
    1. Strong, static
        - Easily to collaborate
        - hard way: challenging
            - popular
            - fast
            - eco-friendly
            - less error-prone
1. Design Pattern - Visitor
2. **Hierarchy** of our project - Object-Oriented Design
    2. DB, table (top-down)
        1. TABLES, COLUMNS
        2.
        3. DataRow, DataGrid
            1. Supported type...
    3. JSqlParser
        1. JSqlParser parses an SQL statement and translate it into a hierarchy of Java classes.
    4. Workflow
        1. load(), save()
        2. Abstract Syntax Tree
---
# DEMO (Implementation)
2. TODO: UI
3. TODO: DROP TABLE WHILE HAVing foreign key constraints
4. (Preloaded)
    - Select * from TABLES
    - Select * from COLUMNS
5. Data Definition Language & DML
    - Create table
        - Constraints(keys)
        - Insert not valid
        - update cascade
        - delete set null
        -
    - Drop Table
        - 1. STORAGE
            - SAVE & LOAD
    - Create index
    - Drop index
6. Data Manipulation Language
    1.  SELECT
        2. ***WHERE***(10min)
            1. operands, operator
                1. column integer string
                2. and, or, =, !=, >,>=,<,<=,+-\/\*
                    1. <span style = "color:red">《輕鬆了很多》</span>
                    2. Recursively thanks to our visitor pattern
        3. **JOIN**
            1. (INNER) JOIN
            2. OUTER JOIN
                1. LEFT
                2. RIGHT
                3. FULL
            3. Optimization
                1. cost-based: dynamic hash
                    1. not added in documnt
                    2. index auto creation
                2. rule-based:
                    1.  Finding Join condition in WHERE
        4. DISTINCT
           1. All-field
        5. ORDER BY
            1. ASCEND
            2. DE~
        
        6. LIMIT... - ORDER BY
            1. Optimization(rule-based)
    2. UPDATE
        1. expression
        2. value

## Citation
JSQL parser



## Constraints
### On the Structure
- [ ] 兩個基礎表格: `TABLES, COLUMNS`
- [X] 表格不能重名
  - `Map<String, Table> tables`  in `TOJO.Database`
- [ ] 每個表格內部列不能重名
  - 由於不會對表格結構進行變更, 因此僅在create table時創建臨時hashset用於檢測

### On the Data
- **Entity Integrity** 實體完整性
  - primary key (of each table) should be unique non-null values.
  - [ ] `Set<String> primaryKeySet` in `POJO.Table` : maintain a HashSet
- **Referential integrity** 參照完整性
  - any column in a base table that is declared a foreign key can only contain either null values or values from a parent table's primary key or a candidate key.
