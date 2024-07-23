<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["username"])){

        $other_user = getUserByUsername($_GET["username"], $conn);

        if ($other_user == null) {
            $query = $conn->prepare("UPDATE users SET username = ? WHERE phone = ?;");
            $query->bind_param("ss", $_GET["username"], $_GET["phone"]);
            if ($query->execute()){
                echo json_encode(['status' => 'success']);
            }else{
                echo json_encode(['status' => 'failed']);
            }
            $query->close();
        }else{
            echo json_encode(['status' => 'failed', 'message' => 'Username already exists']);
        }
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
