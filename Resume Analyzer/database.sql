-- Optional MySQL setup only. The default project run uses a local H2 database
-- at ./data/resume_analyzer.mv.db and creates tables automatically.

CREATE DATABASE IF NOT EXISTS resume_analyzer1;

-- Optional separate user:
-- CREATE USER IF NOT EXISTS 'resume_user'@'localhost' IDENTIFIED BY 'resume_pass';
-- GRANT ALL PRIVILEGES ON resume_analyzer1.* TO 'resume_user'@'localhost';
-- FLUSH PRIVILEGES;
