package com.placement.util;

import com.placement.model.*;
import com.placement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds rich demo data on initial application startup:
 * - 1 Officer account (officer@college.edu / Officer@123)
 * - 5 Verified recruiting companies
 * - 10 Diverse student candidates with academic profiles
 * - 10 Approved job postings with realistic criteria
 * - Realistic candidate applications in diverse recruitment stages
 * - Institutional bulletins and peer interview experiences
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final InterviewExperienceRepository interviewExperienceRepository;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      CompanyRepository companyRepository,
                      StudentProfileRepository studentProfileRepository,
                      JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      AnnouncementRepository announcementRepository,
                      InterviewExperienceRepository interviewExperienceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.interviewExperienceRepository = interviewExperienceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedOfficer();
        seedCompanies();
        seedStudents();
        seedJobs();
        seedApplications();
        seedAnnouncements();
        seedInterviewExperiences();
    }

    private void seedOfficer() {
        String email = "officer@college.edu";
        if (!userRepository.existsByEmail(email)) {
            User officer = new User(email, passwordEncoder.encode("Officer@123"), Role.ROLE_OFFICER);
            officer.setActive(true);
            userRepository.save(officer);
            logger.info("Seeded Placement Officer: {}", email);
        }
    }

    private void seedCompanies() {
        if (companyRepository.count() >= 5) {
            return;
        }

        createCompany("recruiter@google.com", "Google@123", "Google India", "Bangalore, Karnataka",
                "https://careers.google.com", "Global technology company specializing in internet-related services and products.",
                "hr@google.com", "9845012345");

        createCompany("recruiter@microsoft.com", "Microsoft@123", "Microsoft Corporation", "Hyderabad, Telangana",
                "https://careers.microsoft.com", "Leading multinational developer of personal-computer software, services, and cloud solutions.",
                "campus@microsoft.com", "9845023456");

        createCompany("recruiter@amazon.com", "Amazon@123", "Amazon Web Services", "Bangalore, Karnataka",
                "https://amazon.jobs", "World leader in cloud computing and e-commerce infrastructure platforms.",
                "university-recruiting@amazon.com", "9845034567");

        createCompany("recruiter@tcs.com", "Tcs@123", "TCS Digital", "Mumbai, Maharashtra",
                "https://www.tcs.com/careers", "Premier IT services, consulting, and business solutions organization.",
                "campus.careers@tcs.com", "9845045678");

        createCompany("recruiter@infosys.com", "Infosys@123", "Infosys Technologies", "Bangalore, Karnataka",
                "https://www.infosys.com/careers", "Global leader in next-generation digital services and consulting.",
                "talent.acquisition@infosys.com", "9845056789");

        logger.info("Seeded 5 top recruiting partner companies.");
    }

    private Company createCompany(String email, String rawPassword, String name, String location,
                                 String website, String description, String contactEmail, String contactNumber) {
        if (userRepository.existsByEmail(email)) {
            return companyRepository.findByUser(userRepository.findByEmail(email).get()).orElse(null);
        }
        User user = new User(email, passwordEncoder.encode(rawPassword), Role.ROLE_COMPANY);
        user.setActive(true);
        userRepository.save(user);

        Company company = new Company(user, name, location, website, description, contactEmail, contactNumber, CompanyStatus.APPROVED);
        return companyRepository.save(company);
    }

    private void seedStudents() {
        if (studentProfileRepository.count() >= 10) {
            return;
        }

        createStudent("aarav.sharma@college.edu", "Student@123", "Aarav Sharma", LocalDate.of(2004, 5, 14),
                "Computer Science", "aarav.sharma@college.edu", "9876500001", 8.8, 0, 2026,
                "Java, Spring Boot, MySQL, Data Structures", true);

        createStudent("priya.patel@college.edu", "Student@123", "Priya Patel", LocalDate.of(2004, 8, 22),
                "Information Technology", "priya.patel@college.edu", "9876500002", 9.2, 0, 2026,
                "Python, Machine Learning, SQL, Algorithms", true);

        createStudent("rohan.verma@college.edu", "Student@123", "Rohan Verma", LocalDate.of(2004, 3, 10),
                "Computer Science", "rohan.verma@college.edu", "9876500003", 8.1, 0, 2026,
                "JavaScript, React, Node.js, MongoDB", false);

        createStudent("sneha.reddy@college.edu", "Student@123", "Sneha Reddy", LocalDate.of(2004, 11, 5),
                "Electronics & Communication", "sneha.reddy@college.edu", "9876500004", 8.5, 0, 2026,
                "C++, Embedded Systems, IoT, Python", true);

        createStudent("vikram.singh@college.edu", "Student@123", "Vikram Singh", LocalDate.of(2003, 12, 19),
                "Mechanical Engineering", "vikram.singh@college.edu", "9876500005", 7.8, 1, 2026,
                "AutoCAD, SolidWorks, Python, MATLAB", false);

        createStudent("ananya.iyer@college.edu", "Student@123", "Ananya Iyer", LocalDate.of(2004, 1, 28),
                "Computer Science", "ananya.iyer@college.edu", "9876500006", 9.5, 0, 2026,
                "Java, AWS, Cloud Computing, Docker, System Design", true);

        createStudent("karthik.nair@college.edu", "Student@123", "Karthik Nair", LocalDate.of(2004, 7, 15),
                "Information Technology", "karthik.nair@college.edu", "9876500007", 7.4, 0, 2026,
                "HTML, CSS, JavaScript, PHP, MySQL", false);

        createStudent("pooja.gupta@college.edu", "Student@123", "Pooja Gupta", LocalDate.of(2004, 9, 3),
                "Electronics & Communication", "pooja.gupta@college.edu", "9876500008", 8.3, 0, 2026,
                "VLSI, Verilog, C++, Microcontrollers", false);

        createStudent("rahul.das@college.edu", "Student@123", "Rahul Das", LocalDate.of(2003, 6, 17),
                "Computer Science", "rahul.das@college.edu", "9876500009", 6.9, 2, 2026,
                "Python, Web Development, Git", false);

        createStudent("divya.menon@college.edu", "Student@123", "Divya Menon", LocalDate.of(2004, 4, 30),
                "Information Technology", "divya.menon@college.edu", "9876500010", 8.7, 0, 2026,
                "Java, Kotlin, Android Development, REST APIs", true);

        logger.info("Seeded 10 student candidates.");
    }

    private StudentProfile createStudent(String email, String rawPassword, String name, LocalDate dob,
                                         String department, String collegeEmail, String phone,
                                         double cgpa, int arrears, int gradYear, String skills, boolean placed) {
        if (userRepository.existsByEmail(email)) {
            return studentProfileRepository.findByUser(userRepository.findByEmail(email).get()).orElse(null);
        }
        User user = new User(email, passwordEncoder.encode(rawPassword), Role.ROLE_STUDENT);
        user.setActive(true);
        userRepository.save(user);

        StudentProfile profile = new StudentProfile(user, name, department, collegeEmail, phone);
        profile.setDateOfBirth(dob);
        profile.setCgpa(cgpa);
        profile.setTotalArrears(arrears);
        profile.setPassingYear(gradYear);
        profile.setSkills(skills);
        profile.setPlaced(placed);
        return studentProfileRepository.save(profile);
    }

    private void seedJobs() {
        if (jobRepository.count() >= 10) {
            return;
        }

        Company google = companyRepository.findByName("Google India").orElse(null);
        Company msft = companyRepository.findByName("Microsoft Corporation").orElse(null);
        Company amazon = companyRepository.findByName("Amazon Web Services").orElse(null);
        Company tcs = companyRepository.findByName("TCS Digital").orElse(null);
        Company infy = companyRepository.findByName("Infosys Technologies").orElse(null);

        if (google == null || msft == null || amazon == null || tcs == null || infy == null) {
            return;
        }

        createJob(google, "Software Development Engineer - I", JobType.FULL_TIME,
                "Join Google engineering team to design large-scale distributed systems and high-throughput web architectures.",
                "Bangalore", 28.0, 5, 8.5, 0, "Java, Data Structures, Algorithms, System Design",
                "Computer Science, Information Technology", 2026, false, null, LocalDate.now().plusDays(25));

        createJob(google, "Site Reliability Engineer (SRE)", JobType.FULL_TIME,
                "Optimize performance and reliability across Google distributed infrastructure and mission-critical cloud pipelines.",
                "Bangalore", 24.0, 3, 8.0, 0, "Python, Linux, Networking, Distributed Systems",
                "Computer Science, Information Technology, Electronics & Communication", 2026, false, null, LocalDate.now().plusDays(20));

        createJob(msft, "Associate Cloud Solutions Engineer", JobType.FULL_TIME,
                "Architect enterprise cloud infrastructure solutions and deploy microservices on Azure.",
                "Hyderabad", 22.0, 8, 8.0, 0, "Python, Cloud Computing, C++, Algorithms",
                "Computer Science, Information Technology, Electronics & Communication", 2026, false, null, LocalDate.now().plusDays(30));

        createJob(msft, "Support Escalation Engineer", JobType.FULL_TIME,
                "Collaborate with top enterprise customers to resolve complex system architectures and networking anomalies.",
                "Bangalore", 14.0, 6, 7.0, 0, "Troubleshooting, Operating Systems, Networking, C++",
                "Computer Science, Information Technology, Electronics & Communication, Mechanical Engineering", 2026, false, null, LocalDate.now().plusDays(28));

        createJob(amazon, "Systems Development Engineer", JobType.FULL_TIME,
                "Build automation tools and services that maintain high availability of Amazon AWS global cloud footprint.",
                "Bangalore", 18.5, 10, 7.5, 0, "Java, Linux, Distributed Systems, Scripting",
                "Computer Science, Information Technology", 2026, false, null, LocalDate.now().plusDays(15));

        createJob(amazon, "QA Automation Engineer", JobType.FULL_TIME,
                "Develop comprehensive automated test frameworks, regression suites, and load testing harnesses.",
                "Hyderabad", 12.0, 4, 7.0, 0, "Selenium, Java, Testing, CI/CD",
                "Computer Science, Information Technology, Electronics & Communication", 2026, false, null, LocalDate.now().plusDays(22));

        createJob(tcs, "Digital Innovator - Full Stack", JobType.FULL_TIME,
                "Work on next-gen digital initiatives involving cloud-native Spring Boot apps, AI integrations, and responsive UX.",
                "Mumbai", 7.5, 20, 7.0, 1, "Java, Python, SQL, Spring Boot",
                "Computer Science, Information Technology, Electronics & Communication, Mechanical Engineering", 2026, false, null, LocalDate.now().plusDays(35));

        createJob(tcs, "Ninja Developer Trainee", JobType.FULL_TIME,
                "Entry-level engineering track with comprehensive technical foundation training in software engineering.",
                "Pune", 3.6, 35, 6.0, 2, "C++, Java, Basic Programming, DBMS",
                "Computer Science, Information Technology, Electronics & Communication, Mechanical Engineering", 2026, false, null, LocalDate.now().plusDays(40));

        createJob(infy, "Specialist Programmer", JobType.FULL_TIME,
                "High-performance coding role focused on core architecture development, algorithms, and microservice engineering.",
                "Bangalore", 9.5, 15, 7.5, 0, "Java, Full Stack, Data Structures, OOP",
                "Computer Science, Information Technology, Electronics & Communication", 2026, false, null, LocalDate.now().plusDays(18));

        createJob(infy, "Systems Engineer", JobType.FULL_TIME,
                "Develop and maintain enterprise business applications across global consulting engagements.",
                "Chennai", 4.5, 40, 6.0, 1, "Java, DBMS, HTML, CSS",
                "Computer Science, Information Technology, Electronics & Communication, Mechanical Engineering", 2026, false, null, LocalDate.now().plusDays(32));

        logger.info("Seeded 10 campus job drives.");
    }

    private Job createJob(Company company, String title, JobType jobType, String description,
                         String location, double pkg, int vacancies, double minCgpa, int maxArrears,
                         String requiredSkills, String eligibleDepts, int passingYear,
                         boolean internshipReq, String internshipDetails, LocalDate deadline) {
        Job job = new Job(company, title, jobType, description, location, pkg, vacancies, minCgpa,
                maxArrears, requiredSkills, eligibleDepts, passingYear, internshipReq, internshipDetails,
                deadline, JobStatus.APPROVED);
        return jobRepository.save(job);
    }

    private void seedApplications() {
        if (applicationRepository.count() >= 12) {
            return;
        }

        List<StudentProfile> students = studentProfileRepository.findAll();
        List<Job> jobs = jobRepository.findAll();

        if (students.size() < 10 || jobs.size() < 10) {
            return;
        }

        StudentProfile aarav = students.stream().filter(s -> s.getName().contains("Aarav")).findFirst().orElse(students.get(0));
        StudentProfile priya = students.stream().filter(s -> s.getName().contains("Priya")).findFirst().orElse(students.get(1));
        StudentProfile rohan = students.stream().filter(s -> s.getName().contains("Rohan")).findFirst().orElse(students.get(2));
        StudentProfile sneha = students.stream().filter(s -> s.getName().contains("Sneha")).findFirst().orElse(students.get(3));
        StudentProfile ananya = students.stream().filter(s -> s.getName().contains("Ananya")).findFirst().orElse(students.get(5));
        StudentProfile divya = students.stream().filter(s -> s.getName().contains("Divya")).findFirst().orElse(students.get(9));
        StudentProfile karthik = students.stream().filter(s -> s.getName().contains("Karthik")).findFirst().orElse(students.get(6));

        Job googleSde = jobs.stream().filter(j -> j.getTitle().contains("Software Development Engineer")).findFirst().orElse(jobs.get(0));
        Job msftCloud = jobs.stream().filter(j -> j.getTitle().contains("Associate Cloud Solutions")).findFirst().orElse(jobs.get(2));
        Job amazonSys = jobs.stream().filter(j -> j.getTitle().contains("Systems Development")).findFirst().orElse(jobs.get(4));
        Job tcsDigital = jobs.stream().filter(j -> j.getTitle().contains("Digital Innovator")).findFirst().orElse(jobs.get(6));
        Job infySpec = jobs.stream().filter(j -> j.getTitle().contains("Specialist Programmer")).findFirst().orElse(jobs.get(8));
        Job infySys = jobs.stream().filter(j -> j.getTitle().contains("Systems Engineer")).findFirst().orElse(jobs.get(9));

        createApp(aarav, googleSde, ApplicationStatus.PLACED, LocalDate.now().minusDays(10), "10:00 AM", "Google HQ Campus / Meet");
        createApp(aarav, msftCloud, ApplicationStatus.SELECTED, LocalDate.now().minusDays(12), "02:00 PM", "Microsoft Teams");

        createApp(priya, googleSde, ApplicationStatus.SELECTED, LocalDate.now().minusDays(8), "11:30 AM", "Google Meet");
        createApp(priya, amazonSys, ApplicationStatus.INTERVIEW_SCHEDULED, LocalDate.now().plusDays(4), "03:00 PM", "Chime Meeting Room A");

        createApp(sneha, msftCloud, ApplicationStatus.PLACED, LocalDate.now().minusDays(15), "10:30 AM", "Microsoft Campus Hyderabad");

        createApp(ananya, googleSde, ApplicationStatus.PLACED, LocalDate.now().minusDays(14), "09:30 AM", "Google Meet");
        createApp(ananya, amazonSys, ApplicationStatus.SELECTED, LocalDate.now().minusDays(16), "04:00 PM", "AWS Office Bangalore");

        createApp(divya, infySpec, ApplicationStatus.PLACED, LocalDate.now().minusDays(18), "11:00 AM", "Infosys Hall 2");

        createApp(rohan, tcsDigital, ApplicationStatus.SHORTLISTED, null, null, null);
        createApp(rohan, infySpec, ApplicationStatus.APPLIED, null, null, null);

        createApp(karthik, tcsDigital, ApplicationStatus.APPLIED, null, null, null);
        createApp(karthik, infySys, ApplicationStatus.SHORTLISTED, null, null, null);

        logger.info("Seeded candidate applications across recruitment states.");
    }

    private void createApp(StudentProfile student, Job job, ApplicationStatus status,
                           LocalDate interviewDate, String interviewTime, String venue) {
        if (applicationRepository.existsByStudentAndJob(student, job)) {
            return;
        }
        Application app = new Application(student, job);
        app.setStatus(status);
        app.setInterviewDate(interviewDate);
        app.setInterviewTime(interviewTime);
        app.setInterviewVenue(venue);
        applicationRepository.save(app);
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() >= 4) {
            return;
        }

        announcementRepository.save(new Announcement(
                "Google On-Campus Recruitment Drive 2026 Schedule",
                "Google India campus recruitment for SDE-I and SRE roles will commence next week. Registered candidates should verify resume links and report to Seminar Hall A on Monday at 9:00 AM sharp with college ID."));

        announcementRepository.save(new Announcement(
                "Resume Building & Technical Mock Interview Workshop",
                "Placement cell is organizing an intensive mock technical interview workshop this Saturday from 10:00 AM to 1:00 PM. Expert alumni from Amazon and Microsoft will review student resumes and conduct 1:1 DSA problem-solving sessions."));

        announcementRepository.save(new Announcement(
                "TCS Digital Assessment Results Published",
                "TCS has released the list of shortlisted candidates for Digital Innovator positions. Shortlisted students must upload their updated semester transcripts by Thursday 5:00 PM."));

        announcementRepository.save(new Announcement(
                "Mandatory Pre-Placement Training on Aptitude & Communication",
                "All pre-final and final year CSE/IT/ECE students must attend the 5-day structured soft-skills and quantitative aptitude boot camp starting next month. 85% attendance is compulsory to remain eligible for upcoming tier-1 drives."));

        logger.info("Seeded 4 institutional campus placement announcements.");
    }

    private void seedInterviewExperiences() {
        if (interviewExperienceRepository.count() >= 3) {
            return;
        }

        StudentProfile ananya = studentProfileRepository.findByCollegeEmail("ananya.iyer@college.edu").orElse(null);
        StudentProfile sneha = studentProfileRepository.findByCollegeEmail("sneha.reddy@college.edu").orElse(null);
        StudentProfile divya = studentProfileRepository.findByCollegeEmail("divya.menon@college.edu").orElse(null);

        Company google = companyRepository.findByName("Google India").orElse(null);
        Company msft = companyRepository.findByName("Microsoft Corporation").orElse(null);
        Company infy = companyRepository.findByName("Infosys Technologies").orElse(null);

        if (ananya != null && google != null) {
            interviewExperienceRepository.save(new InterviewExperience(
                    ananya, google,
                    "Round 1: Online Assessment (2 hard questions on Dynamic Programming & Sliding Window, 60 mins). Round 2: Technical Interview (Binary Trees & Graph BFS). Round 3: System Design & Java Memory Management. Round 4: Googleyness & Cultural fit.",
                    "1. Construct Binary Tree from Preorder and Inorder Traversal. 2. Implement an LRU Cache with O(1) operations. 3. Garbage Collection internals in JVM and thread pool sizing.",
                    "Master Striver SDE sheet and practice writing clean, bug-free code on whiteboard/Google Docs without IDE autocompletion. Explain your thought process out loud!"));
        }

        if (sneha != null && msft != null) {
            interviewExperienceRepository.save(new InterviewExperience(
                    sneha, msft,
                    "Round 1: Microsoft Codility OA (3 questions, 90 mins). Round 2: Core Data Structures (Strings, Two Pointers, HashMaps). Round 3: OS Concepts, Virtual Memory, and Azure Fundamentals. Round 4: Senior Director Round.",
                    "1. Longest Palindromic Substring. 2. Explain Process vs Thread, Deadlocks prevention, and Semaphore implementation. 3. Detailed walkthrough of my IoT final year project.",
                    "Know every bullet point on your resume deeply. Review Operating Systems, Computer Networks, and DBMS fundamentals in addition to DSA. Microsoft values collaborative problem-solving!"));
        }

        if (divya != null && infy != null) {
            interviewExperienceRepository.save(new InterviewExperience(
                    divya, infy,
                    "Round 1: HackWithInfy National Coding Challenge. Round 2: Technical Interview on Java 17 features, Spring Boot architecture, and SQL queries. Round 3: HR Round.",
                    "1. Difference between interface and abstract class in modern Java. 2. Write a SQL query with GROUP BY and HAVING to find second highest salary. 3. Explain Spring Boot dependency injection and annotations (@RestController, @Autowired, @Service).",
                    "For Infosys Specialist Programmer, focus heavily on core Java OOP concepts, collections framework (ArrayList vs LinkedList, HashMap collisions), and practice standard SQL joins."));
        }

        logger.info("Seeded 3 peer interview experiences.");
    }
}
