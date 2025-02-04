CREATE TABLE participants(
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  owner_name VARCHAR(255) NOT NULL,
  owner_email VARCHAR(255) NOT NULL,
  is_confirmed BOOLEAN NOT NULL,
  trip_id UUID, 
  FOREIGN KEY (trip_id) REFERENCES  trips (id)

);