<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["username"], $_GET["password"])){
        $user = getUserByPhone($_GET['phone'], $conn);

        if ($user !== null){
            echo json_encode(['status' => 'failed', 'message' => 'Email or phone already exists']);
        }else{
            $hashedPassword = password_hash($_GET["password"], PASSWORD_DEFAULT);

            $insertUserQuery = $conn->prepare("INSERT INTO users (phone, username, password) VALUES (?, ?, ?);");
            $insertUserQuery->bind_param("sss", $_GET["phone"], $_GET["username"], $hashedPassword);

            if ($insertUserQuery->execute()) {
                echo json_encode(['status' => 'success']);
            }else{
                echo json_encode(['status' => 'failed']);
            }

            $insertUserQuery->close();
        }
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
