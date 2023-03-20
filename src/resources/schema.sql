CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name varchar(50) NOT NULL,
  username varchar(50) NOT NULL,
  password varchar(255) NOT NULL,
  role varchar(50) NOT NULL,
  locked bool NOT NULL
);