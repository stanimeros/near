<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"], $_GET["username"])){
        $friend = getUserByUsername($_GET["username"], $conn);

        if ($friend !== null){
            $request = requestExists($_GET["phone"], $_GET["username"], $conn);

            if ($request == null){
                $user = getUserByPhone($_GET['phone'], $conn);
                if ($user['username'] != $_GET["username"]){
                    $query = $conn->prepare("INSERT INTO friends (friendId, userId) 
                    SELECT t2.id , t1.id 
                    FROM users t1,users t2 
                    WHERE t1.phone = ? AND t2.username = ?;");
                    $query->bind_param("ss", $_GET["phone"], $_GET["username"]);
                    if ($query->execute()){
                        echo json_encode(['status' => 'success']);
                    }else{
                        echo json_encode(['status' => 'failed']);
                    }
                    $query->close();
                }else{
                    echo json_encode(['status' => 'failed', 'message' => 'Phone and username are linked']);
                }
            }else{
                echo json_encode(['status' => 'failed', 'message' => 'Request already exists']);
            }
        }else{
            echo json_encode(['status' => 'failed', 'message' => 'Username does not exist']);
        }
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
