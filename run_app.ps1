$env:JAVA_HOME = "C:\Program Files\JetBrains\PyCharm Community Edition 2023.2.2\jbr"
$env:PATH = "$env:JAVA_HOME\bin;E:\S.G.jadhav\projects\Personal_Expense_Tracker\apache-maven-3.9.6\bin;$env:PATH"

Write-Output "Starting Maven build and run..." > run_log.txt
mvn clean spring-boot:run -DskipTests >> run_log.txt 2>&1
