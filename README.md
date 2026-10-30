# RythuSahayak

RythuSahayak is a Java 17 Maven WAR project for a farmer assistance and crop issue reporting portal. Farmers can submit and track complaints, register and view their linked complaints, and browse illustrative crop learning notes. Authorized administrators can review complaints and record status updates with farmer-visible remarks.

This is a student demonstration project, not an official government portal. Crop learning notes are illustrative and are not diagnosis or official agricultural advice.

The context root opens the login page for signed-out visitors and routes an existing session to its role-specific workspace. Farmer dashboard summaries and complaint lists are read from the signed-in farmer's own records.

Authenticated JSP pages share one role-aware navigation include: farmers see farmer services and profile, administrators see complaint management and public crop notes, and signed-out visitors see public routes plus login/registration. Signed-in users requesting the login or registration pages are redirected to their role-specific workspace.

## Requirements

- JDK 17
- Maven 3.8 or later
- Apache Tomcat 10.1 (Jakarta Servlet 6.0 and JSP 3.1)
- MySQL 8.0
- Internet access for Bootstrap, Bootstrap Icons, jQuery, and web fonts loaded from CDNs

Do not deploy this Jakarta application to Tomcat 9, which uses the older `javax.*` APIs.

## Build and tests

From the project directory:

```powershell
mvn test
mvn clean package
```

The deployable archive is `target\rythusahayak.war`. Unit tests do not require a live MySQL server; authentication, authorization, and complaint servlet behavior have isolated tests. Complete registration, database-backed complaints, and browser workflows require MySQL and Tomcat.

## Application routes

| Route | Access | Purpose |
|---|---|---|
| `/` | Public entry | Redirects to login when signed out; routes an authenticated user to the correct workspace |
| `/login.jsp` and `POST /login` | Public; authenticated users are redirected | Sign in with email and password |
| `/register.jsp` and `POST /register` | Public; authenticated users are redirected | Register a farmer account |
| `/farmer/complaints` | Farmer session | Farmer dashboard, own complaint counts, and five most recent linked complaints |
| `/farmer/profile` | Farmer session | Read-only name, email, and phone details |
| `/report-issue.jsp` and `POST /complaints/create` | Public form; optional farmer session | Submit a guest report or link it to the currently signed-in farmer |
| `/complaints/track?reference=...` | Public | Look up limited public status information by complaint reference |
| `/advisories` | Public | Browse and filter the XML learning-note catalogue |
| `/admin/complaints` | Administrator session | Admin dashboard with database-backed status counts; review reports and filter by status |
| `POST /admin/complaints/update` | Administrator session | Update status and add a public remark |
| `POST /logout` | Session + CSRF token | Invalidate the current session |

There is no complaint-edit or profile-edit feature. Public tracking intentionally exposes only the information selected by `ComplaintDAO.findPublicByReference`; a farmer's dashboard/profile queries use the authenticated session ID, not a client-provided user ID.

The farmer dashboard includes module links to report an issue, review recent complaints, track a reference, browse crop learning notes, and view the read-only profile. The administrator dashboard shows database-backed complaint totals by status and links to complaint review/status updates and the public crop-note catalogue. The shared mobile navigation uses the native keyboard-operable disclosure control.

## Local Setup and Demonstration

### Required software

- JDK 17 (the project compiler targets Java 17; use JDK 17 for consistent results).
- Maven 3.8 or newer.
- Apache Tomcat 10.1, which provides Jakarta Servlet 6.0 and JSP 3.1. Tomcat 9 is incompatible.
- MySQL 8.0 for registration, login, complaints, tracking, and administrator workflows.
- Internet access for the current Bootstrap, Bootstrap Icons, web fonts, and jQuery CDN links.

### Verify Java and Maven

From the project directory:

```powershell
java -version
mvn -version
```

`mvn -version` reports the Java runtime Maven actually uses. For a consistent setup, configure `JAVA_HOME` to JDK 17 for the terminal before running Maven; do not uninstall other JDK versions. In the inspected environment, Maven reported Java 17.0.19 and `java` on PATH reported Java 24.0.2.

### Locate or install Tomcat 10.1

Check `CATALINA_HOME` and the installation folder. In the inspected environment, Apache Tomcat 10.1.61 is present at `C:\Users\sangi\Downloads\apache-tomcat-10.1.61-windows-x64 (1)\apache-tomcat-10.1.61`; Tomcat 9 installations elsewhere must not be used for this application.

Download Tomcat 10.1 only from the official Apache page: <https://tomcat.apache.org/download-10.cgi>. For Windows, download the 64-bit Windows zip distribution, extract it to a user-writable folder (for example `%USERPROFILE%\tools\apache-tomcat-10.1.x`), and set `CATALINA_HOME` to that extracted directory in the current PowerShell session:

```powershell
$env:CATALINA_HOME = "$env:USERPROFILE\tools\apache-tomcat-10.1.x"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

Adjust both paths to the actual installed versions. These session-scoped assignments do not modify system settings.

### Check MySQL and configure credentials

The inspected environment had MySQL Server 8.0.45, the `MySQL80` service running, and TCP reachability at `127.0.0.1:3306`. TCP reachability does not prove that credentials or SQL permissions work.

The application expects these properties in `%USERPROFILE%\.rythusahayak\db.properties`:

```properties
jdbc.url=jdbc:mysql://127.0.0.1:3306/rythusahayak_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
jdbc.username=YOUR_LOCAL_MYSQL_USERNAME
jdbc.password=YOUR_LOCAL_PASSWORD
```

Create that file locally, outside the project. The project `.gitignore` excludes `db.properties` and the example file contains placeholders only. Alternatively, set `RYTHUSAHAYAK_DB_CONFIG` or the JVM property `rythusahayak.db.config` to a different local file path. Do not put credentials in the terminal command, source code, screenshots, or version control. The config path is resolved from the Tomcat process's user home unless an override is set.

### Initialize a new development database safely

Inspect `database\schema.sql` before running it. It selects `rythusahayak_db`, creates that database if absent, and creates the `users`, `complaints`, and `complaint_updates` InnoDB tables if they do not exist. It does not drop tables, but it does not migrate an older schema either. Only run it when you intend to use this specific development database. The optional `database\jdbc-demo.sql` adds the separate `jdbc_feature_demo` table and two sample rows; it does not modify user/complaint tables.

If you have the MySQL client at the inspected standard path, choose your own authorized account and execute from the project root:

```powershell
$mysqlUser = Read-Host "MySQL username"
Get-Content .\database\schema.sql | & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -u $mysqlUser -p
```

The client prompts for the password without putting it in the command line. Ask a database administrator to create a dedicated local application account if needed; do not guess/reset an existing account's password.

### Build, deploy, and open the application

```powershell
mvn test
mvn clean package
```

For an existing deployment, stop Tomcat first, confirm its HTTP port is no longer listening, back up both the WAR and its expanded application directory, then replace those deployment files. Do not continue if Tomcat has not stopped. This does not touch MySQL data.

```powershell
$tomcat = 'C:\Users\sangi\Downloads\apache-tomcat-10.1.61-windows-x64 (1)\apache-tomcat-10.1.61'
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $tomcat
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17.0.19'
& "$tomcat\bin\shutdown.bat"
for ($i = 0; $i -lt 30 -and (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue); $i++) { Start-Sleep -Seconds 1 }
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { throw 'Tomcat is still listening; do not replace the deployment.' }
$backup = Join-Path $tomcat "backup\rythusahayak-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Path $backup -Force | Out-Null
$war = Join-Path $tomcat 'webapps\rythusahayak.war'
$app = Join-Path $tomcat 'webapps\rythusahayak'
if (Test-Path $war) { Copy-Item -LiteralPath $war -Destination (Join-Path $backup 'rythusahayak.war') }
if (Test-Path $app) { Copy-Item -LiteralPath $app -Destination (Join-Path $backup 'exploded') -Recurse }
if (Test-Path $war) { Remove-Item -LiteralPath $war }
if (Test-Path $app) { Remove-Item -LiteralPath $app -Recurse }
Copy-Item -LiteralPath '.\target\rythusahayak.war' -Destination $war
& "$tomcat\bin\startup.bat"
```

Confirm Tomcat has stopped before replacing files. The Maven final name sets the context path to `/rythusahayak`. Local URLs are:

- Context root (signed out): <http://localhost:8080/rythusahayak/>
- Login: <http://localhost:8080/rythusahayak/login.jsp>
- Registration: <http://localhost:8080/rythusahayak/register.jsp>
- Farmer dashboard: <http://localhost:8080/rythusahayak/farmer/complaints>
- Farmer profile: <http://localhost:8080/rythusahayak/farmer/profile>
- Complaint form: <http://localhost:8080/rythusahayak/report-issue.jsp>
- Complaint tracking form: <http://localhost:8080/rythusahayak/track-complaint.jsp>
- Crop advisories: <http://localhost:8080/rythusahayak/advisories>

Start the application and inspect Tomcat's logs for deployment or JSP compilation errors. Stop Tomcat with:

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
```

### Database-independent and database-backed features

The XML advisory catalogue and its filtering work independently of MySQL. Maven XML/password/validation tests also run without MySQL or Tomcat. Account registration/login, complaint submission and tracking, farmer/admin dashboards, and the JDBC demonstration require an external DB config and MySQL. JSP/Servlet execution requires Tomcat 10.1.

### Common setup errors

- **Java version mismatch / `JAVA_HOME`:** compare `java -version` with `mvn -version`. Set `$env:JAVA_HOME` to JDK 17 and reopen Maven commands in that terminal.
- **MySQL access denied (SQLState `28000`, error `1045`):** the credentials loaded by the application are not accepted for that database host/account. A successful CLI test only proves the password typed at that prompt works; it does not prove it matches the password in the app's config file. Check the `config file` path in the application log, then update that local file with the known-good password without displaying it or putting it in a command. If the file path belongs to a different Windows account, configure `-Drythusahayak.db.config=<absolute path>` for the Tomcat process or place the config in that account's `%USERPROFILE%\.rythusahayak\` directory. Do not reset the MySQL account password unless the database administrator determines it is necessary.
- **Database connection failure:** confirm `MySQL80` is running and `127.0.0.1:3306` is reachable; then check the external properties file path, URL, and credentials.
- **Missing schema/tables:** run `database\schema.sql` only against the intended `rythusahayak_db`, after reviewing its `CREATE DATABASE/TABLE IF NOT EXISTS` statements.
- **Tomcat deployment failure:** confirm Tomcat 10.1 (not 9), JDK 17, and inspect `%CATALINA_HOME%\logs`.
- **JSP compilation error:** inspect Tomcat logs and confirm the WAR deployed on Tomcat 10.1 with the expected JSTL and Servlet/JSP container APIs.
- **Port 8080 already in use:** identify the process using the port before changing Tomcat's connector port; do not terminate unknown processes.
- **HTTP 404:** confirm Tomcat startup/deployment completed, the WAR context name is `rythusahayak`, and the URL uses `/advisories` for the current advisory servlet.
- **HTTP 500:** inspect Tomcat logs for the underlying error; check database config for DB-backed pages. Do not expose credentials while collecting logs.

### Manual testing and presentation

Use fabricated data and review the step-by-step sequence and screenshot list in [docs/DEMO_CHECKLIST.md](docs/DEMO_CHECKLIST.md). It covers farmer and administrator workflows, XML filters, responsive pages, and a backup plan if Tomcat or MySQL is unavailable. Do not present an unverified screenshot as evidence of a current live deployment.

## XML crop advisory catalogue

The advisory catalogue is demonstration data stored in `src\main\resources\xml\crop-advisories.xml`. It uses the `urn:rythusahayak:crop-advisories` XML namespace and contains 10 sample records with a unique ID, crop, issue category, title, description, recommendation, optional growth stage, language, review date, and source note. The examples are educational, are not official or personalized advice, and should be reviewed by a qualified agricultural expert before real-world use.

`crop-advisories.dtd` describes the allowed element order and required ID attribute. A DTD can validate document structure and basic XML ID uniqueness, but it does not offer XSD's strong data types, date validation, or enumerated category values. `crop-advisories.xsd` targets the same namespace and validates required content, field lengths, categories, IDs, and review dates.

`XmlAdvisoryService` loads both XML and XSD from the classpath, validates the XML against the schema, and maps its namespace-aware elements into Java `CropAdvisory` objects. The runtime parser enables secure processing, disables external entities and DTD loading, rejects other doctypes, and prevents external schema/DTD access. For the DTD classroom demonstration, a separate validator resolves only the bundled DTD resource. The advisory servlet and page operate without a database connection. The old `advisory.jsp` URL redirects to the servlet-backed `/advisories` route.

To add an advisory, add a `<cat:advisory>` element to the catalogue with a new `id` and all required child elements, choose one of the XSD category values (`pest`, `disease`, `irrigation`, `nutrient`, `general`), use an ISO date for `lastReviewed`, then run `mvn test`. Keep language-neutral, generic educational wording and do not add unverified diagnoses or treatment prescriptions.

## Advanced JDBC demonstration

The standalone `com.rythusahayak.util.JdbcFeatureDemo` utility demonstrates database and driver metadata, ResultSet metadata, scrollable and requested updatable ResultSets, a connected `JdbcRowSet`, a disconnected `CachedRowSet`, and transaction control using read-only queries. It never updates complaint or account records; the updatable cursor example reports whether the driver grants it and performs no row updates. `JdbcRowSet` remains connected while it is used; `CachedRowSet` copies rows into memory and can be used after its source connection is closed.

After configuring a dedicated database account and applying `database\schema.sql`, optionally run `database\jdbc-demo.sql` against `rythusahayak_db` to create the separate `jdbc_feature_demo` table and idempotent sample rows. In PowerShell, provide your local MySQL username and run:

```powershell
$mysqlUser = Read-Host "MySQL username"
Get-Content .\database\jdbc-demo.sql | & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -u $mysqlUser -p rythusahayak_db
```

Adjust the client path if MySQL is installed elsewhere; the password is requested interactively and should not be placed in the command. Then run the class using the runtime classpath:

```powershell
mvn dependency:build-classpath -DincludeScope=runtime "-Dmdep.outputFile=target\jdbc-classpath.txt"
$classpath = "target\classes;" + (Get-Content target\jdbc-classpath.txt -Raw).Trim()
java -cp $classpath com.rythusahayak.util.JdbcFeatureDemo
```

The utility requires locally configured MySQL credentials. Scrollable/updatable cursor behavior depends on Connector/J and server support; the program checks metadata and reports when an updatable cursor is refused. Automated tests do not connect to MySQL.

## Configure MySQL

1. Use a MySQL account with permission to create the project database and tables, or have a database administrator run `database\schema.sql`. The script only creates the database and tables if they do not already exist; it does not drop or reset existing data.
2. Copy `src\main\resources\db.properties.example` to `%USERPROFILE%\.rythusahayak\db.properties`.
3. Set `jdbc.url`, `jdbc.username`, and `jdbc.password` in that local file to match the database and a dedicated local application account. Do not commit the file or place credentials in source code.

An alternate config path can be supplied with the `RYTHUSAHAYAK_DB_CONFIG` environment variable or the `rythusahayak.db.config` JVM system property. The application reports a clear configuration error if required settings are absent or left as placeholders.

## Provision the first administrator

Public registration creates farmer accounts only. After configuring the database, run these PowerShell commands from the project directory in a terminal that supports hidden password entry:

```powershell
mvn dependency:build-classpath -DincludeScope=runtime "-Dmdep.outputFile=target\admin-classpath.txt"
$classpath = "target\classes;" + (Get-Content target\admin-classpath.txt -Raw).Trim()
java -cp $classpath com.rythusahayak.util.AdminProvisioner
```

The provisioner refuses to run when an administrator already exists; it does not use a built-in password.

## Deploy to Tomcat 10.1

1. Configure Tomcat 10.1 to run on JDK 17.
2. Build the WAR with `mvn clean package`.
3. Copy `target\rythusahayak.war` to `%CATALINA_HOME%\webapps\rythusahayak.war`. Check the destination first; do not overwrite an existing deployment unless intended.
4. Start Tomcat with `%CATALINA_HOME%\bin\startup.bat`.
5. Open <http://localhost:8080/rythusahayak/>.

To stop Tomcat, run `%CATALINA_HOME%\bin\shutdown.bat`. For a public deployment, use HTTPS and configure the reverse proxy/container so the application correctly recognizes secure requests.

## Current environment notes

Maven may use a different Java installation than the `java` command on PATH. Check both with `mvn -version` and `java -version`; the project compiler targets Java 17. Tomcat and MySQL service state depends on the machine where the WAR is deployed.

XML validation, parsing, filtering, malformed-input handling, and external-entity rejection are covered by database-independent Maven tests. Authentication, access control, and complaint servlet behavior have focused automated tests. Live MySQL/Tomcat workflows must be reported separately from those unit tests; do not infer database write success from a passing build.
