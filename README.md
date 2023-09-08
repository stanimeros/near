# Near
Αυτή η εφαρμογή δημιουργήθηκε στα πλαίσια της πτυχιακής μου εργασίας στο πανεπιστήμιο Μακεδονίας και είναι μια εφαρμογή κοινωνικού δικτύου με στόχο την ιδιωτικότητα βασισμένη στον αλγόριθμο Two Hop Privacy.

<h3>Παραμετροποίηση</h3>
<strong>Η τοποθεσία του κινητού πρέπει να είναι εντός 100χλμ από το πανεπιστήμιο Μακεδονίας.</strong>

Στην MainActivity.java υπάρχουν τα εξής:
<ul>
  <li>Παράμετρος choice οπού γίνεται η επιλογή της μεθόδου που θα εκτελέσει τον αλγόριθμο. (προκαθορισμένες επιλογές)</li>
  <li>Παράμετρος k που συμβολίζει το k-anonymity και μπορεί να πάρει οποιαδήποτε τιμή.</li>
  <li>Παράμετρος kmFile οπού συμβολίζει ποιον πίνακα ή βάση δεδομένων να κοιτάει το project. Μπορεί να είναι αυστηρά 1km, 5km, 25km, 100km ενώ δε δουλεύει με όλες τις μεθόδους.</li>
  <li>Παράμετρος starting_km οπού συμβολίζει την αρχική τιμή που παίρνει το bounding box με τιμή default: 0.05.</li>
  <li>Έπειτα για τα δέντρα (KD-Tree και Quad Tree) υπάρχουν τα tree max points και το αντίστοιχο tree leaf max points οπού συμβολίζει το bucket max. Ένας μεγάλος αριθμός (π.χ. 2 million) tree max points δεν θα ενεργοποιήσει το group management ενώ ένας μικρός (π.χ. 100k) αριθμός tree max points θα δημιουργήσει όσα δέντρα χρειάζονται με τη βοήθεια του group helper. Οι αριθμοί είναι συγκεκριμένοι διότι το μεγαλύτερο dataset (100km) έχει 1.6 εκατομμύρια points.</li>
</ul>

<h3>Μέθοδοι</h3>
Για την ομαλή λειτουργία της εφαρμογής υπάρχουν αυτοί οι συνδυασμοί παραμέτρων:
<ul>
  <li>linear : Δουλεύουν όλοι οι συνδυασμοί αλλά καθυστερεί σε μεγάλο όγκο δεδομένων.</li>
  <li>sqlite_default : Δουλεύουν όλοι οι συνδυασμοί αλλά χρειάζεται μερικά λεπτά να δημιουργήσει τη βάση στο πρώτο run. //Δεν αποδίδει καλά χρονικά.</li>
  <li>sqlite_rtree : Δεν δουλεύει, υπάρχει πρόβλημα στην select.</li>
  <li>sqlite_spatialite : Δουλεύει σε όλους τους συνδυασμούς, θα χρειαστεί μερικά λεπτά να δημιουργηθεί η βάση των 25km και 100km, ενώ για τις βάσεις 1km, 5km τις τραβάει απευθείας από τα assets.</li>
  <li>sqlserver : Δουλεύει από τον ωκεανό, στον οποίο έχουν ανέβει τα 1km, 5km και 25km. Οπότε δεν θα λειτουργήσει στην περίπτωση των 100km.</li>
  <li>kd και quad: Δουλεύουν καλά αφού φορτώσουν στην μνήμη. Θα χρειαστεί μερικά δευτερόλεπτα. Τα 1km, 5km ακόμα και 25km μπορούν να τα χειριστούν με ένα δέντρο χωρίς το group manager. Από εκεί και έπειτα επιβαρύνουν πολύ την μνήμη με λύση τα group helper τα οποία ξεκινάνε άμα τα dataset points > tree max points.</li>
  <li>rtree : Δουλεύει άψογα μόλις φορτώσει στην μνήμη. Δε μπορεί ανταπεξέλθει στα 100km.</li>
</ul>

<h3>Επιπλέον πληροφορίες</h3>
<ul>
  <li>Το αρχείο MainActivity.java περιέχει βασικές παραμέτρους.</li>
  <li>Στο αρχείο Feed.java το gps εντοπίζει αλλαγή τοποθεσίας και ξεκινάει την διαδικασία.</li>
  <li>Στο αρχείο MyLocation.java ξεκινάει η μέθοδος που έχει επιλεχθεί.</li>
  <li>Server: Η αναζήτηση κοντινότερων σημείων γίνεται μέσω service ενω όλα τα υπόλοιπα (φιλίες, αιτήματα, λίστες φίλων) γίνονται μέσω jdbc.</li>
</ul>

Σύνδεσμος επιπλέον αρχέιων: <a href="https://drive.google.com/drive/folders/1_DANO0D_Nn3OxCPlsbVPUt5vqUHI4xrr">Google Drive</a>


