<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"])){
        $sql = "SELECT users.phone, users.username, users.image, users.joinDate, users.updateTime, 
            ST_X(users.location) as longitude, ST_Y(users.location) as latitude, 
            ST_Distance_Sphere(location, ST_GeomFromText(
                CONCAT('POINT(', 
                    (SELECT ST_X(location) FROM users WHERE phone = ?), ' ', 
                    (SELECT ST_Y(location) FROM users WHERE phone = ?), ')'),
                4326)) 
            AS distance FROM users 
            INNER JOIN friends ON (friends.friendId = (SELECT id FROM users WHERE phone = ?) AND friends.userId = users.id) 
            OR (friends.userId = (SELECT id FROM users WHERE phone = ?) AND friends.friendId = users.id) 
            WHERE friends.accepted = 1 
            ORDER BY distance ASC;";
        $query = $conn->prepare($sql);
        $query->bind_param("ssss", $_GET["phone"], $_GET["phone"], $_GET["phone"], $_GET["phone"]);
        if ($query->execute()){
            $result = $query->get_result();
            $friends = array();
            
            while ($row = $result->fetch_assoc()) {
                $friends[] = $row; // Append each friend to the $friends array
            }
            echo json_encode(['status' => 'success', 'friends' => $friends]);
        }else{
            echo json_encode(['status' => 'failed']);
        }
        $query->close();
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
