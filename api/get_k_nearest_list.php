<?php
    include "connect.php";
    $k = $_GET["k"];
    $lon1 = $_GET["lon"];
    $lat1 = $_GET["lat"];
    $distanceInKm = $_GET["distanceInKm"];
    $table_name = $_GET["table_name"];

    $rowCount = 0;
    while ($rowCount < 1) {
        $sql =
            "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " .
            "FROM " .
            $table_name .
            " " .
            "WHERE MBRContains(" .
            "ST_Buffer(ST_GeomFromText('POINT(" .
            $lon1 .
            " " .
            $lat1 .
            ")', 4326), " .
            $distanceInKm/100 .
            "), location) " .
            "ORDER BY ST_Distance_Sphere(location, ST_GeomFromText('POINT(" .
            $lon1 .
            " " .
            $lat1 .
            ")', 4326)) ASC " .
            "LIMIT 1;";
        $result = $conn->query($sql);
        $result = $conn->query($sql);
        $rowCount = mysqli_num_rows($result);

        if ($rowCount < 1) {
            $distanceInKm = $distanceInKm * 2;
        }
    }

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $lon2 = $row["longitude"];
        $lat2 = $row["latitude"];
    }

    $lat1 = floatval($lat1);
    $lon1 = floatval($lon1);
    $lat2 = floatval($lat2);
    $lon2 = floatval($lon2);

    $rad_diffLon = deg2rad($lon2 - $lon1);
    $rad_diffLat = deg2rad($lat2 - $lat1);

    $rad_lat1 = deg2rad($lat1);
    $rad_lat2 = deg2rad($lat2);

    $rad = 6371;
    $a =
        pow(sin($rad_diffLat / 2), 2) +
        pow(sin($rad_diffLon / 2), 2) * cos($rad_lat1) * cos($rad_lat2);
    $c = 2 * asin(sqrt($a));
    $distanceInKm = $rad * $c + 0.010; //To avoid zero distance

    $rowCount = 0;
    $sqrt2 = sqrt(2);

    while ($rowCount < $k) {
        $sql =
            "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " .
            "FROM " .
            $table_name .
            " " .
            "WHERE MBRContains(" .
            "ST_Buffer(ST_GeomFromText('POINT(" .
            $lon2 .
            " " .
            $lat2 .
            ")', 4326), " .
            $distanceInKm/100 .
            "), location) " .
            "ORDER BY ST_Distance_Sphere(location, ST_GeomFromText('POINT(" .
            $lon2 .
            " " .
            $lat2 .
            ")', 4326)) ASC " .
            "LIMIT " .
            $k .
            ";";

        $result = $conn->query($sql);
        $rowCount = mysqli_num_rows($result);

        if ($rowCount < $k) {
            $distanceInKm = $distanceInKm * $sqrt2;
        }
    }

    header("Content-Type: application/json");
    $rows = [];
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
    }
    echo json_encode($rows);

    $conn->close();
?>
