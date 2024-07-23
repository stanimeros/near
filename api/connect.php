<?php
  $servername = "localhost";
  $dbname = "re552547_near";
  $username = "re552547_near_admin";
  $password = "~O;J)sNMoSgb";

  $conn = mysqli_connect($servername, $username, $password, $dbname);

  if (!$conn) {
    die("Error: " . mysqli_connect_error());
  }

  mysqli_set_charset($conn, "utf8");
  error_reporting(E_ALL);
  ini_set('display_errors', 1);
  header('Content-Type: application/json');