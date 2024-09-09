<?php
  $servername = "localhost";
  $dbname = getenv('DB_NAME') ?: 'default_db_name';
  $username = getenv('DB_USERNAME') ?: 'default_username';
  $password = getenv('DB_PASSWORD') ?: 'default_password';

  $conn = mysqli_connect($servername, $username, $password, $dbname);

  if (!$conn) {
    die("Error: " . mysqli_connect_error());
  }

  mysqli_set_charset($conn, "utf8");
  error_reporting(E_ALL);
  ini_set('display_errors', 1);
  header('Content-Type: application/json');