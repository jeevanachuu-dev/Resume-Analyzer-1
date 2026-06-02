# Resume Analyzer

Resume Analyzer is a full stack web application for candidate resume submission, automatic job-match scoring, resume comparison, recruiter dashboards, and admin shortlisting.

The project uses a Spring Boot backend, a simple HTML/CSS/JavaScript frontend, and a database layer that supports both local H2 storage and MySQL.

## Tech Stack

| Layer | Technologies Used |
| --- | --- |
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 17+, Spring Boot 3.3.5 |
| API | Spring Web REST APIs |
| Database Access | Spring Data JPA, Hibernate |
| Database | MySQL, H2 local file database |
| Build Tool | Maven |
| Runtime Script | PowerShell `run.ps1` |
| UI Assets | Google Fonts, Lucide icons, Unsplash images |

## Project Features

- Candidates can view available jobs.
- Users can register and login from the Account page.
- Admin can view registered users, login count, last login time, and current login status.
- Candidates can apply for a job by filling details and uploading a resume.
- The system calculates a resume match score based on skills, education, experience, resume text, and job requirements.
- Candidates can search an application by ID and view score details.
- Candidates can compare one resume against multiple jobs.
- Recruiters can view dashboard metrics such as total applications, average score, daily applications, and top skills.
- Admin can log in, view ranked applications, filter shortlisted candidates, shortlist candidates, and download uploaded resumes.
- The project stores application data in the database and stores uploaded resume files in the local upload folder.

## Default Job Options

The project automatically inserts sample job postings on startup using `DataSeeder.java`.

The default roles include classic and current fresher-friendly options such as:

- Java Spring Boot Developer.
- Frontend Developer.
- Graduate Data Analyst.
- AI Prompt Engineering Intern.
- Cloud Support Associate.
- Cybersecurity Analyst Trainee.
- DevOps Engineer Trainee.
- Full Stack Developer - Fresher.
- Python Developer Intern.
- QA Automation Tester - Fresher.
- Data Engineer Trainee.
- Business Analyst Trainee.
- UI/UX Designer - Fresher.
- Technical Support Engineer - Fresher.
- Machine Learning Intern.

These are sample project jobs for testing resume matching. They are not live job openings.

## How The Project Works

### 1. Application Startup

When the Spring Boot application starts:

- `ResumeAnalyzerApplication.java` starts the backend server.
- Spring Boot loads configuration from `application.properties`.
- If the MySQL profile is active, Spring Boot also loads `application-mysql.properties`.
- Hibernate creates or updates database tables automatically because `spring.jpa.hibernate.ddl-auto=update`.
- `DataSeeder.java` inserts default job postings if they are not already present.
- Static frontend files from `src/main/resources/static` are served at `http://localhost:8080`.

### 2. Candidate Application Workflow

Candidate workflow:

1. Candidate opens the web app.
2. Frontend loads jobs from `/api/jobs`.
3. Candidate fills name, email, phone, experience, skills, education, selects a job, and uploads a resume.
4. Frontend sends a multipart request to `/api/applications`.
5. Backend validates the selected job and resume file.
6. Resume file is saved inside `uploads/resumes`.
7. Resume text is extracted in a simple readable-text format.
8. `ResumeScoringService` compares candidate details with job requirements.
9. Score, matched skills, missing skills, suggestions, resume metadata, and candidate details are saved in the database.
10. Frontend displays the score result to the candidate.

### 3. User Register And Login Workflow

User account workflow:

1. User opens the `Account` tab.
2. User can register with full name, email, username, and password.
3. Backend validates required fields, email format, duplicate email, duplicate username, and password length.
4. Passwords are stored as PBKDF2 hashes, not plain text.
5. After successful registration, the user is logged in automatically.
6. Existing users can login using username or email.
7. Login updates `lastLoginAt` and `loginCount`.
8. Admin can see all registered users in the Admin dashboard.

User account data is stored in:

```text
registered_users
```

### 4. Resume Scoring Workflow

The scoring logic is handled by `ResumeScoringService.java`.

The score is calculated using:

- Required skills matched with candidate skills and resume text.
- Candidate experience compared with job minimum experience.
- Education keywords such as bachelor, degree, B.Tech, MCA, MBA, etc.
- Resume content depth based on text length.
- Job title keyword matches.

The final result includes:

- Numeric score out of 100.
- Score label such as `Excellent match`, `Strong match`, `Good potential`, or `Needs improvement`.
- Matched skills.
- Missing skills.
- Resume improvement suggestions.

### 5. Resume Comparison Workflow

Resume comparison allows a candidate to compare one resume against multiple jobs.

Workflow:

1. Candidate opens the `Compare` view.
2. Candidate enters skills, experience, education, selects jobs or leaves all jobs unselected.
3. Candidate uploads a resume.
4. Frontend sends the request to `/api/applications/compare`.
5. Backend scores the resume against selected jobs or all jobs.
6. Jobs are ranked by highest match score.
7. Frontend displays the best matching jobs.

Comparison does not save a new candidate application. It only analyzes and returns ranked job matches.

### 6. Admin Workflow

Admin workflow:

1. Admin opens the `Admin` tab.
2. Admin logs in using username and password.
3. Login request goes to `/api/admin/login`.
4. Backend creates a temporary admin token.
5. Frontend stores the token in browser local storage.
6. Protected admin APIs require this token in the `Authorization` header.
7. Admin can view registered users and see who is currently logged in.
8. Admin can view applications, sort by rank/newest/score, filter shortlisted candidates, shortlist candidates, and download resumes.

Default admin login:

```text
username: admin
password: admin123
```

Admin authentication is implemented using:

- `AdminAuthService.java`
- `AdminAuthInterceptor.java`
- `WebConfig.java`

### 7. Dashboard Workflow

The dashboard uses application data to show recruiter analytics.

Dashboard API:

```text
GET /api/applications/dashboard
```

Dashboard shows:

- Total applications.
- Average resume score.
- Today's applications.
- Top skills from submitted applications.
- Daily application count for the last 7 days.

## Database And Storage

### MySQL Storage

When you run the project with MySQL, data is stored in the MySQL database:

```text
resume_analyzer1
```

Main database tables:

```text
job_postings
candidate_applications
registered_users
```

`job_postings` stores job details such as title, company, location, description, required skills, and minimum experience.

`candidate_applications` stores candidate details, selected job reference, score, matched skills, missing skills, suggestions, shortlist status, resume file metadata, and application date.

`registered_users` stores user registration details, password hash, registration time, last login time, and login count.

Create the database manually if needed:

```sql
CREATE DATABASE IF NOT EXISTS resume_analyzer1;
```

The project can also create the database automatically when this MySQL URL is used:

```text
jdbc:mysql://localhost:3306/resume_analyzer1?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

### Local H2 Storage

If you run the project without MySQL, it uses a local H2 database file:

```text
data/resume_analyzer.mv.db
```

Default local database connection:

```properties
spring.datasource.url=jdbc:h2:file:./data/resume_analyzer
spring.datasource.username=sa
spring.datasource.password=
```

H2 console is available in local mode:

```text
http://localhost:8080/h2-console
```

### Resume File Storage

Uploaded resume files are not stored directly inside the database.

Files are stored in:

```text
uploads/resumes
```

The database stores only resume metadata, such as:

- Original file name.
- Stored file name.
- Content type.
- File size.
- Download URL.

## Main Project Files

| File / Folder | Purpose |
| --- | --- |
| `pom.xml` | Maven project configuration and dependencies |
| `run.ps1` | PowerShell script to run the project with local DB or MySQL |
| `database.sql` | Optional MySQL database creation script |
| `src/main/java/com/resumeanalyzer/ResumeAnalyzerApplication.java` | Main Spring Boot application entry point |
| `src/main/java/com/resumeanalyzer/config/DataSeeder.java` | Inserts default jobs on startup |
| `src/main/java/com/resumeanalyzer/controller` | REST API controllers |
| `src/main/java/com/resumeanalyzer/service` | Business logic, scoring, resume storage, admin auth |
| `src/main/java/com/resumeanalyzer/entity` | JPA database entities |
| `src/main/java/com/resumeanalyzer/repository` | Spring Data JPA repositories |
| `src/main/resources/application.properties` | Default local database and app configuration |
| `src/main/resources/application-mysql.properties` | MySQL profile configuration |
| `src/main/resources/static/index.html` | Main frontend page |
| `src/main/resources/static/styles.css` | Frontend styling and layout |
| `src/main/resources/static/app.js` | Frontend API calls and UI behavior |
| `uploads/resumes` | Uploaded resume files |
| `data` | Local H2 database file location |

## Important API Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/jobs` | Get all available jobs |
| `POST` | `/api/applications` | Submit a candidate application |
| `POST` | `/api/applications/compare` | Compare one resume against jobs |
| `GET` | `/api/applications/{id}` | Get application score and details |
| `GET` | `/api/applications/stats` | Get application statistics |
| `GET` | `/api/applications/dashboard` | Get recruiter dashboard data |
| `POST` | `/api/users/register` | Register a user account |
| `POST` | `/api/users/login` | Login a user |
| `POST` | `/api/users/logout` | Logout a user |
| `GET` | `/api/users/me` | Get current logged-in user |
| `POST` | `/api/admin/login` | Admin login |
| `POST` | `/api/admin/logout` | Admin logout |
| `GET` | `/api/admin/applications` | Get admin application list |
| `PATCH` | `/api/admin/applications/{id}/shortlist` | Shortlist or unshortlist candidate |
| `GET` | `/api/admin/applications/{id}/resume` | Download candidate resume |
| `GET` | `/api/admin/stats` | Get admin statistics |
| `GET` | `/api/admin/users` | Get registered users and login status |

## Run The Project Step By Step

### Requirements

- Java 17 or newer.
- Maven.
- MySQL server, if running with MySQL.

Check Java and Maven:

```powershell
java -version
mvn -version
```

### Open Project Folder

```powershell
cd "C:\Users\harsha\OneDrive\Desktop\Resume Analyzer 1\Resume Analyzer"
```

### Run With MySQL

Make sure MySQL is running, then create the database if it does not exist:

```sql
CREATE DATABASE IF NOT EXISTS resume_analyzer1;
```

Run the project:

```powershell
.\run.ps1 -Database mysql -MysqlUser "root" -MysqlPassword "your_mysql_password"
```

If your MySQL root user has no password:

```powershell
.\run.ps1 -Database mysql -MysqlUser "root" -MysqlPassword ""
```

If you need a custom MySQL URL:

```powershell
.\run.ps1 -Database mysql -MysqlUser "root" -MysqlPassword "your_mysql_password" -MysqlUrl "jdbc:mysql://localhost:3306/resume_analyzer1?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
```

### Run With Local H2 Database

```powershell
.\run.ps1
```

### Run On Another Port

Use this if port `8080` is already busy:

```powershell
.\run.ps1 -ServerPort 8081
```

For MySQL on another port:

```powershell
.\run.ps1 -Database mysql -MysqlUser "root" -MysqlPassword "your_mysql_password" -ServerPort 8081
```

### Open In Browser

Default URL:

```text
http://localhost:8080
```

If using port `8081`:

```text
http://localhost:8081
```

### Stop The Project

Press `Ctrl + C` in the PowerShell terminal where the server is running.

## Build And Test

Run tests:

```powershell
mvn test
```

Build the project:

```powershell
mvn clean package
```

Run using Maven directly:

```powershell
mvn spring-boot:run
```

## Configuration Details

Default local configuration is in:

```text
src/main/resources/application.properties
```

MySQL configuration is in:

```text
src/main/resources/application-mysql.properties
```

The `run.ps1` script sets these environment variables when MySQL mode is used:

```text
SPRING_PROFILES_ACTIVE=mysql
MYSQL_URL
MYSQL_USER
MYSQL_PASSWORD
SERVER_PORT
```

Admin credentials can be changed using:

```text
ADMIN_USERNAME
ADMIN_PASSWORD
```

Upload directory can be changed using:

```text
UPLOAD_DIR
```

## Notes

- Hibernate creates and updates tables automatically.
- Default jobs are inserted automatically on startup.
- Uploaded resumes are stored on disk, not as database blobs.
- Registered user passwords are stored as hashes, not plain text.
- PDF, DOC, DOCX, and TXT resume files are accepted.
- Resume text extraction is simple readable-text extraction, so TXT files give the clearest text parsing.
- Admin tokens are temporary in-memory sessions and expire after 8 hours.
- User login tokens are temporary in-memory sessions and expire after 8 hours.
