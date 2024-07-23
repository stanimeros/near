<?php

    function getUserByPhone($phone, $conn){
        $userQuery = $conn->prepare("SELECT phone, username, password, image, joinDate, updateTime FROM users WHERE phone = ?;");
        $userQuery->bind_param("s", $phone);
        $userQuery->execute();
        $result = $userQuery->get_result();
        // Check if any rows were returned
        if ($result->num_rows > 0) {
            // User exists, fetch the user data
            $user = $result->fetch_assoc();
        } else {
            // User does not exist
            $user = null;
        }
        $userQuery->close();
        return $user;
    }

    function getUserByUsername($username, $conn){
        $userQuery = $conn->prepare("SELECT phone, username, password, image, joinDate, updateTime FROM users WHERE username = ?;");
        $userQuery->bind_param("s", $username);
        $userQuery->execute();
        $result = $userQuery->get_result();
        // Check if any rows were returned
        if ($result->num_rows > 0) {
            // User exists, fetch the user data
            $user = $result->fetch_assoc();
        } else {
            // User does not exist
            $user = null;
        }
        $userQuery->close();
        return $user;
    }

    function requestExists($phone, $username, $conn){
        $requestQuery = $conn->prepare("SELECT friends.id 
            FROM friends INNER JOIN users t1 INNER JOIN users t2 ON friends.friendId=t1.id AND friends.userId=t2.id 
            WHERE (t1.phone = ? AND t2.username = ?) OR (t2.phone = ? AND t1.username = ?);");
        $requestQuery->bind_param("ssss", $phone, $username, $phone, $username);
        $requestQuery->execute();
        $result = $requestQuery->get_result();
        // Check if any rows were returned
        if ($result->num_rows > 0) {
            // User exists, fetch the user data
            $request = $result->fetch_assoc();
        } else {
            // User does not exist
            $request = null;
        }
        $requestQuery->close();
        return $request;
    }