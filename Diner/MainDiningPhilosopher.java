import java.util.Scanner;
import java.util.Random;
import java.io.*;
import java.util.concurrent.Semaphore;
class RunnableClass implements Runnable{
    public int countThink;
    public int countEat;
    public int moreCounter;
    public int amountOfRepeat;
    public int threadCount;
    public int philosopher;
    public Semaphore[] chopsticks;
 
    //all of these are shared among the philosophers 
    public static Semaphore table;
    public static Semaphore chopnom = new Semaphore(1);    
    public static Semaphore frontDoor = new Semaphore(0);
    public static Semaphore backrooms = new Semaphore(0);
    public static int arrived = 0;
    public static int readyToLeave = 0;
    public static int mealsStarted = 0;
    public static int mealsEaten = 0;
 
    public RunnableClass(int threads, int mealsLeft, Semaphore[] chopstick, int philosopher){
        this.threadCount = threads;
        this.chopsticks = chopstick;
        this.amountOfRepeat = mealsLeft;
        this.philosopher = philosopher;
    }
    

    public void run() {
        Random j = new Random();
 
        // number of chopsticks and assigning the chopsticks based on the thread id/philosophers position
        int amountOfChopstick = chopsticks.length;
        int leftchopstick = philosopher;
        int rightchopstick = (philosopher + 1) % amountOfChopstick;
 
        try{
            
        
            System.out.println("Philosopher " + philosopher + " starting.");
            chopnom.acquire();
            
            // adds the count of the amount of philosophers who have arrived from their thread, then gives resource to frontDoor only after all 
            // philosophers have arrived
            arrived++;
            if (arrived == threadCount) {
                System.out.println("--All Philosophers have arrived.");
                frontDoor.release();
            }
            
            chopnom.release();
            
            // can only proceed if the semaphore front door is able to be activated (or has a resource)
            frontDoor.acquire();
            frontDoor.release();
 
            System.out.println("--Philosopher " + philosopher + " sits down at the table.");
 
            while (true) {
                
                // checks the condition of the table to begin seeing if philosophers are hungry
                table.acquire();
                
                // checks if a meal has begun being eaten yet (calls semaphor chompnom to indicate that meals have begun taking place)
                chopnom.acquire();
                boolean hungry = mealsStarted < amountOfRepeat;
                if (hungry) {
                    mealsStarted++;
                }
                chopnom.release();
                
                if (!hungry) {
                    table.release();
                    break;
                }
                boolean success = false;
                
                // aquiring the left chopstick
                if (chopsticks[leftchopstick].tryAcquire()) {
                    System.out.println("---Philosopher " + philosopher + "'s left chopstick IS available.");
                    }
                else {
                    System.out.println("---Philosopher " + philosopher + "'s left chopstick IS NOT available.");
                    chopsticks[leftchopstick].acquire();
                    System.out.println("---Philosopher " + philosopher + "'s left chopstick IS available.");
                    }
                    
                // aquiring the right chopstick
                if (chopsticks[rightchopstick].tryAcquire()) {
                    System.out.println("---Philosopher " + philosopher + "'s right chopstick IS available.");
                } 
                else {
                    System.out.println("---Philosopher " + philosopher + "'s right chopstick IS NOT available.");
                    chopsticks[rightchopstick].acquire();
                    System.out.println("---Philosopher " + philosopher + "'s right chopstick IS available.");
                    success = true;
                }
                
                
                // checks to make sure philosopher has both chopsticks
                System.out.println("----Philosopher " + philosopher + " grabs both chopsticks.");
                if (chopsticks[leftchopstick].tryAcquire()){
                    if (chopsticks[rightchopstick].tryAcquire()){
                        System.out.println("----Philosopher " + philosopher + " has two chopsticks.");
                    }
                }
 
                System.out.println("-----Philosopher " + philosopher + " is eating.");
                
                
                // random wait cycle for eating time
                countEat = j.nextInt(3, 6);
                moreCounter = 0;
                while (moreCounter != countEat) {
                    moreCounter++;
                }
 
                // checking number of meals eaten
                chopnom.acquire();
                mealsEaten++;
                System.out.println("Meals ate: " + mealsEaten);
                chopnom.release();
 
                System.out.println("------Philosopher " + philosopher + " is finished eating.");
                chopsticks[leftchopstick].release();
                System.out.println("-------Philosopher " + philosopher + " dropped his left chopstick.");
                chopsticks[rightchopstick].release();
                System.out.println("-------Philosopher " + philosopher + " dropped his right chopstick");
                table.release();
 
 
                // checking a random wait time for the philosopher to be thinking
                System.out.println("--------Philosopher " + philosopher + " is thinking.");
                countThink = j.nextInt(3, 6);
                moreCounter = 0;
                while (moreCounter != countThink) {
                    moreCounter++;
                }
            }
 
            //checks that the philosophers have eaten atleast once if possible and stores the ones that can in readytoLeae
            chopnom.acquire();
            readyToLeave++;
            
            //once all the philosophers have eaten their meal, provide a key/resource for the backrooms to be unlocked
            if (readyToLeave == threadCount) {
                System.out.println("---------All Philosophers have finished eating.");
                backrooms.release();
            }
            chopnom.release();
            
            //activates the backroom now that there is a resource available to continue the process
            backrooms.acquire();
            backrooms.release();
 
            System.out.println("----------Philosopher " + philosopher + " has left the table.");
        }
        catch (InterruptedException e){
            System.out.println("FAIL!!!");
            Thread.currentThread().interrupt();
        }
    }
}

public class MainDiningPhilosopher {
 
    public static void main(String[] args) {
        
        Scanner scan = new Scanner(System.in);
 
        System.out.println("How many philosophers should be created (integer from 1-10000):");
        int threadCount = scan.nextInt();
        
        //Makes sure threadcount is good number.
        if (threadCount <= 0 || threadCount > 10000) {
            System.out.println("How many philosophers should be created (integer from 1-10000):");
            threadCount = scan.nextInt();
        }
 
        System.out.println("How many meals? (1-10000):");
        int amountOfRepeat = scan.nextInt();
 
        if (amountOfRepeat <= 0 || amountOfRepeat > 10000) {
            System.out.println("How many meals?:");
            amountOfRepeat = scan.nextInt();
        }
 
        // Creating the chopsticks for the table
        Semaphore[] chopsticks = new Semaphore[threadCount];
        for (int i = 0; i < chopsticks.length; i++) {
            chopsticks[i] = new Semaphore(1);
        }
        
        RunnableClass.table = new Semaphore(threadCount - 1);
        Thread[] amountOfPhilosophers = new Thread[threadCount];
        
        // start timeer
        long start_time = System.nanoTime();
        // Calls the thread for each philosopher, along with the amount of meals to be eaten, chopsticks semaphore array list and the actual number of the
        // current philosopher (essentially an id)
        for (int i = 0; i < threadCount ; i++) {
 
            RunnableClass b = new RunnableClass(threadCount, amountOfRepeat, chopsticks, i);
            Thread philosophers = new Thread(b, "philosophers " + i + ": ");
            
            
            amountOfPhilosophers[i] = philosophers;
            
            // begin the philosopher thread
            philosophers.start();
        }
        for (int i = 0; i < threadCount; i++) {
            try {
                amountOfPhilosophers[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        //stop timer once the amount of philosophers has everything stored
        long end_time = System.nanoTime();
        System.out.printf("Runtime in milliseconds = ");
        System.out.println((end_time - start_time) / 1000000.0);     
        
    }
 
}
 