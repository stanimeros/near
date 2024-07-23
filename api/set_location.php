<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["lon"], $_GET["lat"])){
        $query = $conn->prepare("UPDATE users 
            SET updateTime = NOW(), location = ST_GeomFromText(?) WHERE phone = ?");
        $point = "POINT(" . $_GET["lon"] . " " . $_GET["lat"] . ")";
        $query->bind_param("ss", $point, $_GET["phone"]);
        if ($query->execute()){
            echo json_encode(['status' => 'success']);
        }else{
            echo json_encode(['status' => 'failed']);
        }
        $query->close();
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
