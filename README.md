# Campus Placement Management System (Campus-PMS) 🎓

A modern, production-grade **Campus Placement Management System** web application designed and built for university career and placement cells. Developed using **Java 17**, **Spring Boot 3**, **Spring Security**, **Spring Data JPA (Hibernate)**, **Thymeleaf**, and **MySQL**, styled with **Bootstrap 5**, **Bootstrap Icons**, and **Chart.js**.

---

## 🚀 Key Features by Role

### 1. 🛡️ Placement Officer (Admin Portal)
- **Executive Placement Dashboard**: Live metric counters for registered students, placed candidates, placement %, approved companies, active drives, and total applications.
- **Visual Analytics (Chart.js)**:
  - 📊 **Department Placement Rates**: Grouped bar chart comparing placed vs. total candidates across CSE, IT, ECE, MECH.
  - 🏢 **Selections by Company**: Bar chart showing offer counts per recruiting firm.
  - 💰 **Package Distribution**: Doughnut chart showing drives by CTC range (<5 LPA, 5-10 LPA, 10-15 LPA, 15+ LPA).
  - 🔄 **Application Status Pipeline**: Funnel breakdown of all candidate applications.
- **Approvals & Governance**:
  - One-click approval / rejection of new **Company Registrations**.
  - Review and approval / rejection of incoming **Job Postings**.
- **Student & Company Directories**:
  - View full academic profiles and PDF resumes.
  - One-click **Toggle Placed** to officially mark students as Placed.
  - One-click **Account Block / Unblock** security controls.
- **Institutional Broadcasts**: Post announcements and guidelines directly to all student dashboards.
- **Interview Experience Moderation**: Review and moderate senior interview tips and advice.
- **Reports & Data Exports (CSV)**:
  - 📥 **Export Placed Students Roster** (CSV with name, department, CGPA, email, contact, placed status).
  - 📥 **Export Master Applications Log** (CSV with complete recruitment tracking and interview details).

---

### 2. 🏢 Recruiting Company (Corporate Partner Portal)
- **Company Profile Management**: Edit company profile, headquarters location, website, HR contact details, and organization description.
- **Campus Drive Posting**: Post job drives with package (LPA), vacancies, minimum CGPA, maximum arrears, eligible departments, graduation year, required skills, and deadline.
- **Candidate Review & Management**:
  - Filter applicants by drive.
  - Direct PDF resume preview for verified applicants.
  - Candidate recruitment progression: **Shortlist**, **Schedule Interview**, **Select**, or **Reject**.
- **Interview Scheduling Modal**:
  - Schedule interview date, time, and venue/meeting link.
  - Automatically notifies the student with complete interview specifics.
- **Export Job Applicants**: Download complete candidate lists for any posted drive in CSV format.
- **Analytics Dashboard**: Bar charts of applicants per drive and recruitment funnel pipeline.

---

### 3. 🎓 Student Candidate (Student Portal)
- **Profile Completeness Tracker**: Real-time progress bar (100% required before applying).
- **Academic Credentials & Resume Upload**:
  - Department, CGPA, arrears count, passing year, technical skills.
  - PDF Resume upload (validated up to 2MB).
- **Smart Job Search & Filtering**:
  - Keyword search, location, minimum package slider, skills filter, and department matching.
  - **"Only Eligible Jobs" Toggle** to instantly hide drives exceeding student's arrears or falling below required CGPA.
- **Automated Eligibility Engine**:
  - Live criteria checklist on every job details page:
    - ✅ CGPA requirement
    - ✅ Maximum arrears allowed
    - ✅ Department match
    - ✅ Graduation passing year
    - ✅ Case-insensitive **Skill Match Percentage** calculation.
- **One-Click Application & Bookmarks**:
  - Apply for eligible drives directly.
  - Bookmark and save jobs to track later.
- **Application Tracking**: Real-time color-coded badges:
  - 🔵 `Applied`
  - 🟡 `Shortlisted`
  - 🟣 `Interview Scheduled` (displays interview date, time, and meeting venue)
  - 🟢 `Selected`
  - 🔴 `Rejected`
  - 🏆 `Placed`
- **Peer Interview Insights**:
  - Read interview stories, round breakdowns, coding questions, and preparation advice.
  - Placed students can publish their interview experiences to guide peers.

---

## 🔑 Default Demo User Credentials

The application includes an automated seeder (`DataSeeder.java`) that pre-configures all roles upon startup:

### 🛡️ Placement Officer
| Email | Password | Role |
|---|---|---|
| `officer@college.edu` | `Officer@123` | Placement Cell Admin |

### 🏢 Recruiting Companies
| Company | Email | Password | Status |
|---|---|---|---|
| **Google India** | `recruiter@google.com` | `Google@123` | APPROVED |
| **Microsoft Corporation** | `recruiter@microsoft.com` | `Microsoft@123` | APPROVED |
| **Amazon Web Services** | `recruiter@amazon.com` | `Amazon@123` | APPROVED |
| **TCS Digital** | `recruiter@tcs.com` | `Tcs@123` | APPROVED |
| **Infosys Technologies** | `recruiter@infosys.com` | `Infosys@123` | APPROVED |

### 🎓 Sample Students
| Student Name | Email | Password | Department | CGPA | Status |
|---|---|---|---|---|---|
| **Aarav Sharma** | `aarav.sharma@college.edu` | `Student@123` | Computer Science | 8.8 | 🏆 Placed (Google SDE) |
| **Priya Patel** | `priya.patel@college.edu` | `Student@123` | Information Technology | 9.2 | 🏆 Placed (Google SDE) |
| **Sneha Reddy** | `sneha.reddy@college.edu` | `Student@123` | Electronics & Communication | 8.5 | 🏆 Placed (Microsoft Cloud) |
| **Ananya Iyer** | `ananya.iyer@college.edu` | `Student@123` | Computer Science | 9.5 | 🏆 Placed (Google SDE) |
| **Divya Menon** | `divya.menon@college.edu` | `Student@123` | Information Technology | 8.7 | 🏆 Placed (Infosys Specialist) |
| **Rohan Verma** | `rohan.verma@college.edu` | `Student@123` | Computer Science | 8.1 | Shortlisted (TCS Digital) |
| **Karthik Nair** | `karthik.nair@college.edu` | `Student@123` | Information Technology | 7.4 | Applied (TCS Digital) |
| **Pooja Gupta** | `pooja.gupta@college.edu` | `Student@123` | Electronics & Communication | 8.3 | Interview Scheduled |
| **Vikram Singh** | `vikram.singh@college.edu` | `Student@123` | Mechanical Engineering | 7.8 | Active |
| **Rahul Das** | `rahul.das@college.edu` | `Student@123` | Computer Science | 6.9 | Active |

---

## 🛠️ Technology Stack & Architecture

- **Backend**: Java 17, Spring Boot 3.2.5
- **Security**: Spring Security 6 with BCrypt password hashing, role-based URL protection, custom authentication handlers
- **Persistence**: Spring Data JPA, Hibernate, MySQL 8
- **View Layer**: Spring MVC, Thymeleaf templating with modular layout fragments (`layout.html`)
- **Frontend / UI**:
  - Bootstrap 5.3.3 (Modern responsive theme)
  - Bootstrap Icons
  - Chart.js 4.4 (Data visualization)
  - Google Font "Inter"
  - Light / Dark Mode with anti-FOUC persistence in `localStorage`
- **Background Tasks**: Spring `@Scheduled` automated job expiry monitor (`JobExpiryScheduler.java`)

---

## 💻 Step-by-Step Setup & How to Run

### 1. Prerequisites
- **Java**: JDK 17 or higher (`java -version`)
- **MySQL Server**: Running on `localhost:3306`
- **Maven**: (Maven Wrapper `mvnw` / `mvnw.cmd` included in project root)

### 2. MySQL Database Setup
In your MySQL client (e.g. MySQL Command Line, Workbench, or DBeaver), create the database:
```sql
CREATE DATABASE IF NOT EXISTS placement_db;
```

Verify your MySQL credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/placement_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=yohan
spring.jpa.hibernate.ddl-auto=update
```
*(Adjust username and password to match your local MySQL configuration if different).*

### 3. Run the Application
Open a terminal in the project root directory and run:

**On Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Once started, access the application in your browser at:
👉 **`http://localhost:8080`**

---

## 📂 Project Structure

```
src/main/java/com/placement/
├── CampusPmsApplication.java         # Main Application Entry Point (@EnableScheduling)
├── config/
│   ├── CustomAuthenticationFailureHandler.java  # Handles pending company login errors
│   ├── CustomAuthenticationSuccessHandler.java  # Role-based landing redirects
│   ├── CustomUserDetails.java                   # UserDetails wrapper
│   ├── CustomUserDetailsService.java            # Loads users & checks company status
│   └── SecurityConfig.java                      # Spring Security filter chain & RBAC
├── controller/
│   ├── AuthController.java                      # Login and user registration
│   ├── CompanyController.java                   # Company profile, jobs, applicant scheduling & CSV
│   ├── CompanyDashboardController.java          # Company dashboard & Chart.js data
│   ├── HomeController.java                      # Public landing page with live counters
│   ├── NotificationController.java              # In-app notifications
│   ├── OfficerDashboardController.java          # Admin dashboard & Chart.js placement analytics
│   ├── OfficerManagementController.java         # Approvals, directories, reports & CSV exports
│   ├── StudentDashboardController.java          # Student dashboard & profile completeness
│   ├── StudentJobController.java                # Job browsing, eligibility, applications, saved jobs
│   └── StudentProfileController.java            # Student profile & PDF resume downloads
├── dto/
│   ├── CompanyRegisterDto.java
│   ├── EligibilityResult.java
│   └── StudentRegisterDto.java
├── model/                                       # JPA Entities (no Lombok, clean getters/setters)
│   ├── Announcement.java
│   ├── Application.java
│   ├── ApplicationStatus.java                   # APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED, PLACED
│   ├── Company.java
│   ├── CompanyStatus.java                       # PENDING, APPROVED, REJECTED
│   ├── InterviewExperience.java
│   ├── Job.java
│   ├── JobStatus.java                           # PENDING, APPROVED, REJECTED, CLOSED
│   ├── JobType.java
│   ├── Notification.java
│   ├── Role.java                                # ROLE_STUDENT, ROLE_COMPANY, ROLE_OFFICER
│   ├── SavedJob.java
│   ├── StudentProfile.java
│   └── User.java
├── repository/                                  # Spring Data JPA interfaces
├── service/
│   ├── EligibilityService.java                  # Multi-rule eligibility evaluation & skill match %
│   ├── FileStorageService.java                  # PDF resume upload & validation (2MB limit)
│   ├── JobExpiryScheduler.java                  # Hourly scheduled job auto-closer
│   ├── NotificationService.java                 # In-app notifications
│   └── UserService.java                         # User registration & password hashing
└── util/
    └── DataSeeder.java                          # Rich demo database seeder

src/main/resources/
├── static/
│   ├── css/style.css                            # Clean custom stylesheet (Light & Dark themes)
│   └── js/main.js                               # Sidebar toggle and dark mode persistence
└── templates/
    ├── company/                                 # Company portal Thymeleaf templates
    ├── fragments/layout.html                    # Layout fragments (navbar, sidebar, head, scripts)
    ├── officer/                                 # Placement Officer portal Thymeleaf templates
    ├── student/                                 # Student portal Thymeleaf templates
    ├── home.html                                # Public landing page
    ├── login.html                               # Unified login
    ├── register-student.html                    # Student registration
    ├── register-company.html                    # Company registration
    ├── notifications.html                       # In-app notification center
    └── error.html                               # Custom error page
```

---

## 🎨 Theme Support
The system features an automated **Dark / Light Mode** switch in the top navigation bar. Theme preference is automatically detected from system settings and saved in browser `localStorage`.
