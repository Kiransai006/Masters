import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class CollegeClassrooms {
    // the Classroom data
    static final Map<String, Integer> CLASSROOM_CAPACITIES = Map.of(
        "W201", 60,
        "W202", 60,
        "W101", 20,
        "J5101", 30
    );
    
    // Lecturer names
    static final String[] LECTURERS = {"Osama", "Barry", "Faheem", "Alex", "Ageel", "Waseem"};
    
    // Shared classroom objects
    static final Map<String, Classroom> classrooms = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        // Initializng classrooms
        for (String room : CLASSROOM_CAPACITIES.keySet()) {
            classrooms.put(room, new Classroom(room, CLASSROOM_CAPACITIES.get(room)));
        }
        
        // Starting monitor thread
        new Thread(new Monitor()).start();
        
        // Start lecturer threads (one per lecturer)
        for (String lecturer : LECTURERS) {
            new Thread(new Lecturer(lecturer)).start();
        }
        
        // Continuously create student and visitor threads
        Random random = new Random();
        while (true) {
            try {
                // Random delay before creating new students/visitors
                Thread.sleep(random.nextInt(1000) + 500);
                
                // Create new student
                String randomClassroom = getRandomClassroom();
                new Thread(new Student(randomClassroom)).start();
                
                // Occasionally create visitor
                if (random.nextDouble() < 0.3) {
                    new Thread(new Visitor(getRandomClassroom())).start();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private static String getRandomClassroom() {
        String[] rooms = CLASSROOM_CAPACITIES.keySet().toArray(new String[0]);
        return rooms[new Random().nextInt(rooms.length)];
    }
    
    // Classroom class representing each of theclassroom
    static class Classroom {
        final String name;
        final int capacity;
        final Semaphore studentSemaphore;
        final Semaphore lecturerSemaphore = new Semaphore(1); // Binary semaphore for lecturer
        final Lock lock = new ReentrantLock();
        final Condition lectureCondition = lock.newCondition();
        
        String currentLecturer = null;
        boolean inSession = false;
        int studentCount = 0;
        int visitorCount = 0;
        
        public Classroom(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
            this.studentSemaphore = new Semaphore(capacity - 5); // Reserving 5 spots for visitors
        }
        
        public void enterStudent() throws InterruptedException {
            studentSemaphore.acquire();
            lock.lock();
            try {
                while (inSession) {
                    lectureCondition.await();
                }
                studentCount++;
            } finally {
                lock.unlock();
            }
        }
        
        public void leaveStudent() {
            lock.lock();
            try {
                studentCount--;
                studentSemaphore.release();
            } finally {
                lock.unlock();
            }
        }
        
        public void enterVisitor() throws InterruptedException {
            lock.lock();
            try {
                while (inSession || visitorCount >= 5) { // Max 5 visitors
                    lectureCondition.await();
                }
                visitorCount++;
            } finally {
                lock.unlock();
            }
        }
        
        public void leaveVisitor() {
            lock.lock();
            try {
                visitorCount--;
                lectureCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
        
        public void enterLecturer(String lecturerName) throws InterruptedException {
            lecturerSemaphore.acquire();
            lock.lock();
            try {
                currentLecturer = lecturerName;
                inSession = false; // the Lecture hasn't started yet
                // No one can enter now, but students can still leave
            } finally {
                lock.unlock();
            }
        }
        
        public void startLecture() {
            lock.lock();
            try {
                inSession = true;
                lectureCondition.signalAll(); // to  Notify waiting threads
            } finally {
                lock.unlock();
            }
        }
        
        public void leaveLecturer() {
            lock.lock();
            try {
                inSession = false;
                currentLecturer = null;
                lecturerSemaphore.release();
                lectureCondition.signalAll(); // for Notifying waiting threads
            } finally {
                lock.unlock();
            }
        }
        
        public String getStatus() {
            lock.lock();
            try {
                return String.format("%-6s %-8s %-5s %-8d %-8d",
                    name,
                    currentLecturer != null ? currentLecturer : "None",
                    inSession,
                    studentCount,
                    visitorCount);
            } finally {
                lock.unlock();
            }
        }
    }
    
    // Student thread
    static class Student implements Runnable {
        private final String classroomName;
        
        public Student(String classroomName) {
            this.classroomName = classroomName;
        }
        
        @Override
        public void run() {
            Classroom classroom = classrooms.get(classroomName);
            try {
                classroom.enterStudent();
                System.out.println("Student entered " + classroomName);
                
                // Simulating student staying in class for random time
                Thread.sleep(new Random().nextInt(5000) + 3000);
                
                classroom.leaveStudent();
                System.out.println("Student left " + classroomName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // Visitor thread
    static class Visitor implements Runnable {
        private final String classroomName;
        
        public Visitor(String classroomName) {
            this.classroomName = classroomName;
        }
        
        @Override
        public void run() {
            Classroom classroom = classrooms.get(classroomName);
            try {
                classroom.enterVisitor();
                System.out.println("Visitor entered " + classroomName);
                
                // Simulating visitor staying for shorter time
                Thread.sleep(new Random().nextInt(3000) + 1000);
                
                classroom.leaveVisitor();
                System.out.println("Visitor left " + classroomName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // Lecturer thread
    static class Lecturer implements Runnable {
        private final String name;
        private final Random random = new Random();
        
        public Lecturer(String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    // Choose a random classroom
                    String classroomName = getRandomClassroom();
                    Classroom classroom = classrooms.get(classroomName);
                    
                    // Enter classroom
                    classroom.enterLecturer(name);
                    System.out.println(name + " entered " + classroomName);
                    
                    // Start lecture after a delay
                    Thread.sleep(random.nextInt(2000) + 1000);
                    classroom.startLecture();
                    System.out.println(name + " started lecture in " + classroomName);
                    
                    // Simulate lecture duration
                    Thread.sleep(random.nextInt(8000) + 5000);
                    
                    // End lecture and leave
                    classroom.leaveLecturer();
                    System.out.println(name + " left " + classroomName);
                    
                    // Wait before going to next classroom
                    Thread.sleep(random.nextInt(5000) + 3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    // Monitoringg thread
    static class Monitor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000); // to Update every 2 seconds
                    printStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        private void printStatus() {
            System.out.println("\n=============================================================================");
            System.out.printf("%-6s %-8s %-5s %-8s %-8s%n", 
                "Room", "Lecturer", "InSes", "Students", "Visitors");
            System.out.println("=============================================================================");
            
            for (Classroom classroom : classrooms.values()) {
                System.out.println(classroom.getStatus());
            }
            
            System.out.println("=============================================================================\n");
        }
    }
}