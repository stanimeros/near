<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"])){
        $query = $conn->prepare("SELECT ST_X(users.location) as longitude, ST_Y(users.location) as latitude FROM users WHERE phone = ?;");
        $query->bind_param("s", $_GET["phone"]);
        if ($query->execute()){
            $location = $query->get_result()->fetch_assoc();
            echo json_encode(['status' => 'success', 'lon' => $location['longitude'], 'lat' => $location['latitude']]);
        }else{
            echo json_encode(['status' => 'failed']);
        }
        $query->close();
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
