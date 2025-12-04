import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class CollegeSimulation {
    // Classroom class representing each classroom in the college
    static class Classroom {
        String name;
        int capacity;
        Lecturer currentLecturer;
        boolean inSession;
        int studentCount;
        int visitorCount;
        Semaphore lecturerSemaphore; // Binary semaphore for lecturer
        Semaphore studentSemaphore; // Counting semaphore for students
        Semaphore visitorSemaphore; // Counting semaphore for visitors
        Lock classroomLock;
        Condition lectureCondition;

        public Classroom(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
            this.inSession = false;
            this.studentCount = 0;
            this.visitorCount = 0;
            this.lecturerSemaphore = new Semaphore(1); // Only one lecturer at a time
            this.studentSemaphore = new Semaphore(capacity); // Capacity limits students
            this.visitorSemaphore = new Semaphore(5); // Max 5 visitors
            this.classroomLock = new ReentrantLock();
            this.lectureCondition = classroomLock.newCondition();
        }

        public void studentEnter() throws InterruptedException {
            studentSemaphore.acquire();
            classroomLock.lock();
            try {
                while (inSession) {
                    lectureCondition.await();
                }
                studentCount++;
            } finally {
                classroomLock.unlock();
            }
        }

        public void studentLeave() throws InterruptedException {
            classroomLock.lock();
            try {
                if (!inSession) {
                    studentCount--;
                    studentSemaphore.release();
                }
            } finally {
                classroomLock.unlock();
            }
        }

        public void visitorEnter() throws InterruptedException {
            visitorSemaphore.acquire();
            classroomLock.lock();
            try {
                while (inSession) {
                    lectureCondition.await();
                }
                visitorCount++;
            } finally {
                classroomLock.unlock();
            }
        }

        public void visitorLeave() throws InterruptedException {
            classroomLock.lock();
            try {
                visitorCount--;
                visitorSemaphore.release();
            } finally {
                classroomLock.unlock();
            }
        }

        public void lecturerEnter(Lecturer lecturer) throws InterruptedException {
            lecturerSemaphore.acquire();
            classroomLock.lock();
            try {
                currentLecturer = lecturer;
                inSession = true;
                lectureCondition.signalAll();
            } finally {
                classroomLock.unlock();
            }
        }

        public void startLecture() {
            // Lecture is already marked as in session when lecturer enters
        }

        public void lecturerLeave() {
            classroomLock.lock();
            try {
                inSession = false;
                currentLecturer = null;
                lectureCondition.signalAll();
            } finally {
                classroomLock.unlock();
                lecturerSemaphore.release();
            }
        }

        public String getStatus() {
            return String.format("%-5s %-10s %-5s %-8d %-8d", 
                name, 
                currentLecturer != null ? currentLecturer.name : "None", 
                inSession, 
                studentCount, 
                visitorCount);
        }
    }

    // Lecturer class
    static class Lecturer {
        String name;
        List<Classroom> classrooms;
        Random random;

        public Lecturer(String name, List<Classroom> classrooms) {
            this.name = name;
            this.classrooms = classrooms;
            this.random = new Random();
        }

        public void run() {
            while (true) {
                try {
                    // Choose a random classroom
                    Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
                    
                    // Enter the classroom
                    classroom.lecturerEnter(this);
                    
                    // Start lecture
                    classroom.startLecture();
                    
                    // Simulate lecture time (10-15 minutes)
                    Thread.sleep(10000 + random.nextInt(5000));
                    
                    // Leave the classroom
                    classroom.lecturerLeave();
                    
                    // Take a break before next lecture (5-10 minutes)
                    Thread.sleep(5000 + random.nextInt(5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // Student class with classroom preferences
    static class Student implements Runnable {
        List<Classroom> classrooms;
        Random random;
        Classroom preferredClassroom;

        public Student(List<Classroom> classrooms) {
            this.classrooms = classrooms;
            this.random = new Random();
            // Students tend to prefer certain classrooms (weighted probability)
            double rand = random.nextDouble();
            if (rand < 0.4) {
                preferredClassroom = classrooms.get(0); // W201 (40% chance)
            } else if (rand < 0.7) {
                preferredClassroom = classrooms.get(1); // W202 (30% chance)
            } else if (rand < 0.9) {
                preferredClassroom = classrooms.get(3); // J5101 (20% chance)
            } else {
                preferredClassroom = classrooms.get(2); // W101 (10% chance)
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // 70% chance to go to preferred classroom
                    Classroom classroom = random.nextDouble() < 0.7 ? preferredClassroom : 
                                        classrooms.get(random.nextInt(classrooms.size()));
                    
                    classroom.studentEnter();
                    
                    // Stay longer if lecture is in session (8-18 minutes)
                    if (classroom.inSession) {
                        Thread.sleep(8000 + random.nextInt(10000));
                    } else {
                        // Stay shorter if no lecture (3-8 minutes)
                        Thread.sleep(3000 + random.nextInt(5000));
                    }
                    
                    classroom.studentLeave();
                    
                    // Take a break between classes (2-7 minutes)
                    Thread.sleep(2000 + random.nextInt(5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // Visitor class
    static class Visitor implements Runnable {
        List<Classroom> classrooms;
        Random random;

        public Visitor(List<Classroom> classrooms) {
            this.classrooms = classrooms;
            this.random = new Random();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Choose a random classroom
                    Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
                    
                    // Enter the classroom
                    classroom.visitorEnter();
                    
                    // Simulate time in classroom (2-6 minutes)
                    Thread.sleep(2000 + random.nextInt(4000));
                    
                    // Leave the classroom
                    classroom.visitorLeave();
                    
                    // Take a break before next visit (3-9 minutes)
                    Thread.sleep(3000 + random.nextInt(6000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // Monitor class to display status
    static class Monitor implements Runnable {
        List<Classroom> classrooms;

        public Monitor(List<Classroom> classrooms) {
            this.classrooms = classrooms;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Clear console (for better display)
                    System.out.print("\033[H\033[2J");
                    System.out.flush();

                    // Print header
                    System.out.println("=".repeat(60));
                    System.out.printf("%-5s %-10s %-5s %-8s %-8s%n", 
                        "Room", "Lecturer", "InSes", "Students", "Visitors");
                    System.out.println("=".repeat(60));

                    // Print status for each classroom
                    for (Classroom classroom : classrooms) {
                        System.out.println(classroom.getStatus());
                    }

                    // Print timestamp
                    System.out.println("\nLast update: " + new Date());
                    System.out.println("=".repeat(60));

                    // Wait before next update (2 seconds)
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        // Create classrooms
        List<Classroom> classrooms = new ArrayList<>();
        classrooms.add(new Classroom("W201", 60));
        classrooms.add(new Classroom("W202", 60));
        classrooms.add(new Classroom("W101", 20));
        classrooms.add(new Classroom("J5101", 30));

        // Create lecturers
        String[] lecturerNames = {"Osama", "Barry", "Faheem", "Alex", "Ageel", "Waseem"};
        List<Lecturer> lecturers = new ArrayList<>();
        for (String name : lecturerNames) {
            lecturers.add(new Lecturer(name, classrooms));
        }

        // Create and start threads
        ExecutorService executor = Executors.newCachedThreadPool();

        // Start lecturers
        for (Lecturer lecturer : lecturers) {
            executor.execute(lecturer::run);
        }

        // Start students (150 students)
        for (int i = 0; i < 150; i++) {
            executor.execute(new Student(classrooms));
        }

        // Start visitors (15 visitors)
        for (int i = 0; i < 15; i++) {
            executor.execute(new Visitor(classrooms));
        }

        // Start monitor
        executor.execute(new Monitor(classrooms));

        // Let the simulation run for a while (1 hour)
        try {
            Thread.sleep(3600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }
}