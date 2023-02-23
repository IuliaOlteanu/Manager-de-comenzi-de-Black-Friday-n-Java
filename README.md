# Command Black Friday Manager

The goal is to process orders in parallel, respectively to process each individual product (even within the same order) in parallel. Thus, at any moment in time an order can have a part of products shipped, but only when all the products within it are sent can it be said that the order is shipped.

**Input Data**

The input files are the following:
\
➢ orders.txt (lines following the pattern id_command, no_products) 
\
➢ order_products.txt (lines following the pattern id_command, no_products) 

**Output Data**

The output files are the following:
\
➢ orders_out.txt (lines following the pattern id_command, no_products, status) 
\
➢ order_products_out.txt (lines following the pattern id_command, no_products, status)  

**Implementation**

To process the commands, P threads are started from the beginning that will read each of the commands file, each starting from the line corresponding to its ID and jumping from P to P lines, to ensure that each thread reads another group of lines. Then, each thread will start another thread that will search for one product from given command, holding in an AtomicInteger shared between all threads the number of active threads that looks for products, making sure it never exceeds P. Threads looking for products will read the file of products and will stop on the product corresponding to the order it is dealing with and with the number given by its ID. Once all of these product-seeking threads have finished and shipped the products, the handling thread of the order ships it and moves on to the next order.
