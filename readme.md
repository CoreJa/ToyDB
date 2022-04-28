This is a COSC-580 course project.

Authors:
- Rui Chen
- Haoyang Zhang
- Kevin Qi

# DEMO (Implementation)

# //TODO: UI

blabla

# //TODO: 删表外键依赖

# Preloaded Data

```SQL
select * from TABLES;
select * from COLUMNS;
```

# Data Definition Language & DML

## CREATE table

### constraints(keys)



### insert not valid



### update cascade



### delete set null



## DROP table

### save



### load



## CREATE index



## DROP index



# Data Manipulation Language

## SELECT

### WHERE

#### operands



#### operators



#### recursively searching



### JOIN

#### (INNER) JOIN



#### LEFT (OUTER) JOIN



#### RIGHT (OUTER) JOIN



#### FULL (OUTER) JOIN



#### Optimization

##### COST-BASED



##### RULE-BASED



### ORDER BY

#### ASC



#### DESC



### DISTINCT

ALL-field



### LIMIT

#### Optimization

RULE-BASED



## UPDATE




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
        4. ORDER BY
            1. ASCEND
            2. DE~
        5. DISTINCT
            1. All-field
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
