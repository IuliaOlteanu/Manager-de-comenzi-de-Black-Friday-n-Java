# Manager-de-comenzi-de-Black-Friday-In-Java

Pentru procesarea comenzilor, se pornesc de la inceput P thread-uri care vor citi fiecare din fisierul de comenzi,
fiecare pornind de la linia corespunzatoare ID-ului sau si sarind din P in P linii, pentru a se asigura ca fiecare 
thread citeste alta grupare de linii. Apoi, fiecare thread va porni alt thread care va cauta cate un produs din 
comanda data, tinand intr-un AtomicInteger partajat intre toate thread-urile numarul de thread-uri active care 
cauta produse, asigurandu-se ca acesta nu depaseste niciodata P. Thread-urile ce cauta produse vor citi fisierul 
de produse si se vor opri asupra produsului corespunzator comenzii de care se ocupa si cu numarul dat de ID-ul sau. 
Odata ce toate aceste thread-uri ce cauta produse au terminat si au expediat produsele, thread-ul care se ocupa 
de comanda o expediaza si trece la urmatoarea comanda.
