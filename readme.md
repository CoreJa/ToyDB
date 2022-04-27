This is a COSC-580 course project.

Authors:
- Rui Chen
- Haoyang Zhang
- Kevin Qi

# Checklist
- [ ] Database
  - [x] create table -yc
    - [x] create foreign key -yc
    - [ ] as -yc
  - [ ] drop table -yc
- [ ] Tables
  - [x] insert -hy
  - [x] update -hy
  - [x] delete -hy
  - [x] select -cr
    - [x] where -cr
      - [x] or -cr
      - [x] and -cr
      - [x] equals to -cr
      - [x] not equals to -cr
      - [x] minor than -cr
      - [x] minor than equals -cr
      - [x] greater than -cr
      - [x] greater than equals -cr
      - [x] add -cr
      - [x] subtract -cr
      - [x] multiply -cr
      - [x] divide -cr
    - [x] join -cr //on onley support one sub statement.
      - [x] join/inner join -cr
      - [x] left join/left outer join -cr
      - [x] right join/right outer join -cr
      - [x] full join/full outer join -cr
      - [x] on (with optimization) -cr
      - [ ] and
      - [ ] or
      - [x] equals to
    - [x] distinct -hy int√ string?
    - [x] order by () -hy
    - [x] limit () -hy
  - [x] Indexing -hy
    - [x] create -hy
    - [x] delete -hy
  - [ ] check foreign key -hycr
    - [x] on insert -hy
    - [x] on update
    - [x] on delete
      - [x] on dependence
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
  - [ ] `List<List<String>> foreignKeyList`: 每一個表的外碼列表. 同樣要檢查是否重複tableName, 同表重複ColumnName
  - [ ] `checkRefIntegrity(Database db)`:
    - 等待所有表格都加上之後, 檢查check foreign key(s)? --- database
    - 還是每create一個table就檢查? --- table
