import java.util.concurrent.Semaphore;
import java.util.Scanner;

public class ReaderWriterProblem {
    static Semaphore mutex = new Semaphore(1, true); //Readers' Semaphore
    static Semaphore wrt = new Semaphore(1, true); //Writers' Semaphore
    static Semaphore batchDone = new Semaphore(0, true); //Marks when there are no more concurrent readers
    static Semaphore wDone = new Semaphore(0, true); //Marks when the writer is finished writing
    static int readCount; //For delaying the writers.
    static int finishedReaders; //Total # of readers done
    static int NofR; //# of concurrent readers

    static class Reader implements Runnable {
        @Override
        public void run() {
            try {
                mutex.acquire();
                readCount++;
                if (readCount == 1) {
                    wrt.acquire(); //Readers stop Writers
                }
                mutex.release();
                //Reader Start
                System.out.println("-" + Thread.currentThread().getName() + " began reading.");
                Thread.sleep(1111); //Reader Reads Readings
                System.out.println("--" + Thread.currentThread().getName() + " finished reading.");
                //KILL Reader
                mutex.acquire();
                readCount--;
                if (readCount == 0) {
                    wrt.release(); //Writers may write now
                }

                finishedReaders++;
                if (finishedReaders == NofR) { //Tell main there are no more concurrent readers
                    batchDone.release();
                }

                mutex.release();
            }
            catch (Exception e) {
                System.out.println("Something went wrong :(");
            }
        }
    }
    static class Writer implements Runnable {
        @Override
        public void run() {
            try {
                wrt.acquire();
                //Writer Start
                System.out.println("---" + Thread.currentThread().getName() + " began writing.");
                Thread.sleep(2222); //Writer Writes Writings
                System.out.println("----" + Thread.currentThread().getName() + " finished writing.");
                //KILL Writer
                wrt.release();

                wDone.release(); //Scrivere Finito
            }
            catch (Exception e) {
                System.out.println("Something went wrong :(");
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("Starting Readers-Writers Problem");
        Scanner myObj = new Scanner(System.in);

        System.out.print("How many reading threads should be created? (integer from 1-10000): ");
        int R = myObj.nextInt(); //Get Reader Amount
        System.out.print("How many writer threads should be created? (integer from 1-10000): ");
        int W = myObj.nextInt(); //Get Writer Amount
        System.out.print("How many readers should be allowed to read at once? ");
        int N = myObj.nextInt(); //Get # of concurrent Readers

        Thread[] readers = new Thread[R];
        Thread[] writers = new Thread[W];
        for (int i = 0; i < R; i++) {
            readers[i] = new Thread(new Reader(), "R" + (i + 1)); //Create readers
        }
        for (int i = 0; i < W; i++) {
            writers[i] = new Thread(new Writer(), "W" + (i + 1)); //Create writers
        }

        int RIndex = 0;
        int WIndex = 0;
        while (RIndex < R || WIndex < W) {
            if (RIndex < R) {//Process readers in groups of N
                NofR = Math.min(N, R - RIndex); //How many current readers

                mutex.acquireUninterruptibly();
                finishedReaders = 0; //Reset completed readers
                mutex.release();

                for (int i = 0; i < NofR; i++) {
                    readers[RIndex + i].start(); //Start group reading sesh
                }
                batchDone.acquireUninterruptibly(); //Wait for group to finish
                RIndex += NofR; //Move on to new group reading sesh
            }

            if (WIndex < W) { //Start writer
                writers[WIndex].start();
                wDone.acquireUninterruptibly(); //Wait for writer to write words
                WIndex++;
            }
        }
        System.out.println("All writers have finished.");
        System.out.println("Program Exiting.");
    }
}