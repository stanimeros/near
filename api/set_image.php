<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["image"])){
        $query = $conn->prepare("UPDATE users SET image = ? WHERE phone = ?;");
        $query->bind_param("is", $_GET["image"], $_GET["phone"]);
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
    
