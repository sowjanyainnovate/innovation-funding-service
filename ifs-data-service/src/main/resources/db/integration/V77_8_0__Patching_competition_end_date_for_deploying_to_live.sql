update milestone set DATE = DATE_ADD(DATE, INTERVAL 10 YEAR) where competition_id = 1 and type != 'OPEN_DATE';