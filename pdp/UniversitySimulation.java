//Student Name : Bandaru Kiran Subrahamanya Sai
//Student No : 3178784
//Course: Parallel and Distributed Programming 
//Threads, Locks and Semaphores 
//Module: PDP 
//Semester: Semester II 
//Assignment Number: 1


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


class Classroom {
    private final String roomId;
    private final int roomCapacity;
    
    private final Semaphore traineeSlots;
    

    private final Semaphore visitorPasses;
    
    
    private final Semaphore tutorAccessLock;
    
    
    private boolean lectureOngoing = false;
    
    
    private String activeTutor = "None";
    
    
    private int currentTrainees = 0;
    
    private int currentVisitors = 0;
    
    
    private final Lock roomLock = new ReentrantLock();

    public Classroom(String id, int capacity) {
        this.roomId = id;
        this.roomCapacity = capacity;
        this.traineeSlots = new Semaphore(capacity, true);
        this.visitorPasses = new Semaphore(5, true);
        this.tutorAccessLock = new Semaphore(1);
    }

    public boolean attemptTraineeEntry() {
        roomLock.lock();
        try {
            if (!lectureOngoing && traineeSlots.tryAcquire()) {
                currentTrainees++;
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }


    public boolean attemptVisitorEntry() {
        roomLock.lock();
        try {
            if (!lectureOngoing && visitorPasses.tryAcquire()) {
                currentVisitors++;
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }

    public void traineeExit() {
        roomLock.lock();
        try {
            currentTrainees--;
            traineeSlots.release();
        } finally {
            roomLock.unlock();
        }
    }

    public void visitorExit() {
        roomLock.lock();
        try {
            currentVisitors--;
            visitorPasses.release();
        } finally {
            roomLock.unlock();
        }
    }

    public boolean grantTutorAccess(String tutorName) {
        roomLock.lock();
        try {
            if (tutorAccessLock.tryAcquire()) {
                lectureOngoing = true;
                activeTutor = tutorName;
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }

    public void tutorDeparture() {
        roomLock.lock();
        try {
            lectureOngoing = false;
            activeTutor = "None";
            tutorAccessLock.release();
        } finally {
            roomLock.unlock();
        }
    }



    public String getRoomId() {
        return roomId;
    }

    public String getActiveTutor() {
        return activeTutor;
    }

    public boolean isLectureOngoing() {
        return lectureOngoing;
    }

    public int getCurrentTrainees() {
        return currentTrainees;
    }

    public int getCurrentVisitors() {
        return currentVisitors;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }
}


class Trainee extends Thread {
    private final Classroom trainingArea;

    public Trainee(Classroom area) {
        this.trainingArea = area;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (trainingArea.attemptTraineeEntry()) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 4000));
                    
                    // If there's no lecture, exit after studying
                    if (!trainingArea.isLectureOngoing()) {
                        trainingArea.traineeExit();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

class Visitor extends Thread {
    private final Classroom visitingSpace;

    public Visitor(Classroom space) {
        this.visitingSpace = space;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (visitingSpace.attemptVisitorEntry()) {
                try {
                    int stayTime = ThreadLocalRandom.current().nextInt(500, 2500);
                    Thread.sleep(stayTime);
                    visitingSpace.visitorExit();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}


class Tutor extends Thread {
    private final Classroom lectureArea;
    private final String tutorId;

    public Tutor(Classroom area, String id) {
        this.lectureArea = area;
        this.tutorId = id;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (lectureArea.grantTutorAccess(tutorId)) {
                try {
                    Thread.sleep(4000);  // Lecture duration
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                lectureArea.tutorDeparture();
            }
            // Wait before trying to start another lecture
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class StatusReporter extends Thread {
    private final List<Classroom> trackedRooms;

    public StatusReporter(List<Classroom> rooms) {
        this.trackedRooms = rooms;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("\n" + "=".repeat(80));
            System.out.printf("%-12s %-12s %-12s %-12s %-12s%n",
                "ClassRoom", "Lecturer", "InSession", "Students", "Visitors");
            System.out.println("=".repeat(80));

            synchronized (trackedRooms) {
                for (Classroom room : trackedRooms) {
                    System.out.printf("%-12s %-12s %-12s %-12d %-12d%n",
                        room.getRoomId(),
                        room.getActiveTutor(),
                        room.isLectureOngoing() ? "True" : "False",
                        room.getCurrentTrainees(),
                        room.getCurrentVisitors());
                }
            }

            System.out.println("=".repeat(80) + "\n");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}


public class UniversitySimulation {
    public static void main(String[] args) {
        List<Classroom> allRooms = Arrays.asList(
            new Classroom("W201", 60),
            new Classroom("W202", 60),
            new Classroom("W101", 20),
            new Classroom("JS101", 30)
            
        );

        List<String> facultyMembers = Arrays.asList(
            "Osama", "Barry", "Faheem", "Alex", "Aqeel", "Waseem"
        );

    
        for (Classroom room : allRooms) {
            int traineesToSpawn = room.getRoomCapacity() / 3;
            for (int i = 0; i < traineesToSpawn; i++) {
                new Trainee(room).start();
            }
            for (int i = 0; i < 3; i++) {
                new Visitor(room).start();
            }
        }

        
        for (int i = 0; i < facultyMembers.size(); i++) {
            new Tutor(
                allRooms.get(i % allRooms.size()),
                facultyMembers.get(i)
            ).start();
        }
    new StatusReporter(allRooms).start();
    }
}