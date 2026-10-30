# RythuSahayak College Demonstration Checklist

## Before the demonstration

- [ ] Confirm Java 17 and Maven with `mvn -version`. The `java` command can select another installed runtime; Maven's Java runtime is the build runtime.
- [ ] Install and extract Apache Tomcat 10.1 if it is not already installed. This project uses Jakarta Servlet 6 and must not be run on Tomcat 9.
- [ ] Start MySQL and create/configure the project database using the local-only settings in the README. Never show the DB properties file or passwords in screenshots.
- [ ] Build from the project root with `mvn clean package`.
- [ ] Deploy `target\rythusahayak.war` to Tomcat 10.1's `webapps` folder and check Tomcat logs for successful deployment.
- [ ] Provision an administrator through `com.rythusahayak.util.AdminProvisioner`; it prompts for the password without echoing it and refuses when an administrator already exists.
- [ ] Use fabricated classroom identities and complaint descriptions, not real farmer or personal data.
- [ ] Confirm the home page and the `/advisories` page open before the presentation.
- [ ] If demonstrating JDBC, configure the local DB and optionally create the isolated `jdbc_feature_demo` table with `database\jdbc-demo.sql`.

## 5–7 minute walkthrough

1. **Home and purpose (30–45 seconds):** introduce RythuSahayak as a student demonstration portal, not an official service.
2. **Farmer account (45–60 seconds):** create a farmer account using fabricated details, then sign in. Avoid displaying the password.
3. **Complaint (60–75 seconds):** submit a sample crop concern; point out the server-side form validation and generated reference.
4. **Public tracking (45–60 seconds):** use the reference to show the public status view. Explain that it omits the farmer's mobile number and full private description.
5. **Administrator workflow (60–75 seconds):** sign in separately as the provisioned administrator, review the report, update status with a suitable public remark, then return to tracking to show the new status.
6. **XML advisories (60–75 seconds):** open `/advisories`, filter by crop and issue category, search a title/description, demonstrate clear filters and the no-results state. Explain that records are educational samples, not official or real-time advice.
7. **Implementation summary (45–60 seconds):** identify the Servlet/JSP, JDBC/MySQL, XML/DTD/XSD, CSS/Bootstrap, JavaScript and jQuery responsibilities. Mention automated tests separately from live deployment checks.

## Short technology explanation

- **XML:** stores the 10 advisory records with a namespace and fields.
- **DTD:** checks the element sequence and required ID attribute.
- **XSD:** adds typed dates, category restrictions, lengths, and ID uniqueness.
- **Java XML parser:** validates the catalogue against XSD, maps namespaced elements to Java records, and blocks external entity/DTD access.
- **JDBC:** DAOs use parameterized SQL for app data; the separate optional utility demonstrates metadata, result sets, RowSets, and transaction control using a dedicated demo table.
- **Servlets:** receive requests, validate/dispatch actions, and protect server-side routes.
- **JSP:** renders escaped values and server-provided data.
- **CSS and Bootstrap:** provide responsive layout and reusable interface components.
- **JavaScript and jQuery:** provide form behavior and advisory filtering enhancements.

## Screenshot checklist

- [ ] Home page at desktop width.
- [ ] Farmer registration/login form with no real user details visible.
- [ ] Complaint form showing client-side validation or the blank form.
- [ ] Complaint confirmation with a fabricated reference (blur/redact it if it could be reused).
- [ ] Public tracking result using fabricated data.
- [ ] Admin dashboard with fabricated complaint data and no personally identifying information.
- [ ] Advisory catalogue and one filtered view.
- [ ] Advisory empty-results state.
- [ ] Optional: successful `mvn test` summary and WAR contents listing; ensure terminal output contains no credentials.

Do not claim screenshots or live runs were performed unless they were actually captured during this demo setup.

## Backup plan

- **MySQL or DB credentials unavailable:** show the independently loaded XML advisory page and explain that account/complaint operations need the database. Do not invent a successful login or complaint workflow.
- **Tomcat unavailable:** show the Maven test/build results and source/packaged WAR inspection. Be explicit that JSP/Servlet execution was not demonstrated.
- **Presentation network unavailable:** Bootstrap, icons, fonts, and jQuery are loaded from CDNs; some styling/enhancements may be missing. The app has an advisory filter fallback, but do not assume the CDN-dependent appearance.
- Keep screenshots as supplemental materials only; do not present them as evidence of a current live server if they are from another run.

## Known limitations and verification status

- Automated XML and unit tests can run without MySQL or Tomcat.
- Live login, registration, complaints, tracking and admin updates require configured MySQL and Tomcat 10.1.
- The JDBC demo requires the local DB configuration and optional demo table. Scrollable/updatable cursor support depends on the Connector/J/server combination and is reported by the utility.
- Crop advisory content is demonstration data, not qualified diagnosis, government guidance, or real-time outbreak information.
- The local Java command may use Java 24 while Maven uses Java 17. Check both versions before the demo.
- Login rate limiting is not implemented.
