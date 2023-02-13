import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
	public static void main(String[] args) {
		// initializare variabile, thread-uri si un atomicinteger care va numara cate thread-uri care se ocupa de
		// produse sunt active la un moment de timp, cat si 2 variabile logice pentru a sti care este primul thread
		// care va deschide fisierele de citire, urmatoarele deschizandu-le in modul "append"
		// writeLock este pentru a ne asigura ca scrierea se face sincronizat
		int P = Integer.parseInt(args[1]);
		String path = args[0];
		Thread[] orderWorkers = new Thread[P];
		AtomicInteger noOfProductWorkers = new AtomicInteger(0);
		AtomicBoolean firstOrderWorker = new AtomicBoolean(true);
		AtomicBoolean firstProductWorker = new AtomicBoolean(true);
		try {
			// initializam bufferele de scriere partajate intre toate thread-urile, pentru optimizare
			BufferedWriter ordersWrite = new BufferedWriter(new FileWriter("./orders_out.txt"));
			BufferedWriter productsWrite = new BufferedWriter(new FileWriter("./order_products_out.txt"));
			// pornim cele P thread-uri de parsare a comenzilor
			for (int i = 0; i < P; i++) {
				orderWorkers[i] = new OrderWorker(path, P, noOfProductWorkers, i, firstOrderWorker, firstProductWorker, ordersWrite, productsWrite);
				orderWorkers[i].start();
			}

			for (int i = 0; i < P; i++) {
				try {
					orderWorkers[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ordersWrite.close();
			productsWrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class OrderWorker extends Thread {
	private String path;
	private int P;
	private AtomicInteger noOfProductWorkers;
	private int id;
	private AtomicBoolean firstOrderWorker, firstProductWorker;
	private BufferedWriter ordersWrite, productsWrite;

	public OrderWorker(String _path, int _P, AtomicInteger _noOfProductWorkers, int _id, AtomicBoolean _firstOrderWorker, AtomicBoolean _firstProductWorker, BufferedWriter _ordersWrite, BufferedWriter _productsWrite) {
		path = _path;
		P = _P;
		noOfProductWorkers = _noOfProductWorkers;
		id = _id;
		firstOrderWorker = _firstOrderWorker;
		firstProductWorker = _firstProductWorker;
		ordersWrite = _ordersWrite;
		productsWrite = _productsWrite;
	}

	public void run() {
		try {
			// se deschide fisierul de comenzi
			BufferedReader orders = new BufferedReader(new FileReader(path + "/orders.txt"));
			String order;
			// se porneste de la comanda de pe linia id, unde id este id-ul thread-ului, iar apoi
			// se va citi din P in P linii, astfel fiecare thread se ocupa de portiunea sa de linii
			for (int i = 0; i < id && (orders.readLine() != null); i++);
			order = orders.readLine();
			while (order != null) {
				StringTokenizer orderParser = new StringTokenizer(order, ",\n");
				String order_ID = orderParser.nextToken();
				int no_products = Integer.parseInt(orderParser.nextToken());
				// se ignora comenzile cu 0 produse
				if (no_products == 0) {
					for (int i = 0; i < P && ((order = orders.readLine()) != null); i++);
					continue;
				}
				Thread[] productWorkers = new Thread[no_products];
				// pentru fiecare produs se porneste un thread care il va cauta
				for (int i = 0; i < no_products; i++) {
					while (noOfProductWorkers.get() == P) {
						// se asteapta un loc liber in cele P thread-uri
					}
					noOfProductWorkers.incrementAndGet();
					productWorkers[i] = new ProductWorker(noOfProductWorkers, i, order_ID, path, firstProductWorker, productsWrite);
					productWorkers[i].start();
				}
				for (int i = 0; i < no_products; i++) {
					productWorkers[i].join();
				}
				// dupa ce comanda e procesata se expediaza
				ordersWrite.write(order + ",shipped\n");
				for (int i = 0; i < P && ((order = orders.readLine()) != null); i++);
			}
			orders.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class ProductWorker extends Thread {
	private AtomicInteger noOfProductWorkers;
	private int productNumber;
	private String order_ID;
	private String path;
	private AtomicBoolean firstProductWorker;
	private BufferedWriter productsWrite;

	public ProductWorker(AtomicInteger _noOfProductWorkers, int _productNumber, String _order_ID, String _path, AtomicBoolean _firstProductWorker, BufferedWriter _productsWrite) {
		noOfProductWorkers = _noOfProductWorkers;
		productNumber = _productNumber;
		order_ID = _order_ID;
		path = _path;
		firstProductWorker = _firstProductWorker;
		productsWrite = _productsWrite;
	}

	public void run() {
		try {
			// se deschide fisierul de produse si se citesc produsele care corespund comenzii cu ID-ul
			// dat ca parametru pana se ajunge la produsul cautat de thread-ul curent (reprezentat de productNumber)
			BufferedReader products = new BufferedReader(new FileReader(path + "/order_products.txt"));
			int currProduct = 0;
			String product = products.readLine();
			StringTokenizer productParser = new StringTokenizer(product, ",\n");
			String order_ID_parsed = productParser.nextToken();
			if (order_ID_parsed.equals(order_ID)) {
				currProduct++;
			}
			while (currProduct != productNumber + 1) {
				product = products.readLine();
				productParser = new StringTokenizer(product, ",\n");
				order_ID_parsed = productParser.nextToken();
				if (order_ID_parsed.equals(order_ID)) {
					currProduct++;
				}
			}
			productsWrite.write(product + ",shipped\n");
			products.close();
			noOfProductWorkers.decrementAndGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}