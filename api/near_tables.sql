-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Sep 09, 2024 at 07:58 AM
-- Server version: 10.11.8-MariaDB-cll-lve
-- PHP Version: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `u321831237_near`
--

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE `friends` (
  `id` int(11) NOT NULL,
  `friendId` int(11) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  `accepted` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;

--
-- Dumping data for table `friends`
--

INSERT INTO `friends` (`id`, `friendId`, `userId`, `accepted`) VALUES
(1, 1, 2, 1),
(2, 1, 3, 1),
(3, 1, 4, 1),
(4, 1, 5, 1),
(5, 1, 6, 1),
(6, 1, 7, 1),
(7, 1, 8, 1),
(8, 1, 9, 1),
(9, 1, 10, 1),
(10, 1, 11, 0),
(11, 1, 12, 1);

-- --------------------------------------------------------

--
-- Table structure for table `geopoints`
--

CREATE TABLE `geopoints` (
  `location` point NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(10) NOT NULL,
  `phone` varchar(32) NOT NULL,
  `username` varchar(32) NOT NULL,
  `password` varchar(512) NOT NULL,
  `image` int(4) DEFAULT 1,
  `joinDate` date DEFAULT current_timestamp(),
  `updateTime` datetime DEFAULT NULL,
  `location` point DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `phone`, `username`, `password`, `image`, `joinDate`, `updateTime`, `location`) VALUES
(1, '6912345678', 'Tom', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 1, '2023-02-05', '2024-09-09 07:35:15', 0x000000000101000000789ca223b9dc3640afd172a087524440),
(2, '805 542-1917', 'Jennifer', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 5, '2023-03-06', '2023-09-06 15:23:20', 0x000000000101000000b10342469bf836408d7857e2a64c4440),
(3, '571 814-1897', 'James', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 2, '2023-04-06', '2023-09-06 08:23:20', 0x000000000101000000132ba391cff336403095230967524440),
(4, '628 000-8043', 'Patricia', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 3, '2023-05-06', '2023-09-06 12:23:20', 0x000000000101000000b72572c119f436408fd893652d524440),
(5, '541 708-7708', 'Robert', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 4, '2023-05-09', '2023-09-06 09:23:20', 0x000000000101000000519c48d51bf43640461d67e43d524440),
(6, '434 115-1977', 'Michael', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 1, '2023-05-09', '2023-09-06 18:58:20', 0x000000000101000000f7d8a72dbff53640c13b54ae4b524440),
(7, '585 484-0136', 'Linda', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 6, '2023-05-09', '2023-09-09 15:58:20', 0x0000000001010000002d921bea1c103740388d486f134f4440),
(8, '850 282-0808', 'William', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 7, '2023-05-09', '2023-09-09 13:58:20', 0x0000000001010000006e765a6db4f236408d4127840e554440),
(9, '772 642-8807', 'Elizabeth', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 8, '2023-05-09', '2023-09-09 12:58:20', 0x000000000101000000fb8a7fe9feeb36401feffa71a0524440),
(10, '380 647-1930', 'Jessica', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 10, '2023-05-09', '2023-09-09 11:58:20', 0x000000000101000000822a244f48e73640b4f688e29f544440),
(11, '785 010-6682', 'Thomas', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 11, '2023-05-09', '2023-09-09 10:58:20', 0x0000000001010000007527333910013740de28684936544440),
(12, '650 008-4954', 'Christopher', '$2y$10$i307iw7cHmWwPZi7dcqfQOkdq4k0S/J3sMZrN3g1TEsdwR//P12De', 12, '2023-05-09', '2023-09-09 09:58:20', 0x000000000101000000f36217fb81fb3640442564d641544440);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `friends`
--
ALTER TABLE `friends`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `friends`
--
ALTER TABLE `friends`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
