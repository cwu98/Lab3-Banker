import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Description: The program simulates two resource allocation algorithms 
 * an optimistic resource manager and Dijkstra's Banker's algorithm
 *
 * @author: Cary Wu
 */
public class Banker {
    static int numOfTasks;
    static int numOfResources;
    static String abortedMsgs = "";
    //2 separate copies of resources list and tasks list with different references
    static ArrayList<Integer> resourcesFIFO = new ArrayList<Integer>(); //value at index i corresponds to number of units there are for resource type i+1
    static ArrayList<Integer> resourcesBanker = new ArrayList<Integer>();
    static ArrayList<Task> tasksFIFO = new ArrayList<Task>();
    static ArrayList<Task> tasksBanker = new ArrayList<Task>();


    /*
     *Each Activity is implemented as an Activity object
     */
    public class Activity {
	/* attributes */
	private String name;
	private int resourceType;
	private int units;

	/* getters */
	private String getName(){
	    return this.name;
	}
	private int getResourceType(){
	    return this.resourceType;
	}
	private int getUnits(){
	    return this.units;
	}

	/* Class constructor */
	public Activity(String n, int r, int u){
	    this.name = n;
	    this.resourceType = r;
	    this.units = u;
	}
	/* Empty Class constructor */
	public Activity(){
	}
    }

    /* 
     * Each Task is implemented as a Task object
     */
    public class Task implements Comparable<Task> {
	/* attributes */
	private int taskNum;
       	private int waitTime=0;
	private int totalTime=0;
	private Activity blockedActivity = new Activity();
	private boolean aborted = false;
	private LinkedList<Activity> activitiesList = new LinkedList<Activity>(); //all actions are initially put into a queue
	private ArrayList<Integer> allocatedR; //allocated resources
	private ArrayList<Integer> claims; //the task's initial claims

	/* getters, setters, other methods */
	public int getTaskNum(){
	    return this.taskNum;
	}
	public int getWaitTime(){
	    return this.waitTime;
	}
	public int getTotalTime(){
	    return this.totalTime;
	}
	public void incrementWait(){
	    this.waitTime++;
	}
	public void setTotalTime(int t){
	    this.totalTime=t;
	}
	public LinkedList<Activity> getActivitiesList(){
	    return this.activitiesList;
	}
	public Activity getNextActivity(){
	    Activity activity = this.activitiesList.pollFirst();
	    return activity;
	}
	public Activity viewNextActivity(){
	    return this.activitiesList.get(0);
	}
	public void addActivity(Activity a){
	    this.activitiesList.add(a);
	}
	public void setBlockedActivity(Activity a){
	    this.blockedActivity = a;
	}
	public Activity getBlockedActivity(){
	    return this.blockedActivity;
	}
	public ArrayList<Integer> getClaims(){
	    return this.claims;
	}
	public ArrayList<Integer> getAllocatedR(){
	    return this.allocatedR;
	}
	public void abort(){ 
	    this.aborted = true;
	    //empty the tasks allocated resources
	    if(this.allocatedR.size()<0){
		for(int i=0;i<allocatedR.size();i++){
		    this.allocatedR.set(i,0);
		}
	    }
	}
	public boolean isAborted(){
	    return this.aborted;
	}
	public boolean terminate(){ //checks if next action is to terminate
	    if(this.activitiesList.getFirst().getName().equals("terminate")){
	      return true;
	    }
	    else{
		return false;
	    }
	}
	public void addAllocatedR(int resourceType, int amt){
	    this.allocatedR.set(resourceType-1, this.allocatedR.get(resourceType-1)+amt);
	}
	public void removeAllocatedR(int resourceType, int amt){
	    this.allocatedR.set(resourceType-1, this.allocatedR.get(resourceType-1)-amt);
	}
	public void setClaim(int resourceType, int amt){
	    this.claims.set(resourceType-1, amt);
	}
	/* Class contrsuctor */
	public Task(int taskNumber){
	    this.taskNum = taskNumber;
	    this.allocatedR = new ArrayList<Integer>(Collections.nCopies(numOfResources,0));
	    this.claims = new ArrayList<Integer>(Collections.nCopies(numOfResources,0));
	}
	/* Class constructor for deep cloning (creating a copy of the same object but with a 
	 * different reference)
	 */
	public Task(Task t){
	    this.taskNum = t.taskNum;
	    this.waitTime = t.waitTime;
	    this.totalTime = t.totalTime;
	    this.aborted = t.aborted;
	    this.blockedActivity = t.blockedActivity;
	    this.activitiesList = t.activitiesList;
	    this.allocatedR = t.allocatedR; 
	    this.claims = t.claims;
	}
	
	/* give info about the Task */
	public String toString() {
	    String str = "Task " + this.taskNum + "\n";
	    if (this.aborted == true){
		return str + " has been aborted";
	    }
	    for (Activity a : this.activitiesList) {
		str += a.getName() + " " + a.getResourceType() + " " + a.getUnits() + "\n";
	    }
	    return str;
	}
	
	/* method for sorting by Task number */
	public int compareTo(Task t){
	    return (this.taskNum - t.taskNum);
	}

       
    }//end_Task_Class
    
    
    /*
     *Read input file that has the input format of what's described in the spec
     *@param: name of file 
     *@return: list of task objects
     */
    public void readInputFile(String filename) {
	
	try{
	     File file = new File(filename);
	     Scanner sc = new Scanner(file);
	     int t = sc.nextInt(); //number of tasks
	     int r = sc.nextInt(); //number of types of resources
	     numOfTasks = t;
	     numOfResources = r;
	     for (int i=0; i<r; i++){
		 int quantity = sc.nextInt();
		 resourcesFIFO.add(i,quantity);
		 resourcesBanker.add(i,quantity);
	     }
	     //System.out.println(t+" "+ r);
	     while(sc.hasNext()){
		 String activity = sc.next();
		 int taskNumber = sc.nextInt();
		 int resourceType = sc.nextInt();
		 int units = sc.nextInt();
		 Activity newActivity1 = new Activity(activity, resourceType, units);
		 Activity newActivity2 = new Activity(activity, resourceType, units);
		 //System.out.println(activity+" "+ taskNumber + " " + resourceType + " " + units);
		 if (activity.equals("initiate")){ //create new Task object
		     if(tasksFIFO.size() == (taskNumber-1)){
			 Task newTask1 = new Task(taskNumber);
			 Task newTask2 = new Task(taskNumber);
			 tasksFIFO.add(newTask1);
			 tasksBanker.add(newTask2);
		     }
		 }
		 int index = taskNumber - 1;
		 if (index >= 0){
		     tasksFIFO.get(index).addActivity(newActivity1);
		     tasksBanker.get(index).addActivity(newActivity2);
		 }
		 else {
		     System.out.println("Error: invalid task number");
		 }
	     
	     }
      	}//end_try_block
	
	catch (FileNotFoundException e){
	    System.out.println("FileNotFound error");
	}//end_catch_block
	
    }//end_readInputFile_method
	
    
    /*
     * Simulation of optimistic resource manager using FIFO algorithm
     * The method does resource allocation by satisfying a request if 
     * possible and if not, the task is blocked. When a release occurs,
     * the resource manager tries to satisfy pending requests. After 
     * satisfying the request of the blocked task, the tasks is enteresd back into 
     * the queue of running tasks. It checks for deadlock and fixes by repeatedly aborting the task
     * with lowest task number and freeing its resources until no deadlock remains
     *
     *@param: list of task objects
     *@param: list of quantity of resources
     *@return: list of terminated task objects with updated wait times
     */
    public ArrayList<Task> FIFO (ArrayList<Task> listOfTasks, ArrayList<Integer> resources) {
	
	ArrayList<Integer> resourcesMap = resources;
	ArrayList<Task> unblockedTasks = listOfTasks;
	ArrayList<Task> blockedTasks = new ArrayList<Task>();
	ArrayList<Task> completedTasks = new ArrayList<Task>();
	LinkedHashMap<Task,Integer> beingComputed = new LinkedHashMap<Task,Integer>();
	int cycle = 0;
	ArrayList<Task> terminatedTasks = new ArrayList<Task>(); //tasks that released resources and terminated
	ArrayList<Integer> released = new ArrayList<Integer>(Collections.nCopies(numOfResources, 0)); //initialize all values to 0
	boolean keepGoing = true;
	
	while (keepGoing) {
	    int taskNum;
	    ArrayList<Task> temp = new ArrayList<Task>(); //list of tasks to add back to running list of tasks at the end of cycle
	    Iterator<Task> j = blockedTasks.iterator();
	    Iterator<Task> k = completedTasks.iterator();
	    Iterator iter = beingComputed.entrySet().iterator();
	    Activity blockedActivity = new Activity();
	    Activity action = new Activity();
	    String activityType;
	    int resourceType, numOfUnits;
	    int avail=0;
	     
	    //System.out.println("During " + cycle + "-"+ (cycle+1));
	    cycle++;

	    //clear list of released resources
	    for(int i=0; i<released.size(); i++){
		released.set(i,0);
	    }

	    //remove tasks that released and terminated from queue of running tasks
	    for(Task term : terminatedTasks){
		unblockedTasks.remove(term);
	    }

	    //update delayed number of cycles for tasks being computed
	    while(iter.hasNext()){
		HashMap.Entry mapElement = (HashMap.Entry)iter.next();
		Task task = (Task)mapElement.getKey();
		//System.out.println("Task "+task.getTaskNum()+" delayed "+beingComputed.get(task));
		beingComputed.replace(task, beingComputed.get(task)-1); //decrement cycle
		if(beingComputed.get(task) == 0){ //end of compute 
		    iter.remove();
		    if(task.terminate()){ //if next activity is terminate, terminate.
			//System.out.println("\tTask "+task.getTaskNum()+" terminates at cycle "+cycle);
			task.setTotalTime(cycle);
			completedTasks.add(task);
		    }
		    else{ //add back into queue of running tasks
			temp.add(task);
		    }
		}
	    }
	    
	    //try to satisfy blocked tasks 
	    if (!blockedTasks.isEmpty()){
		//System.out.println("\tFirst check blocked tasks:");
		    while(j.hasNext()){
      			Task blocked = j.next();
			//increment wait times for blocked tasks
			blocked.incrementWait();
			//System.out.print("\t");
			taskNum = blocked.getTaskNum();
			blockedActivity = blocked.getBlockedActivity();
			activityType = blockedActivity.getName();
			resourceType = blockedActivity.getResourceType();
			numOfUnits = blockedActivity.getUnits();
			avail = resourcesMap.get(resourceType-1);
     			if(activityType.equals("request")) { //try to grant request
			    if(numOfUnits <= avail){//grant request
				avail = avail - numOfUnits;
				resourcesMap.set(resourceType-1, avail);
				blocked.addAllocatedR(resourceType, numOfUnits);
				//System.out.print("\tTask "+taskNum+" completes its request ");
				//System.out.printf("(resource[%d]: requested = %d, remaining = %d)\n", resourceType, numOfUnits, avail);
				j.remove();
				temp.add(blocked); 
			    }
			
			    else{
				//System.out.println("\tTask "+taskNum+" request still cannot be granted.");
			    }		
			}
			    
		    }
	    }
	    Iterator<Task> i = unblockedTasks.iterator();
	    while(i.hasNext()){
		//System.out.print("\t");
		Task t = i.next();
		taskNum = t.getTaskNum();
      		action = t.getNextActivity();
	        resourceType = action.getResourceType();
		numOfUnits = action.getUnits();
		if (action.getName().equals("compute")){
		    avail = 0;
		}
		else{
		    avail = resourcesMap.get(resourceType-1); //num of avail units of this resource
		}
		switch (action.getName()) {
		case "initiate": //FIFO algorithm ignores initiate avtivity
		    //System.out.println("Task "+taskNum+ " does initialization");
		    break;

		case "request":

      		    if (numOfUnits <= avail) { //grant request
			avail = avail - numOfUnits;
			resourcesMap.set(resourceType-1, avail);
			t.addAllocatedR(resourceType, numOfUnits);
			//System.out.print("Task "+taskNum+" completes its request ");
			//System.out.printf("(resource[%d]: requested = %d, remaining = %d)\n",resourceType,numOfUnits, avail); 
		    }
		    else { //block task
			blockedTasks.add(t);
			i.remove();
			t.setBlockedActivity(action);
			//System.out.print("Task "+taskNum+" waiting ");
			//System.out.printf("(resource[%d]: requested = %d, available = %d)\n",resourceType,numOfUnits,avail);
			
		    }
		    break;
		    
		case "release": //free the task's resources 
		    avail = avail+numOfUnits;
		    released.set(resourceType-1, released.get(resourceType-1)+numOfUnits);
		    t.removeAllocatedR(resourceType, numOfUnits);
		    //System.out.print("Task "+taskNum+" completes its release ");
		    //System.out.printf("(resource[%d]: released = %d, available next cycle = %d)\n", resourceType, numOfUnits, avail); 
		    
		    break;

		case "compute": //task is delayed for certain num of cycles
		    int numCycles = resourceType; //this value for "compute" activity corresponds to num-of-cycles
		    if(numCycles > 1){
			beingComputed.put(t,numCycles-1); //reduce number of cycles by 1 to account for this cycle
		    }
		    else if(numCycles == 1){
			temp.add(t);
		    }
		    i.remove();
		    //System.out.println("Task "+taskNum+" delayed "+numCycles+".");
		    break;
		}//end_switch
	    
		if(t.terminate()){ //the next activity is to terminate which doesn't take a cycle so terminate 
		    if(beingComputed.containsKey(t)){ //accounts for if tasks that are being computed but will terminate after, ignore them
			continue;
		    }
		    //System.out.println("\tTask "+taskNum+" terminates at cycle "+cycle);
		    t.setTotalTime(cycle); //set finish time 
			if(unblockedTasks.contains(t)){
			    completedTasks.add(t);
			    terminatedTasks.add(t);
			}
			else if (temp.contains(t)){ //computed for 1 cycle terminates
			    temp.remove(t);
			    completedTasks.add(t);
			}
		}
		
	    }
	    
	    //add tasks that were unblocked this cycle back into unblocked list
	    for(Task p : temp){
		unblockedTasks.add(p);
	    }
	    temp.clear();

	    
	    //detect deadlock and try to fix it using algorithm described in spec
	    if((completedTasks.size()!= numOfTasks) && (unblockedTasks.isEmpty()) && beingComputed.isEmpty()){
		boolean deadlocked = true;
		//get blocked task with lowest task num
		while(deadlocked){
		    Task minPriority = blockedTasks.get(0); //task with lowest task number in blocked list
		    for(Task t : blockedTasks){
			if(t.getTaskNum() < minPriority.getTaskNum()){
			    minPriority = t;
			}
		   }
		    
		    //free up all its resources and abort task
		    for(int w = 0; w < minPriority.getAllocatedR().size(); w++){
			released.set(w, released.get(w)+minPriority.getAllocatedR().get(w));
		    }
		    minPriority.abort();
		    //System.out.println("Optimistic Resoure Manager aborts Task "+minPriority.getTaskNum() +" because a deadlock was detected according to spec. Its resources are available next cycle ("+cycle+"-"+(cycle+1)+")");
		    completedTasks.add(minPriority);
		    blockedTasks.remove(minPriority);

		    //check if deadlock remains after removing the task
		    Task next = blockedTasks.get(0);
		    Activity blockedAct = next.getBlockedActivity();
		    resourceType = blockedAct.getResourceType();
		    numOfUnits=  blockedAct.getUnits(); 
		    if(blockedAct.getName().equals("request")){
			//check if there are enough units to grant request with the recently aborted tasks' released units taken into account
			avail = resourcesMap.get(resourceType-1) + released.get(resourceType-1);
			if(numOfUnits <= avail){
			    
			    deadlocked = false; //no longer deadlocked
			}
			else{
			    deadlocked = true;
			}
		    }	
		
		}//end_while(deadlocked)
	    }//end_deadlock_detection
   
	    //update available resources from tasks that released for the next
	    for (int r=0; r<numOfResources; r++){
      
		int newVal = resourcesMap.get(r) + released.get(r);
		resourcesMap.set(r, newVal);
	    }
	    //System.out.println("size of completed tasks: "+completedTasks.size());
	    if(completedTasks.size()==numOfTasks){ //tell program to stop
		keepGoing=false;
	    }

	    
	}//end_while

	//sort list of completed tasks
	Collections.sort(completedTasks);
	return completedTasks;
    }
    /*
     *Detects if a state is safe, used for Banker's algorithm
     *The method looks for a task whose claims can be satisfied (claim - needs < what's avail). It no such task exists, system will deadlock.
     *Assume this task finishes and releases its resources. Repeat these steps until either all tasks terminate (the initial state is safe) or
     *no task is left whose resources can be met (unsafe).
     *
     *@param: tasksList: current list of tasks
     *@param: resources: list of number of currently available resources 
     *return: true if state is safe, false otherwise
     */
    public boolean isSafe(ArrayList<Task> tasksList, ArrayList<Integer> resources){
        ArrayList<Task> tasks = new ArrayList<Task>();
	ArrayList<Integer> availR = new ArrayList<Integer>();
	//make copies of lists having different references from original
	for(Task task : tasksList){
	    tasks.add(new Task(task));
	}
	for(Integer i : resources){
	    availR.add(i);
	}
	//matrix of additional resources each task needs to fulfill its claims
	int[][] needMatrix = new int[tasks.size()][availR.size()];
	/*
	System.out.println("Need Matrix:");
	*/
	for(int n = 0; n <tasks.size(); n++){
	    for(int m=0; m < availR.size(); m++){
		int need = tasks.get(n).getClaims().get(m) - tasks.get(n).getAllocatedR().get(m);
		needMatrix[n][m] = need;
	    }
	}
        
	boolean[] done = new boolean[numOfTasks]; //all values by default is false
	int[] possibleSeq = new int[tasks.size()];
	int foo = 0;
	//keepgoing until all tasks are either done or system is unsafe
	while(foo<tasks.size()){
	    //find a task not done and whose requests can be granted with curr available resources
	    boolean check = false;
	    for(int t = 0; t < tasks.size(); t++){
		if(!done[t]){
		    int r = 0;
		    for(r = 0; r < availR.size(); r++){
			if(needMatrix[t][r] > availR.get(r)){
			    break;
			}
		    }
		    //all needs of task can be granted
		    if(r==availR.size()){
			for(int x=0; x<availR.size(); x++){
			    availR.set(x, availR.get(x)+tasks.get(t).getAllocatedR().get(x));
			}
			possibleSeq[foo++] = tasks.get(t).getTaskNum();
			done[t] = true;
			check = true;
		    }
		}
	    }
	    if(!check){ //there does not exist a task that keeps the state safe
		return false;
	    }
	}
	
	//done matrix contains all true values, system is in safe state
	return true;
    }//end_isSafe

    
    /*
     * Simulation of Banker's Algorithm
     * Description: Banker's algorithm does resource allocation by trying to avoid deadlocks by checking if granting
     * a request is an unsafe state and will eventually lead to deadlock. If a request is unsafe, it is blocked. 
     * At the beginning of each cycle, Banker will first check if it can grant pending requests and if it can, it grants the request
     * and adds the task back to the list of running tasks. Banker uses the isSafe method to check if a state is safe.
     *
     *@param: list of task objects
     *@param: list of quantity of resources
     *@return: list of terminated task objects with updated wait times
     */
     
    public ArrayList<Task> Banker(ArrayList<Task> listOfTasks, ArrayList<Integer> resources) {
	int cycle = 0;
	ArrayList<Task> unblockedTasks = listOfTasks;
	ArrayList<Task> blockedTasks = new ArrayList<Task>();
	ArrayList<Task> completedTasks = new ArrayList<Task>();
	ArrayList<Integer> tempRList = new ArrayList<Integer>();
	LinkedHashMap<Task,Integer> beingComputed = new LinkedHashMap<Task,Integer>();
	ArrayList<Integer> resourcesMap = resources;  
	ArrayList<Integer> released = new ArrayList<Integer>(Collections.nCopies(numOfResources,0));
	ArrayList<Task> terminatedTasks = new ArrayList<Task>(); //tasks that released resources and terminated
	boolean keepGoing = true;
        
	while (keepGoing) {
	    int taskNum;
	    ArrayList<Task> temp = new ArrayList<Task>();
	    Iterator<Task> j = blockedTasks.iterator();
	    Iterator<Task> k = completedTasks.iterator();
	    Iterator iter = beingComputed.entrySet().iterator();
	    Activity blockedActivity = new Activity();
	    Activity action = new Activity();
	    String activityType;
	    int resourceType, numOfUnits;
	    int avail=0;
	    boolean safe; 
	    //System.out.println("During " + cycle + "-"+ (cycle+1));
	    cycle++;
	    
	    //clear list of released resources
	    for(int i=0; i<released.size(); i++){
		released.set(i,0);
	    }
	    //remove tasks that released and terminated
	    for(Task term : terminatedTasks){
		unblockedTasks.remove(term);
	    }
	    
	    while(iter.hasNext()){
		HashMap.Entry mapElement = (HashMap.Entry)iter.next();
		Task task = (Task)mapElement.getKey();
		//System.out.println("Task "+task.getTaskNum()+" delayed "+beingComputed.get(task));
		beingComputed.replace(task, beingComputed.get(task)-1); //decrement cycle
	        if(beingComputed.get(task) == 0){
		    iter.remove();
		    if(task.terminate()){
			//System.out.println("\tTask "+task.getTaskNum()+" terminates at cycle "+cycle);
			task.setTotalTime(cycle);
			completedTasks.add(task);
		    }
		    else{
			temp.add(task);
		    }
		}
	    }
	    
	    //try to satisfy blocked tasks first
	    if (!blockedTasks.isEmpty()){
		//System.out.println("\tFirst check blocked tasks:");
		    while(j.hasNext()){
     			Task blocked = j.next();
			blocked.incrementWait();
			//System.out.print("\t");
			taskNum = blocked.getTaskNum();
			blockedActivity = blocked.getBlockedActivity();
			activityType = blockedActivity.getName();
			resourceType = blockedActivity.getResourceType();
			numOfUnits = blockedActivity.getUnits();
			avail = resourcesMap.get(resourceType-1);
			tempRList.clear();
			for(Integer copy : resourcesMap){
			    
			    tempRList.add(copy);
			}
			blocked.addAllocatedR(resourceType,numOfUnits);
			tempRList.set(resourceType-1, avail-numOfUnits);
		       
			
			//temporarily add blocked task to unblockedTasks and check if granting the request is safe
			unblockedTasks.add(blocked);
        
			safe = isSafe(unblockedTasks, tempRList);
			blocked.removeAllocatedR(resourceType, numOfUnits);
			unblockedTasks.remove(blocked);
		       
			if(safe){
			    if(activityType.equals("request")) {
				if(numOfUnits <= avail){//grant request
				    avail = avail - numOfUnits;
				    resourcesMap.set(resourceType-1, avail);
				    blocked.addAllocatedR(resourceType, numOfUnits);
				    //System.out.print("\tTask "+taskNum+" completes its request ");
				    //System.out.printf("(resource[%d]: requested = %d, remaining = %d)\n", resourceType, numOfUnits, avail);
				    j.remove();
				    temp.add(blocked);
				}
				else{
				    //System.out.println("\tTask "+taskNum+" request still cannot be granted.");
				}		
			    }
			    
			}
			else{
			    //blocked task stays blocked
			    //System.out.println("Task "+taskNum+ " request cannot be granted (not safe). Task "+taskNum+" is blocked");
			}
		    }
	    }
	    Iterator<Task> i = unblockedTasks.iterator();
	    while(i.hasNext()){
		//System.out.print("\t");
		Task t = i.next();
		taskNum = t.getTaskNum();
      		action = t.getNextActivity();
	        resourceType = action.getResourceType();
		numOfUnits = action.getUnits();
		if(action.getName().equals("compute")){
		    avail = 0;
		}
		else{
		    avail = resourcesMap.get(resourceType-1); //num of avail units of this resource
		}
		tempRList.clear();
		switch (action.getName()) {
		case "initiate":
		    //error check
		    if(numOfUnits > resourcesMap.get(resourceType-1)){ //claim exceeds resources present: abort
			abortedMsgs += "Banker aborts ask "+taskNum+" before run begins: \n\tclaim for resource "+resourceType+ " ("+numOfUnits+") exceeds number of units present ("+avail+")\n";
			t.abort();
			i.remove();
			completedTasks.add(t);
		    }
		    else{
			t.setClaim(resourceType, numOfUnits);
			//System.out.println("Task "+taskNum+ " does initialization");
		    }
		    break;

		case "request":
		    //calculate total units (including what's already allocated) being requested by task
		    int totalRequests = t.getAllocatedR().get(resourceType-1) + numOfUnits;
		    if (totalRequests > t.getClaims().get(resourceType-1)){
			
			abortedMsgs += "During cycle "+(cycle-1)+"-"+(cycle)+" of Banker's Algorithm: \n\tTask "+taskNum+"'s request exceeds initial claim; aborted\n";
			//abort and release resources by adding to list of freed resources for this cycle
		        
			ArrayList<Integer> toBeReleased = t.getAllocatedR();
			for (int q=0; q < numOfResources; q++) {
			    released.set(q, released.get(q)+toBeReleased.get(q));
			}
			t.abort(); //abort method zeros allocated resources list for the task
			completedTasks.add(t);
			i.remove();
					     
		    }
		    else{
			for(Integer copy : resourcesMap){
			   tempRList.add(copy);
			}
			t.addAllocatedR(resourceType,numOfUnits);
			tempRList.set(resourceType-1, avail-numOfUnits);
			safe = isSafe(unblockedTasks, tempRList);
			t.removeAllocatedR(resourceType, numOfUnits);
			if(safe){//safe, grant request
			    if (numOfUnits <= avail) {
				avail = avail - numOfUnits;
				resourcesMap.set(resourceType-1, avail); 
				t.addAllocatedR(resourceType, numOfUnits);
				//System.out.print("Task "+taskNum+" completes its request ");
				//System.out.printf("(resource[%d]: requested = %d, remaining = %d)\n",resourceType,numOfUnits, avail); 
			    }
			}
			else {
			    blockedTasks.add(t);
			    i.remove();
			    t.setBlockedActivity(action);
			    //System.out.print("Task "+taskNum+" is blocked (unsafe) ");
			    //System.out.printf("(resource[%d]: requested = %d, available = %d)\n",resourceType,numOfUnits,avail);
			
			}
		    }
		    break;
		    
		case "release":
		    avail = avail+numOfUnits;
		    released.set(resourceType-1, released.get(resourceType-1)+numOfUnits);
		    t.removeAllocatedR(resourceType, numOfUnits);
		    //System.out.print("Task "+taskNum+" completes its release ");
		    //System.out.printf("(resource[%d]: released = %d, available next cycle = %d)\n", resourceType, numOfUnits, avail); 
		    
		    break;

		case "compute":
		    int numCycles = resourceType; //this value for "compute" activity corresponds to num-of-cycles
		    if(numCycles > 1){
			beingComputed.put(t,numCycles-1); //reduce num of cycles by 1 to account for this current cycle
		    }
		    else if (numCycles==1) {
			temp.add(t);
		    }
		    i.remove();
		    //System.out.println("Task "+taskNum+" delayed "+numCycles+".");
		    break;
		}//end_switch
	    
		if(t.terminate()){
		    if(beingComputed.containsKey(t)){
			continue;
		    }
		    //System.out.println("\tTask "+taskNum+" terminates at cycle "+cycle);
		    t.setTotalTime(cycle);
			if(unblockedTasks.contains(t)){
			    completedTasks.add(t);
			    terminatedTasks.add(t);
			}
			else if (temp.contains(t)){ //computed for 1 cycle and terminate
			    temp.remove(t);
			    completedTasks.add(t);
			}
		   		
		}
		
	    }
	    //update available resources from tasks that released for the next
	    for (int r=0; r<numOfResources; r++){
		int newVal = resourcesMap.get(r) + released.get(r);
		resourcesMap.set(r, newVal);
	    }
	  
	    
	    for(Task p : temp){
		unblockedTasks.add(p);
	    }
	    temp.clear();
	    
	    if(completedTasks.size()==numOfTasks){
		keepGoing=false;
	    }
	    
	}//end_while
	Collections.sort(completedTasks);
	return completedTasks;
    }//end_Banker_method

    public static void main(String[] args) {
	Banker b = new Banker();
	if (args.length > 0){
	    b.readInputFile(args[0]);
	 
	    ArrayList<Task> completedTasksFIFO = b.FIFO(tasksFIFO, resourcesFIFO);    
	    ArrayList<Task> completedTasksBanker = b.Banker(tasksBanker, resourcesBanker);
	    System.out.println(abortedMsgs);
	    System.out.format("\t\t%s","  FIFO");
	    System.out.format("\t\t\t\t\t%s","BANKER'S");
	    System.out.println();
	    int ttFIFO=0; int twFIFO=0; int ttBanker=0; int twBanker = 0; //total times and total wait times
	    for (int i = 0; i<numOfTasks; i++){
		Task taskFIFO = completedTasksFIFO.get(i);
		Task taskBanker = completedTasksBanker.get(i);
		if(taskFIFO.isAborted()){
		    System.out.printf("\t%-6s      %-14s", ("Task "+Integer.toString(i+1)), "aborted");
		}
		else{
		    String str = "Task " + Integer.toString(i+1);
		    float wait = 100*((float)taskFIFO.getWaitTime()/(float)taskFIFO.getTotalTime());
		    System.out.printf("\t%-6s      %-3d   %-3d   %.0f%s",str, taskFIFO.getTotalTime(), taskFIFO.getWaitTime(),wait,"%"); 
		    ttFIFO += taskFIFO.getTotalTime();
		    twFIFO += taskFIFO.getWaitTime();
		}
		if(taskBanker.isAborted()){
		    System.out.printf("\t\t%-6s      %-14s",("Task "+Integer.toString(i+1)), "aborted");
		    System.out.println();
		}
		else{
		    float wait = 100*((float)taskBanker.getWaitTime()/(float)taskBanker.getTotalTime());
		    System.out.printf("\t\t%-6s      %-3d   %-3d   %.0f%s",("Task "+Integer.toString(i+1)), taskBanker.getTotalTime(), taskBanker.getWaitTime(), wait, "%");
		    ttBanker += taskBanker.getTotalTime();
		    twBanker += taskBanker.getWaitTime();
		    System.out.println();
		}
	    }
	    float twpFIFO = 100*((float)twFIFO/(float)ttFIFO);
	    System.out.printf("\t%-6s      %-3d   %-3d   %.0f%s", "Total", ttFIFO, twFIFO, twpFIFO, "%");

	    float twpBanker = 100*((float)twBanker/(float)ttBanker);
	    System.out.printf("\t\t%-6s      %-3d   %-3d   %.0f%s", "Total", ttBanker, twBanker, twpBanker, "%");

	    System.out.println();
	
	}
	else {
	    System.out.println("Error: No commandline argument detected");
	}
    }//end_main

   
	    
}//end_Banker_class
