<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["username"])){
        $query = $conn->prepare("DELETE friends.* FROM friends 
            INNER JOIN users t1 ON friends.friendId=t1.id 
            INNER JOIN users t2 ON friends.userId=t2.id 
            WHERE (t1.phone = ? AND t2.username = ?) OR (t2.phone = ? AND t1.username = ?);");
        $query->bind_param("ssss", $_GET["phone"], $_GET["username"], $_GET["phone"], $_GET["username"]);
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
    
