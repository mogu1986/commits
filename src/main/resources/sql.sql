1. 查看某人，每天提交代码行数
select name, sum(`additions`),DATE(committed_date)
from commits
where name = '安小虎' group by DATE(committed_date)


2. 查看每天，每人提交代码行数
select name, sum(`additions`), DATE(committed_date)
from commits
group by name, DATE(committed_date)

3. 最终结果
select * from (
select name as '姓名', sum(`additions`) as line, DATE_FORMAT(committed_date, '%Y-%m') as '月份'
from commits
group by name, DATE_FORMAT(committed_date, '%Y-%m')
) as s order by line desc