<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["password"])){
        $user = getUserByPhone($_GET['phone'], $conn);

        if ($user !== null){
            if (password_verify($_GET["password"], $user['password'])) {
                echo json_encode(['status' => 'success', 'user' => $user]);
            }else{
                echo json_encode(array('status' => 'failed', 'message' => 'Incorrect password'));
            }
        }else{
            echo json_encode(['status' => 'failed', 'message' => 'User not found']);
        }
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
