<?php
    require ("connect.php");
    require ("functions.php");

    if (isset($_GET["phone"])){
        $query = $conn->prepare("SELECT t2.phone, t2.username, t2.image 
            FROM friends 
            INNER JOIN users t1 ON friends.friendId=t1.id 
            INNER JOIN users t2 ON friends.userId=t2.id 
            WHERE t1.phone = ? AND friends.accepted = 0;");
        $query->bind_param("s", $_GET["phone"]);
        if ($query->execute()){
            $result = $query->get_result();
            $requests = array();
            
            while ($row = $result->fetch_assoc()) {
                $requests[] = $row; // Append each friend to the $friends array
            }
            echo json_encode(['status' => 'success', 'requests' => $requests]);
        }else{
            echo json_encode(['status' => 'failed']);
        }
        $query->close();
    }else{
        echo json_encode(['status' => 'failed', 'message' => 'Missing attributes']);
    }
    $conn->close();
    
