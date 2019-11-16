Lab 3 - Banker
==========================================
**Author: Cary Wu**
<br>
*CSCI-UA 202: Operating Systems Fall 2019*
<br>
<br>


The Banker.java program is written in Java. The program simulates two resource allocation algorithms: an optimistic resource allocator and Dijkstra's Banker's algorithm.  <br>
The optimistic resource manager is simple: Satisfy a request if possible, if not make the task wait; when a release occurs, try to satisfy pending requests in a FIFO manner. <br>
<br>
Input: My program takes one command line argument, the name of the file containing the input (a .txt file). <br>
Output: for each task, the time taken, the waiting time, and the percentage of time spent waiting. It also prints the total time for all tasks, the total waiting time, and the overall percentage of
time spent waiting.
<br>

The following instructions are based on Linux Terminal.

## To compile the program: 
```
javac Banker.java
```

## To run the program:

### Input is in the form of a <u>text file</u>: 
Type in: 
```
java Banker inputFileName.txt
```


